package com.armeaze.duduksende

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =====================================================================
//  GELİŞİM EKRANI (v1.4): konu bazlı başarı, zayıf konu önerisi, Yanlışlarım, quiz geçmişi
// =====================================================================

private fun kindName(tr: Boolean, k: String): String = when (k) {
    "CATEGORY" -> tx(tr, "Category", "Kategori")
    "DAILY" -> tx(tr, "Daily challenge", "Günün görevi")
    "PRACTICE" -> tx(tr, "Practice", "Antrenman")
    "TIME" -> tx(tr, "Time Attack", "Zamana Karşı")
    "SUDDEN" -> tx(tr, "Sudden Death", "Ani Ölüm")
    "DUEL" -> tx(tr, "Duel", "Düello")
    "VIDEO" -> "Video"
    else -> k
}

@Composable
fun ProgressScreen(tr: Boolean, store: Store, onBack: () -> Unit, onSpec: (QuizSpec) -> Unit) {
    val stats = (0 until Cats.count()).map { store.catStats(it) }
    val weakest = stats.indices.filter { stats[it][1] >= 5 }
        .minByOrNull { stats[it][0] * 100 / stats[it][1] }
    val wrong = store.wrongIds()
    val answered = store.int("answered")
    val acc = if (answered > 0) store.int("correct") * 100 / answered else 0
    val hist = store.history()

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackBtn(onBack)
            Spacer(Modifier.width(12.dp))
            Text("📈 " + tx(tr, "My progress", "Gelişimim"), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("🎯", "$acc%", tx(tr, "Accuracy", "Başarı"), Modifier.weight(1f))
                StatCard("🔥", store.streak().toString(), tx(tr, "Day streak", "Günlük seri"), Modifier.weight(1f))
                StatCard("❓", answered.toString(), tx(tr, "Answered", "Cevap"), Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
            Text(tx(tr, "Accuracy by topic", "Konulara göre başarı"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            for (c in 0 until Cats.count()) {
                val a = stats[c][1]
                val pct = if (a > 0) stats[c][0] * 100 / a else 0
                val col = when {
                    a == 0 -> C.Muted
                    pct >= 75 -> C.Green
                    pct >= 50 -> C.Yellow
                    else -> C.Red
                }
                AppCard(Modifier.fillMaxWidth().padding(bottom = 8.dp), border = if (c == weakest) C.Red else C.Border) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(Cats.emoji[c] + "  " + Cats.name(tr, c), fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(
                                if (a > 0) "$pct%  (${stats[c][0]}/$a)" else tx(tr, "not played", "oynanmadı"),
                                color = col, fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Bar(pct / 100f, col)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (weakest != null) {
                val wp = stats[weakest][0] * 100 / stats[weakest][1]
                AppCard(Modifier.fillMaxWidth(), border = C.Red) {
                    Column(Modifier.padding(16.dp)) {
                        Text(tx(tr, "Your weakest topic", "En zayıf konun"), color = C.Muted, fontSize = 12.sp)
                        Text(
                            Cats.emoji[weakest] + " " + Cats.name(tr, weakest) + "  ($wp%)",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))
                        PrimaryButton("🎯 " + tx(tr, "Train this topic", "Bu konuda antrenman yap"), {
                            val ids = Questions.all.filter { it.cat == weakest }.shuffled().take(10).map { it.id }
                            onSpec(QuizSpec(Kind.PRACTICE, ids = ids))
                        })
                    }
                }
            } else {
                Text(
                    tx(tr, "Answer at least 5 questions in a topic to see your weakest topic.",
                        "En zayıf konunu görmek için bir konuda en az 5 soru cevapla."),
                    color = C.Muted, fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                "❌ " + tx(tr, "My mistakes", "Yanlışlarım") + " (${wrong.size})",
                { onSpec(QuizSpec(Kind.PRACTICE, ids = wrong.take(15))) },
                enabled = wrong.isNotEmpty()
            )
            if (wrong.isEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    tx(tr, "Questions you answer wrong are collected here until you get them right.",
                        "Yanlış cevapladığın sorular doğru bilene kadar burada toplanır."),
                    color = C.Muted, fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(tx(tr, "Recent quizzes", "Son quizler"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (hist.isEmpty()) {
                Text(tx(tr, "No quiz played yet.", "Henüz quiz oynanmadı."), color = C.Muted)
            }
            for (h in hist) {
                val p = if (h.answered > 0) h.correct * 100 / h.answered else 0
                AppCard(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(kindName(tr, h.kind), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(h.day, color = C.Muted, fontSize = 12.sp)
                        }
                        Text("${h.correct}/${h.answered}  •  $p%", color = C.Yellow, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// =====================================================================
//  QUIZ OLUŞTURUCU (v1.4): kategori + zorluk + soru sayısı seç, kendi quizini kur
// =====================================================================

@Composable
private fun OptChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    AppCard(
        modifier,
        border = if (selected) C.Yellow else C.Border,
        container = if (selected) Color(0xFF2A3350) else C.Card,
        onClick = onClick
    ) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) C.Yellow else Color.White
        )
    }
}

@Composable
fun CreatorScreen(tr: Boolean, onBack: () -> Unit, onSpec: (QuizSpec) -> Unit) {
    var cats by remember { mutableStateOf(setOf<Int>()) }
    var diff by remember { mutableStateOf(0) }
    var count by remember { mutableStateOf(10) }
    val avail = Questions.all.count { (cats.isEmpty() || it.cat in cats) && (diff == 0 || it.diff == diff) }
    val diffLabels = listOf(
        tx(tr, "Any", "Hepsi"), Levels.name(tr, 1), Levels.name(tr, 2), Levels.name(tr, 3)
    )

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackBtn(onBack)
            Spacer(Modifier.width(12.dp))
            Text("🛠 " + tx(tr, "Quiz creator", "Quiz oluşturucu"), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(tx(tr, "Topics (leave empty for all)", "Konular (boş bırakırsan hepsi)"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            for (pair in (0 until Cats.count()).chunked(2)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (c in pair) {
                        OptChip(Cats.emoji[c] + " " + Cats.name(tr, c), c in cats, Modifier.weight(1f)) {
                            cats = if (c in cats) cats - c else cats + c
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(tx(tr, "Difficulty", "Zorluk"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (d in 0..3) {
                    OptChip(diffLabels[d], diff == d, Modifier.weight(1f)) { diff = d }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(tx(tr, "Number of questions", "Soru sayısı"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in listOf(5, 10, 20)) {
                    OptChip(n.toString(), count == n, Modifier.weight(1f)) { count = n }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                tx(tr, "Matching questions: $avail", "Uygun soru sayısı: $avail"),
                color = C.Muted, fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton(
            "▶ " + tx(tr, "Start quiz", "Quizi başlat"),
            {
                val ids = Questions.all
                    .filter { (cats.isEmpty() || it.cat in cats) && (diff == 0 || it.diff == diff) }
                    .shuffled().take(count).map { it.id }
                onSpec(QuizSpec(Kind.PRACTICE, ids = ids))
            },
            enabled = avail > 0
        )
    }
}
