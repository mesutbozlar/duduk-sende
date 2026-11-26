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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Video quiz: 5 animasyonlu pozisyon. Normalde yalnızca yayın kamerası vardır.
 * Emin değilsen "VAR" düğmesiyle (her quizde 2 hak) 3 kamera, ağır çekim ve ofsayt çizgisini açarsın.
 * Cevap verdikten sonra VAR araçları ücretsiz açılır (inceleme).
 */
@Composable
fun VideoQuizScreen(tr: Boolean, onExit: () -> Unit, onFinish: (Int, Int) -> Unit) {
    val pool = remember { VideoBank.pick(5) }
    val mirrors = remember { List(pool.size) { VideoBank.randomMirror() } }
    var idx by remember { mutableStateOf(0) }
    var correct by remember { mutableStateOf(0) }
    var picked by remember { mutableStateOf(-1) }
    var varLeft by remember { mutableStateOf(2) }
    var varOpen by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    val vq = pool[idx]
    val order = remember(idx) { (0 until vq.oEn.size).shuffled() }
    val cdata = remember(idx) { VideoBank.clip(vq.clipId) }
    val answered = picked >= 0
    val right = answered && order[picked] == vq.ans
    val isLast = idx + 1 >= pool.size

    fun pick(i: Int) {
        if (answered || done) return
        picked = i
        if (order[i] == vq.ans) {
            correct += 1
            Sfx.correct()
        } else {
            Sfx.wrong()
        }
    }

    fun next() {
        if (isLast) {
            if (!done) {
                done = true
                Sfx.whistle()
                onFinish(correct, pool.size)
            }
        } else {
            picked = -1
            varOpen = false
            idx += 1
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackBtn(onExit)
            Spacer(Modifier.width(12.dp))
            Text(
                "📺 " + tx(tr, "Video quiz", "Video quiz"),
                fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
            )
            Text("${idx + 1} / ${pool.size}", color = C.Yellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(10.dp))
        Bar((idx + (if (answered) 1 else 0)).toFloat() / pool.size, C.Yellow)
        Spacer(Modifier.height(12.dp))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            key(idx) {
                ClipPlayer(cdata, mirrors[idx], varOpen || answered, tr)
            }
            Spacer(Modifier.height(8.dp))
            if (!answered) {
                OutlinedButton(
                    onClick = {
                        varOpen = true
                        varLeft -= 1
                    },
                    enabled = !varOpen && varLeft > 0,
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text(
                        if (varOpen) "📺 " + tx(tr, "VAR check is on", "VAR incelemesi açık")
                        else "📺 " + tx(tr, "Ask VAR", "VAR'a sor") + " ($varLeft)"
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(Levels.name(tr, vq.diff).uppercase())
                Tag(tx(tr, "VIDEO", "VİDEO"), C.Muted)
            }
            Spacer(Modifier.height(10.dp))
            Text(vq.q(tr), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            val opts = vq.o(tr)
            for (i in order.indices) {
                val real = order[i]
                val state = when {
                    !answered -> 0
                    real == vq.ans -> 1
                    i == picked -> 2
                    else -> 3
                }
                val sc by animateFloatAsState(if (state == 1 || state == 2) 1.03f else 1f, label = "vopt")
                AppCard(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp).graphicsLayer(scaleX = sc, scaleY = sc),
                    border = when (state) {
                        1 -> C.Green
                        2 -> C.Red
                        else -> C.Border
                    },
                    container = when (state) {
                        1 -> C.Green.copy(alpha = 0.55f)
                        2 -> C.Red.copy(alpha = 0.55f)
                        else -> C.Card
                    },
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
            if (answered) {
                AppCard(Modifier.fillMaxWidth(), border = if (right) C.Green else C.Red) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            if (right) tx(tr, "✅ Correct!", "✅ Doğru!") else tx(tr, "❌ Wrong", "❌ Yanlış"),
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(vq.w(tr), color = Color(0xFFCBD5E1))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            tx(tr, "Replay the clip above with the VAR tools.", "Yukarıdaki pozisyonu VAR araçlarıyla tekrar inceleyebilirsin."),
                            color = C.Muted, fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        if (answered) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                if (isLast) tx(tr, "Finish quiz →", "Quizi bitir →") else tx(tr, "Next →", "İleri →"),
                { next() }
            )
        }
    }
}
