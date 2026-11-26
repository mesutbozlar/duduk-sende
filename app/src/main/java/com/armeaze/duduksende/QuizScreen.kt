package com.armeaze.duduksende

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

fun buildPool(spec: QuizSpec): List<Q> = when (spec.kind) {
    Kind.CATEGORY -> Questions.forLevel(spec.cat, spec.level)
    Kind.DAILY -> Questions.daily()
    Kind.PRACTICE -> {
        val ids = spec.ids
        if (ids != null) ids.mapNotNull { Questions.byId(it) }.ifEmpty { Questions.all.shuffled().take(10) }
        else Questions.all.shuffled().take(10)
    }
    Kind.TIME, Kind.SUDDEN -> Questions.all.shuffled()
    Kind.VIDEO -> emptyList()
    Kind.DUEL -> (spec.ids ?: emptyList()).mapNotNull { Questions.byId(it) }
        .ifEmpty { Questions.all.shuffled().take(5) }
}

private const val TIMEOUT = 9

@Composable
fun QuizScreen(
    tr: Boolean, store: Store, services: AppServices, spec: QuizSpec,
    onExit: () -> Unit, onFinish: (Int, Int) -> Unit
) {
    var pool by remember { mutableStateOf<List<Q>?>(null) }
    LaunchedEffect(Unit) {
        val base = buildPool(spec)
        val useAi = services.ai.enabled && spec.ids == null && (spec.kind == Kind.CATEGORY || spec.kind == Kind.PRACTICE)
        pool = if (useAi) {
            val res = withTimeoutOrNull(7000) {
                coroutineScope { base.map { q -> async { services.ai.variant(q) ?: q } }.awaitAll() }
            }
            res ?: base
        } else base
    }
    val p = pool
    if (p == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { BackBtn(onExit) }
            Spacer(Modifier.weight(1f))
            Text("🤖", fontSize = 64.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                tx(tr, "Preparing fresh questions…", "Yeni sorular hazırlanıyor…"),
                fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1f))
        }
    } else {
        QuizBody(tr, store, spec, p, onExit, onFinish)
    }
}

@Composable
fun QuizBody(tr: Boolean, store: Store, spec: QuizSpec, pool: List<Q>, onExit: () -> Unit, onFinish: (Int, Int) -> Unit) {
    var idx by remember { mutableStateOf(0) }
    var correct by remember { mutableStateOf(0) }
    var answered by remember { mutableStateOf(0) }
    var picked by remember { mutableStateOf(-1) }      // option index, TIMEOUT = time out
    var timeLeft by remember { mutableStateOf(60) }    // Time Attack total
    var qLeft by remember { mutableStateOf(0) }        // per-question timer
    var done by remember { mutableStateOf(false) }
    var ruleOpen by remember { mutableStateOf(false) }

    val q = pool[idx % pool.size]
    val lawIdx = Rules.forCat(q.cat)
    val order = remember(idx) { (0 until q.o(tr).size).shuffled() }
    val perQ = when (spec.kind) {
        Kind.CATEGORY -> Levels.SECONDS[spec.level - 1]
        Kind.DUEL -> 15
        else -> 0
    }
    val fixedLength = spec.kind == Kind.CATEGORY || spec.kind == Kind.DAILY ||
        spec.kind == Kind.PRACTICE || spec.kind == Kind.DUEL

    fun finish() {
        if (!done) {
            done = true
            Sfx.whistle()
            onFinish(correct, answered)
        }
    }

    fun pick(i: Int) {
        if (picked >= 0 || done) return
        picked = i
        answered += 1
        store.recordAnswer(q.cat, i in order.indices && order[i] == q.ans, q.id)
        if (i in order.indices && order[i] == q.ans) {
            correct += 1
            Sfx.correct()
        } else {
            Sfx.wrong()
        }
    }

    fun next() {
        val right = picked in order.indices && order[picked] == q.ans
        picked = -1
        if (spec.kind == Kind.SUDDEN && !right) {
            finish()
        } else if (spec.kind != Kind.TIME && idx + 1 >= pool.size) {
            finish()
        } else {
            idx += 1
        }
    }

    LaunchedEffect(Unit) {
        if (spec.kind == Kind.TIME) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft -= 1
            }
            finish()
        }
    }

    LaunchedEffect(idx) {
        if (perQ > 0) {
            qLeft = perQ
            while (qLeft > 0 && picked < 0) {
                delay(1000)
                qLeft -= 1
            }
            if (picked < 0) pick(TIMEOUT)
        }
    }

    val title = when (spec.kind) {
        Kind.CATEGORY -> "${Cats.emoji[spec.cat]} ${Cats.name(tr, spec.cat)}"
        Kind.DAILY -> tx(tr, "Daily challenge", "Günün görevi")
        Kind.PRACTICE -> tx(tr, "Practice", "Antrenman")
        Kind.TIME -> "⏱ $timeLeft s"
        Kind.SUDDEN -> tx(tr, "Sudden Death", "Ani Ölüm")
        Kind.DUEL -> "⚔️ " + tx(tr, "Duel", "Düello")
        Kind.VIDEO -> "📺 Video"
    }
    val counter = if (fixedLength) "${idx + 1} / ${pool.size}" else "✔ $correct"
    val right = picked in order.indices && order[picked] == q.ans
    val isLast = (fixedLength && idx + 1 >= pool.size) || (spec.kind == Kind.SUDDEN && !right && picked >= 0)

    if (ruleOpen && lawIdx >= 0) {
        val law = Rules.laws[lawIdx]
        AlertDialog(
            onDismissRequest = { ruleOpen = false },
            confirmButton = { TextButton(onClick = { ruleOpen = false }) { Text(tx(tr, "Close", "Kapat")) } },
            title = { Text(tx(tr, "Law", "Kural") + " " + law.no + " · " + law.title(tr), color = Color.White) },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { LawBody(tr, law) } },
            containerColor = C.Card
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackBtn(onExit)
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(counter, color = C.Yellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(10.dp))
        if (fixedLength) Bar((idx + (if (picked >= 0) 1 else 0)).toFloat() / pool.size, C.Yellow)
        Spacer(Modifier.height(12.dp))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            CategoryArt(q.cat, Modifier.fillMaxWidth().height(76.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Tag(Levels.name(tr, q.diff).uppercase())
                Tag(Cats.name(tr, q.cat), C.Muted)
                if (perQ > 0 && picked < 0) Tag("⏱ $qLeft", if (qLeft <= 5) C.Red else C.Yellow)
            }
            Spacer(Modifier.height(14.dp))
            Text(q.q(tr), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            val opts = q.o(tr)
            for (i in order.indices) {
                val real = order[i]
                val state = when {
                    picked < 0 -> 0
                    real == q.ans -> 1
                    i == picked -> 2
                    else -> 3
                }
                val bg = when (state) {
                    1 -> C.Green.copy(alpha = 0.55f)
                    2 -> C.Red.copy(alpha = 0.55f)
                    else -> C.Card
                }
                val br = when (state) {
                    1 -> C.Green
                    2 -> C.Red
                    else -> C.Border
                }
                val sc by animateFloatAsState(if (state == 1 || state == 2) 1.03f else 1f, label = "opt")
                AppCard(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp).graphicsLayer(scaleX = sc, scaleY = sc),
                    border = br, container = bg,
                    onClick = { pick(i) }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF24405F)),
                            contentAlignment = Alignment.Center
                        ) { Text("ABCD"[i].toString(), fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(12.dp))
                        Text(opts[real], fontSize = 16.sp, modifier = Modifier.weight(1f))
                        if (state == 1) Text("✔", fontSize = 20.sp)
                        if (state == 2) Text("✖", fontSize = 20.sp)
                    }
                }
            }
            if (picked >= 0) {
                Spacer(Modifier.height(4.dp))
                AppCard(Modifier.fillMaxWidth(), border = if (right) C.Green else C.Red) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (right) tx(tr, "✅ Correct!", "✅ Doğru!")
                            else if (picked == TIMEOUT) tx(tr, "⏰ Time's up", "⏰ Süre doldu")
                            else tx(tr, "❌ Wrong", "❌ Yanlış"),
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(q.w(tr), color = Color(0xFFCBD5E1))
                        if (lawIdx >= 0) {
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = { ruleOpen = true }) {
                                Text("📖 " + tx(tr, "Related law", "İlgili kural") + ": " + (lawIdx + 1), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        if (picked >= 0) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                if (isLast) tx(tr, "Finish quiz →", "Quizi bitir →") else tx(tr, "Next →", "İleri →"),
                { next() }
            )
        }
    }
}
