package com.armeaze.duduksende

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Onboarding ----------

class OnbPage(val emoji: String, val tEn: String, val tTr: String, val dEn: String, val dTr: String)

@Composable
fun OnboardingScreen(tr: Boolean, onLang: (String) -> Unit, onDone: () -> Unit) {
    val pages = listOf(
        OnbPage("📣", "Master the Laws of Football", "Futbol Kurallarına Hakim Ol",
            "Learn the rules, solve match situations and decide like a referee.",
            "Kuralları öğren, maç pozisyonlarını çöz ve hakem gibi karar ver."),
        OnbPage("📅", "Play Daily. Learn Faster.", "Her Gün Oyna. Daha Hızlı Öğren.",
            "Daily challenges, streaks and XP keep you improving.",
            "Günlük görevler, seriler ve XP ile sürekli gelişirsin."),
        OnbPage("🏆", "Climb the Ranks", "Rütbeleri Tırman",
            "Earn XP, unlock harder levels and become an elite referee.",
            "XP kazan, zor seviyeleri aç ve elit hakem ol.")
    )
    var page by remember { mutableStateOf(0) }
    val pg = pages[page]
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { LangSwitch(tr, onLang) }
        Spacer(Modifier.weight(1f))
        Text(pg.emoji, fontSize = 96.sp)
        Spacer(Modifier.height(20.dp))
        Text(tx(tr, pg.tEn, pg.tTr), fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(tx(tr, pg.dEn, pg.dTr), fontSize = 16.sp, color = C.Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Row(Modifier.padding(bottom = 16.dp)) {
            for (i in pages.indices) {
                Box(
                    Modifier.padding(4.dp).size(if (i == page) 24.dp else 10.dp, 10.dp)
                        .clip(RoundedCornerShape(5.dp)).background(if (i == page) C.Yellow else C.Border)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { if (page == 0) onDone() else page -= 1 },
                modifier = Modifier.weight(1f).height(54.dp)
            ) { Text(if (page == 0) tx(tr, "Skip", "Geç") else tx(tr, "Back", "Geri")) }
            Button(
                onClick = { if (page == pages.size - 1) onDone() else page += 1 },
                modifier = Modifier.weight(2f).height(54.dp)
            ) {
                Text(
                    if (page == pages.size - 1) tx(tr, "Start", "Başla") else tx(tr, "Next →", "İleri →"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------- Main (tabs) ----------

@Composable
fun MainScreen(
    tr: Boolean, store: Store, services: AppServices,
    tab: Int, onTab: (Int) -> Unit, onLang: (String) -> Unit,
    onSpec: (QuizSpec) -> Unit, onCat: (Int) -> Unit, onDuel: () -> Unit,
    onCreator: () -> Unit, onRules: () -> Unit, onProgress: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> HomeTab(tr, store, onLang, { onSpec(QuizSpec(Kind.DAILY)) }, onCat, onDuel, { onSpec(QuizSpec(Kind.VIDEO)) })
                1 -> QuizTab(tr, store, onSpec, onCat, onDuel, onCreator, onRules, onProgress)
                2 -> RankTab(tr, store, services)
                else -> ProfileTab(tr, store, onLang, onProgress)
            }
        }
        val items = listOf(
            "🏠" to tx(tr, "Home", "Ana sayfa"),
            "❓" to tx(tr, "Quiz", "Quiz"),
            "🏆" to tx(tr, "Ranking", "Sıralama"),
            "👤" to tx(tr, "Profile", "Profil")
        )
        NavigationBar(containerColor = C.Card) {
            items.forEachIndexed { i, item ->
                NavigationBarItem(
                    selected = tab == i,
                    onClick = { onTab(i) },
                    icon = { Text(item.first, fontSize = 22.sp) },
                    label = { Text(item.second) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = C.Yellow,
                        unselectedTextColor = C.Muted,
                        indicatorColor = Color(0xFF2A3350)
                    )
                )
            }
        }
    }
}

@Composable
fun DailyCard(tr: Boolean, done: Boolean, onDaily: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), border = C.Yellow) {
        Column(Modifier.padding(16.dp)) {
            Tag(tx(tr, "TODAY'S CHALLENGE", "GÜNÜN GÖREVİ"))
            Spacer(Modifier.height(8.dp))
            Text(tx(tr, "Today's decision", "Bugünün kararı"), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(Questions.daily().first().q(tr), color = C.Muted, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(tx(tr, "5 questions • +100 XP • +20 🪙", "5 soru • +100 XP • +20 🪙"), color = C.Yellow, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                if (done) tx(tr, "Completed ✓ Come back tomorrow", "Tamamlandı ✓ Yarın tekrar gel")
                else tx(tr, "Start challenge →", "Göreve başla →"),
                onDaily, enabled = !done
            )
        }
    }
}

@Composable
fun CatCard(tr: Boolean, c: Int, modifier: Modifier, onClick: () -> Unit) {
    AppCard(modifier.height(138.dp), onClick = onClick) {
        Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            CategoryArt(c, Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(10.dp)))
            Column {
                Text(Cats.name(tr, c), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("${Questions.count(c)} " + tx(tr, "questions", "soru"), fontSize = 13.sp, color = C.Muted)
            }
        }
    }
}

@Composable
fun CategoryGrid(tr: Boolean, onCat: (Int) -> Unit) {
    for (row in (0 until Cats.count()).toList().chunked(2)) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (c in row) CatCard(tr, c, Modifier.weight(1f)) { onCat(c) }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun HomeTab(tr: Boolean, store: Store, onLang: (String) -> Unit, onDaily: () -> Unit, onCat: (Int) -> Unit, onDuel: () -> Unit, onVideo: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Düdük Sende", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Yellow)
            LangSwitch(tr, onLang)
        }
        Spacer(Modifier.height(16.dp))
        Text(tx(tr, "Ready to make the call?", "Karar vermeye hazır mısın?"), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            tx(tr, "Train with real match situations based on the IFAB Laws.", "IFAB kurallarına dayalı maç durumlarıyla çalış."),
            color = C.Muted
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("⚡", store.int("xp").toString(), "XP", Modifier.weight(1f))
            StatCard("🔥", store.streak().toString(), tx(tr, "Day streak", "Günlük seri"), Modifier.weight(1f))
            StatCard("🪙", store.int("coins").toString(), tx(tr, "Coins", "Coin"), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        DailyCard(tr, store.dailyDone(), onDaily)
        Spacer(Modifier.height(12.dp))
        ModeCard("⚔️", tx(tr, "Duel", "Düello"), tx(tr, "Same 5 questions – everyone answers in their own language", "Aynı 5 soru – herkes kendi dilinde cevaplar"), onDuel)
        ModeCard("📺", tx(tr, "Video quiz", "Video quiz"), tx(tr, "Watch the situation, make the call – ask VAR when unsure", "Pozisyonu izle, kararı ver – emin değilsen VAR'a sor"), onVideo)
        Spacer(Modifier.height(8.dp))
        Text(tx(tr, "Training categories", "Antrenman kategorileri"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        CategoryGrid(tr, onCat)
        Spacer(Modifier.height(8.dp))
        Text("© 2026 Armeaze Company", color = C.Muted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
fun ModeCard(emoji: String, title: String, desc: String, onClick: () -> Unit) {
    AppCard(Modifier.fillMaxWidth().padding(bottom = 10.dp), onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = C.Muted, fontSize = 13.sp)
            }
            Text("›", fontSize = 26.sp, color = C.Yellow)
        }
    }
}

@Composable
fun QuizTab(
    tr: Boolean, store: Store, onSpec: (QuizSpec) -> Unit, onCat: (Int) -> Unit, onDuel: () -> Unit,
    onCreator: () -> Unit, onRules: () -> Unit, onProgress: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(tx(tr, "Choose a quiz", "Bir quiz seç"), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(tx(tr, "Pick a match situation and train like a referee.", "Bir maç durumu seç, hakem gibi çalış."), color = C.Muted)
        Spacer(Modifier.height(16.dp))
        DailyCard(tr, store.dailyDone()) { onSpec(QuizSpec(Kind.DAILY)) }
        Spacer(Modifier.height(20.dp))
        Text(tx(tr, "Question categories", "Soru kategorileri"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        CategoryGrid(tr, onCat)
        Spacer(Modifier.height(10.dp))
        Text(tx(tr, "More modes", "Diğer modlar"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        ModeCard("📖", tx(tr, "Rule book", "Kural kitabı"), tx(tr, "The 17 Laws summarised and searchable", "17 kuralın özeti, aranabilir"), onRules)
        ModeCard("🛠", tx(tr, "Quiz creator", "Quiz oluşturucu"), tx(tr, "Pick topics, difficulty and length", "Konu, zorluk ve soru sayısını sen seç"), onCreator)
        ModeCard("📈", tx(tr, "My progress", "Gelişimim"), tx(tr, "Topic accuracy, weak spots, my mistakes", "Konu başarısı, zayıf noktalar, yanlışlarım"), onProgress)
        ModeCard("⚔️", tx(tr, "Duel", "Düello"), tx(tr, "Same 5 questions, each player in their own language", "Aynı 5 soru, her oyuncu kendi dilinde"), onDuel)
        ModeCard("📺", tx(tr, "Video quiz", "Video quiz"), tx(tr, "5 animated situations with multi-angle VAR review", "5 animasyonlu pozisyon, çok açılı VAR incelemesi")) {
            onSpec(QuizSpec(Kind.VIDEO))
        }
        ModeCard("🎯", tx(tr, "Practice", "Antrenman"), tx(tr, "10 random questions from all categories", "Tüm kategorilerden 10 rastgele soru")) {
            onSpec(QuizSpec(Kind.PRACTICE))
        }
        ModeCard("⏱", tx(tr, "Time Attack", "Zamana Karşı"), tx(tr, "Answer as many as you can in 60 seconds", "60 saniyede olabildiğince çok cevapla")) {
            onSpec(QuizSpec(Kind.TIME))
        }
        ModeCard("💀", tx(tr, "Sudden Death", "Ani Ölüm"), tx(tr, "One wrong answer ends the run", "Tek yanlışta oyun biter")) {
            onSpec(QuizSpec(Kind.SUDDEN))
        }
    }
}

// ---------- Ranking ----------

@Composable
fun RankTab(tr: Boolean, store: Store, services: AppServices) {
    var period by remember { mutableStateOf(Period.WEEKLY) }
    var rows by remember { mutableStateOf<List<LbRow>>(emptyList()) }
    LaunchedEffect(period) {
        rows = try { services.leaderboard.top(period) } catch (e: Exception) { emptyList() }
    }
    val labels = listOf(
        Period.DAILY to tx(tr, "Daily", "Günlük"),
        Period.WEEKLY to tx(tr, "Weekly", "Haftalık"),
        Period.MONTHLY to tx(tr, "Monthly", "Aylık"),
        Period.ALL to tx(tr, "All", "Tümü")
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("🏆 " + tx(tr, "Leaderboard", "Sıralama"), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(tx(tr, "Compete by XP earned in each period", "Her dönemde kazanılan XP ile yarış"), color = C.Muted)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for ((p, label) in labels) {
                val m = Modifier.weight(1f).height(40.dp)
                if (p == period) Button(onClick = { period = p }, modifier = m, contentPadding = PaddingValues(horizontal = 2.dp)) { Text(label, fontSize = 13.sp) }
                else OutlinedButton(onClick = { period = p }, modifier = m, contentPadding = PaddingValues(horizontal = 2.dp)) { Text(label, fontSize = 13.sp) }
            }
        }
        Spacer(Modifier.height(14.dp))
        if (!services.leaderboard.isOnline) {
            AppCard(Modifier.fillMaxWidth()) {
                Text(
                    tx(tr, "Showing your results on this device. Online ranking against other players is coming later.",
                        "Bu cihazdaki sonuçların gösteriliyor. Diğer oyuncularla online sıralama daha sonra eklenecek."),
                    modifier = Modifier.padding(14.dp), color = C.Muted, fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(10.dp))
        }
        rows.forEachIndexed { i, r ->
            AppCard(Modifier.fillMaxWidth().padding(bottom = 10.dp), border = if (r.isMe) C.Yellow else C.Border) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#${i + 1}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C.Yellow)
                    Spacer(Modifier.width(14.dp))
                    Text("👤", fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${r.quizzes} " + tx(tr, "quizzes", "quiz") + " • ${r.accuracy}% " + tx(tr, "accuracy", "başarı"),
                            fontSize = 12.sp, color = C.Muted
                        )
                    }
                    Text("${r.xp} XP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = C.Yellow)
                }
            }
        }
    }
}

// ---------- Profile ----------

@Composable
fun ProfileTab(tr: Boolean, store: Store, onLang: (String) -> Unit, onProgress: () -> Unit) {
    var name by remember { mutableStateOf(store.name) }
    var snd by remember { mutableStateOf(store.soundOn) }
    var vib by remember { mutableStateOf(store.vibOn) }
    val xp = store.int("xp")
    val r = Ranks.index(xp)
    val nextNeed = if (r < Ranks.need.size - 1) Ranks.need[r + 1] else null
    val frac = if (nextNeed != null) (xp - Ranks.need[r]).toFloat() / (nextNeed - Ranks.need[r]) else 1f
    val answered = store.int("answered")
    val acc = if (answered > 0) store.int("correct") * 100 / answered else 0

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("👤 " + tx(tr, "Profile", "Profil"), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 20) { name = it; store.name = it } },
            singleLine = true,
            label = { Text(tx(tr, "Display name", "Görünen ad")) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        AppCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(Ranks.name(tr, r), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = C.Yellow)
                Spacer(Modifier.height(8.dp))
                Bar(frac, C.Yellow)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (nextNeed != null) "$xp / $nextNeed XP" else "$xp XP",
                    fontSize = 13.sp, color = C.Muted
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        AppCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                StatRow("⚡ XP", xp.toString())
                StatRow("🔥 " + tx(tr, "Day streak", "Günlük seri"), store.streak().toString())
                StatRow("🪙 " + tx(tr, "Coins", "Coin"), store.int("coins").toString())
                StatRow(tx(tr, "Games played", "Oynanan oyun"), store.int("played").toString())
                StatRow(tx(tr, "Questions answered", "Cevaplanan soru"), answered.toString())
                StatRow(tx(tr, "Accuracy", "Başarı oranı"), "$acc%")
                StatRow("⏱ " + tx(tr, "Best Time Attack", "En iyi Zamana Karşı"), store.int("best_time").toString())
                StatRow("💀 " + tx(tr, "Best Sudden Death", "En iyi Ani Ölüm"), store.int("best_sudden").toString())
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton("📈 " + tx(tr, "My progress", "Gelişimim"), onProgress)
        Spacer(Modifier.height(14.dp))
        AppCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🔊 " + tx(tr, "Sound effects", "Ses efektleri"), fontSize = 16.sp)
                    Switch(checked = snd, onCheckedChange = { snd = it; store.soundOn = it })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("📳 " + tx(tr, "Vibration", "Titreşim"), fontSize = 16.sp)
                    Switch(checked = vib, onCheckedChange = { vib = it; store.vibOn = it })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(tx(tr, "Language", "Dil"), fontSize = 16.sp)
                    LangSwitch(tr, onLang)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Düdük Sende v1.4", color = C.Muted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("© 2026 Armeaze Company", color = C.Muted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

// ---------- Levels ----------

@Composable
fun LevelsScreen(
    tr: Boolean, store: Store, services: AppServices, cat: Int,
    onBack: () -> Unit, onPlay: (Int) -> Unit
) {
    var tick by remember { mutableStateOf(0) }
    val refresh = tick
    val xp = store.int("xp")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackBtn(onBack)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("${Cats.emoji[cat]} ${Cats.name(tr, cat)}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(tx(tr, "Choose a training level", "Bir seviye seç"), color = C.Muted)
            }
        }
        Spacer(Modifier.height(16.dp))
        for (l in 1..5) {
            val unlocked = store.levelUnlocked(cat, l)
            val best = store.int("best_${cat}_$l")
            val sec = Levels.SECONDS[l - 1]
            val desc = tx(tr, "5 questions", "5 soru") +
                (if (sec > 0) " • ${sec} " + tx(tr, "sec/question", "sn/soru") else "")
            AppCard(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                onClick = if (unlocked) ({ onPlay(l) }) else null
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${Levels.icons[l - 1]} ${Levels.name(tr, l)}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(if (unlocked) "+${Levels.XP[l - 1]} XP/✔" else "🔒", color = C.Yellow, fontWeight = FontWeight.Bold)
                    }
                    Text(desc, color = C.Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    if (unlocked) {
                        Bar(best / 5f, C.Green)
                        Text(tx(tr, "Best: ", "En iyi: ") + "$best/5", fontSize = 12.sp, color = C.Muted)
                    } else {
                        Text(
                            tx(tr, "Reach ${Levels.NEED[l - 1]} XP to unlock (you have $xp)",
                                "Açmak için ${Levels.NEED[l - 1]} XP'ye ulaş (şu an $xp)"),
                            fontSize = 13.sp, color = C.Muted
                        )
                        val cost = Levels.COINS[l - 1]
                        Spacer(Modifier.height(8.dp))
                        if (store.int("coins") >= cost) {
                            Button(onClick = {
                                store.add("coins", -cost)
                                store.put("unlock_${cat}_$l", 1)
                                tick += 1
                            }) { Text("🪙 " + tx(tr, "Unlock for $cost coins", "$cost coin ile aç")) }
                        } else {
                            Text(tx(tr, "or unlock with $cost 🪙 coins", "veya $cost 🪙 coin ile aç"), fontSize = 13.sp, color = C.Muted)
                        }
                        if (services.ads.available) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                services.ads.showRewarded { ok ->
                                    if (ok) {
                                        store.put("unlock_${cat}_$l", 1)
                                        tick += 1
                                    }
                                }
                            }) { Text("📺 " + tx(tr, "Watch ad to unlock", "Reklam izle ve aç")) }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Result ----------

@Composable
fun ResultScreen(
    tr: Boolean, store: Store, spec: QuizSpec, correct: Int, answered: Int, rw: Rewards,
    canNext: Boolean, onAgain: () -> Unit, onNext: () -> Unit, onBack: () -> Unit
) {
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(if (spec.kind == Kind.CATEGORY && !rw.passed) "🟥" else "🏁", fontSize = 64.sp)
        Text(tx(tr, "Full time!", "Maç bitti!"), fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("$correct / $answered", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = C.Yellow)
        if (spec.kind == Kind.CATEGORY) {
            Text(
                if (rw.passed) tx(tr, "Level cleared! 🎉", "Seviye geçildi! 🎉")
                else tx(tr, "Score 3 or more to clear the level.", "Seviyeyi geçmek için en az 3 doğru yap."),
                color = C.Muted
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("⚡", "+${rw.xp}", "XP", Modifier.weight(1f))
            StatCard("🪙", "+${rw.coins}", tx(tr, "Coins", "Coin"), Modifier.weight(1f))
            StatCard("🔥", store.streak().toString(), tx(tr, "Day streak", "Günlük seri"), Modifier.weight(1f))
        }
        if (rw.dailyBonus) {
            Spacer(Modifier.height(10.dp))
            Text(tx(tr, "Daily challenge bonus earned! 🎁", "Günlük görev bonusu kazanıldı! 🎁"), color = C.Yellow, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        if (spec.kind != Kind.DAILY) {
            PrimaryButton(tx(tr, "Play again", "Tekrar oyna"), onAgain)
            Spacer(Modifier.height(10.dp))
        }
        if (canNext) {
            PrimaryButton(tx(tr, "Next level →", "Sonraki seviye →"), onNext)
            Spacer(Modifier.height(10.dp))
        }
        OutlinedButton(
            onClick = {
                shareText(
                    ctx,
                    tx(tr, "I scored $correct/$answered in Düdük Sende! ⚽🟨 Can you beat me? – Armeaze Company",
                        "Düdük Sende'de $correct/$answered yaptım! ⚽🟨 Sen yapabilir misin? – Armeaze Company")
                )
            },
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text("📤 " + tx(tr, "Share", "Paylaş")) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text(
                if (spec.kind == Kind.CATEGORY) tx(tr, "Back to levels", "Seviyelere dön") else tx(tr, "Home", "Ana menü")
            )
        }
    }
}

// ---------- Duel ----------

fun flag(lang: String): String = if (lang == "tr") "🇹🇷" else "🇬🇧"
fun langName(tr: Boolean, lang: String): String =
    if (lang == "tr") tx(tr, "Turkish", "Türkçe") else tx(tr, "English", "İngilizce")

@Composable
fun DuelLobbyScreen(tr: Boolean, store: Store, services: AppServices, onBack: () -> Unit, onStart: (DuelMatch) -> Unit) {
    var match by remember { mutableStateOf<DuelMatch?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    LaunchedEffect(attempt) {
        match = null
        failed = false
        try {
            match = services.duel.findMatch(store.name, if (tr) "tr" else "en")
        } catch (e: Exception) {
            failed = true
        }
    }
    val m = match
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackBtn(onBack)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("⚔️ " + tx(tr, "Duel", "Düello"), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(tx(tr, "5 questions • 15 sec each", "5 soru • soru başına 15 sn"), color = C.Muted)
            }
        }
        Spacer(Modifier.height(16.dp))
        AppCard(Modifier.fillMaxWidth()) {
            Text(
                tx(tr, "Both players get the same questions. Each one reads them in their own language – a Turkish and an English player can face each other fairly.",
                    "İki oyuncu da aynı soruları alır. Herkes soruyu kendi dilinde okur – Türk ve İngiliz oyuncular adil şekilde karşılaşabilir."),
                modifier = Modifier.padding(14.dp), color = C.Muted, fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        if (m == null) {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (failed) "⚠️" else "🔎", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (failed) tx(tr, "Could not find an opponent.", "Rakip bulunamadı.")
                        else tx(tr, "Searching for an opponent…", "Rakip aranıyor…"),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                    if (failed) {
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton(tx(tr, "Try again", "Tekrar dene"), { attempt += 1 })
                    }
                }
            }
        } else {
            AppCard(Modifier.fillMaxWidth(), border = C.Yellow) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(flag(if (tr) "tr" else "en"), fontSize = 40.sp)
                            Text(store.name, fontWeight = FontWeight.Bold)
                            Text(langName(tr, if (tr) "tr" else "en"), color = C.Muted, fontSize = 12.sp)
                        }
                        Text("VS", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = C.Yellow)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(flag(m.opponentLang), fontSize = 40.sp)
                            Text(m.opponentName + if (!services.duel.isOnline) " 🤖" else "", fontWeight = FontWeight.Bold)
                            Text(langName(tr, m.opponentLang), color = C.Muted, fontSize = 12.sp)
                        }
                    }
                    if (m.opponentLang != (if (tr) "tr" else "en")) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            tx(tr, "Your opponent plays the same questions in ${langName(false, m.opponentLang)}.",
                                "Rakibin aynı soruları ${langName(true, m.opponentLang)} oynuyor."),
                            color = C.Muted, fontSize = 13.sp, textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (!services.duel.isOnline) {
                Spacer(Modifier.height(10.dp))
                Text(
                    tx(tr, "Online matchmaking is coming soon. For now you play against a practice bot (🤖).",
                        "Online eşleşme yakında geliyor. Şimdilik antrenman botuyla (🤖) oynuyorsun."),
                    color = C.Muted, fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(tx(tr, "Start duel ⚔️", "Düelloyu başlat ⚔️"), { onStart(m) })
        }
    }
}

@Composable
fun DuelResultScreen(
    tr: Boolean, store: Store, match: DuelMatch, my: Int, rw: Rewards,
    onAgain: () -> Unit, onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val opp = match.opponentScore ?: 0
    val result = if (my > opp) 1 else if (my == opp) 0 else -1
    LaunchedEffect(Unit) { if (result == 1) Sfx.win() }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(if (result == 1) "🏆" else if (result == 0) "🤝" else "😅", fontSize = 64.sp)
        Text(
            if (result == 1) tx(tr, "You won!", "Kazandın!") else if (result == 0) tx(tr, "Draw!", "Berabere!") else tx(tr, "You lost", "Kaybettin"),
            fontSize = 30.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text("$my – $opp", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = C.Yellow)
        Text("${store.name} ${flag(if (tr) "tr" else "en")}  vs  ${match.opponentName} ${flag(match.opponentLang)}", color = C.Muted)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("⚡", "+${rw.xp}", "XP", Modifier.weight(1f))
            StatCard("🪙", "+${rw.coins}", tx(tr, "Coins", "Coin"), Modifier.weight(1f))
            StatCard("🔥", store.streak().toString(), tx(tr, "Day streak", "Günlük seri"), Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(tx(tr, "New duel", "Yeni düello"), onAgain)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                shareText(
                    ctx,
                    tx(tr, "I played a duel in Düdük Sende: $my–$opp! ⚔️⚽ – Armeaze Company",
                        "Düdük Sende'de düello oynadım: $my–$opp! ⚔️⚽ – Armeaze Company")
                )
            },
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text("📤 " + tx(tr, "Share", "Paylaş")) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(tx(tr, "Home", "Ana menü")) }
    }
}
