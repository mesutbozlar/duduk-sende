package com.armeaze.duduksende

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * KURAL KİTABI (v1.4)
 * IFAB Futbol Oyun Kuralları'nın 17 maddesinin kısa, aranabilir özeti.
 * Metinler özettir; resmî ve bağlayıcı metin IFAB Oyun Kuralları'dır.
 */
class Law(
    val no: Int, val emoji: String,
    val tEn: String, val tTr: String,
    val pEn: List<String>, val pTr: List<String>
) {
    fun title(tr: Boolean) = if (tr) tTr else tEn
    fun points(tr: Boolean) = if (tr) pTr else pEn
}

object Rules {
    /** Soru kategorisi -> ilgili kuralın dizin numarası (kural no - 1). -1 = yok. */
    fun forCat(cat: Int): Int = when (cat) {
        0 -> 10   // Ofsayt -> Kural 11
        1 -> 11   // Fauller -> Kural 12
        2 -> 12   // Yeniden başlatmalar -> Kural 13
        3 -> 13   // Penaltılar -> Kural 14
        4 -> 0    // Temel kurallar -> Kural 1
        else -> -1
    }

    val laws: List<Law> = listOf(
        Law(1, "🏟️", "The Field of Play", "Oyun Alanı",
            listOf(
                "A rectangle; touchlines are longer than goal lines. International matches: length 100–110 m, width 64–75 m.",
                "Goal: 7.32 m wide, 2.44 m high.",
                "Goal area extends 5.5 m, penalty area 16.5 m from the goalposts.",
                "Penalty mark is 11 m from the goal line; centre circle radius 9.15 m; corner arc radius 1 m.",
                "Lines belong to the area they enclose; the ball is in play if it touches a line."
            ),
            listOf(
                "Dikdörtgendir; yan çizgiler kale çizgilerinden uzundur. Uluslararası maçlarda boy 100–110 m, en 64–75 m.",
                "Kale: 7,32 m genişlik, 2,44 m yükseklik.",
                "Kale alanı direklerden 5,5 m, ceza sahası 16,5 m içeri uzanır.",
                "Penaltı noktası kale çizgisine 11 m; orta yuvarlak yarıçapı 9,15 m; köşe yayı yarıçapı 1 m.",
                "Çizgiler kapattıkları alanın parçasıdır; çizgiye değen top oyundadır."
            )),
        Law(2, "⚽", "The Ball", "Top",
            listOf(
                "Spherical, suitable material, circumference 68–70 cm.",
                "Weight 410–450 g at the start of the match; pressure 0.6–1.1 atmosphere.",
                "If the ball becomes defective, play is stopped and restarted with a dropped ball."
            ),
            listOf(
                "Küreseldir, uygun malzemedendir, çevresi 68–70 cm.",
                "Maç başında ağırlığı 410–450 g; basıncı 0,6–1,1 atmosfer.",
                "Top kusurlu hale gelirse oyun durdurulur ve hakem atışıyla yeniden başlar."
            )),
        Law(3, "👥", "The Players", "Oyuncular",
            listOf(
                "A match is played by two teams, each with no more than 11 players; one is the goalkeeper.",
                "A match cannot start or continue if a team has fewer than seven players.",
                "In official competitions a maximum of five substitutions may be made, in three windows plus half-time.",
                "A substituted player may not return (unless the competition rules allow it); a substitute enters at the halfway line during a stoppage."
            ),
            listOf(
                "Maç, her biri en fazla 11 oyunculu iki takım arasında oynanır; biri kalecidir.",
                "Bir takımda yedi oyuncudan az kalırsa maç başlayamaz veya devam edemez.",
                "Resmî müsabakalarda en fazla beş oyuncu değişikliği yapılır; üç fırsat ve devre arası.",
                "Oyundan çıkan oyuncu geri dönemez (müsabaka kuralları izin vermedikçe); yedek, oyun durmuşken orta çizgiden girer."
            )),
        Law(4, "👕", "The Players' Equipment", "Oyuncuların Ekipmanı",
            listOf(
                "Compulsory: shirt with sleeves, shorts, socks, shinguards and footwear.",
                "Shinguards must be covered by the socks and be suitable.",
                "Goalkeepers wear colours distinct from other players and the officials.",
                "Anything dangerous (jewellery etc.) is not allowed. Electronic communication equipment is not permitted for players."
            ),
            listOf(
                "Zorunlu: kollu forma, şort, çorap, tekmelik ve ayakkabı.",
                "Tekmelikler çorapla örtülmeli ve uygun olmalıdır.",
                "Kaleciler diğer oyunculardan ve hakemlerden ayırt edilebilir renkte giyinir.",
                "Tehlikeli her şey (takı vb.) yasaktır. Oyuncular için elektronik iletişim cihazı yasaktır."
            )),
        Law(5, "🧑‍⚖️", "The Referee", "Hakem",
            listOf(
                "The referee has full authority to enforce the Laws and decides to the best of their ability.",
                "Decisions on facts of play are final.",
                "A decision cannot be changed once play has restarted or the referee has ended the match.",
                "The referee applies advantage, cautions with a yellow card and sends off with a red card."
            ),
            listOf(
                "Hakem, kuralları uygulamak için tam yetkiye sahiptir ve en iyi bilgisiyle karar verir.",
                "Oyunla ilgili olgusal kararlar kesindir.",
                "Oyun yeniden başladıktan veya hakem maçı bitirdikten sonra karar değiştirilemez.",
                "Hakem avantaj uygular, sarı kartla uyarır, kırmızı kartla oyundan çıkarır."
            )),
        Law(6, "🚩", "The Other Match Officials", "Diğer Maç Görevlileri",
            listOf(
                "Assistant referees, fourth official, additional assistant referees and reserve assistant referee.",
                "VAR and AVAR support the referee only for clear and obvious errors in goals, penalties, direct red cards and mistaken identity.",
                "The referee always makes the final decision."
            ),
            listOf(
                "Yardımcı hakemler, dördüncü hakem, ek yardımcı hakemler ve yedek yardımcı hakem.",
                "VAR ve AVAR, hakeme yalnızca gol, penaltı, doğrudan kırmızı kart ve yanlış oyuncu durumlarında açık ve net hatalarda yardım eder.",
                "Nihai kararı her zaman hakem verir."
            )),
        Law(7, "⏱️", "The Duration of the Match", "Maçın Süresi",
            listOf(
                "Two equal halves of 45 minutes unless otherwise agreed.",
                "Half-time interval: players are entitled to a break of up to 15 minutes.",
                "Time lost (substitutions, injuries, discipline, delays, VAR checks, etc.) is added at the end of each half.",
                "A penalty kick awarded or to be retaken is completed even if time has expired."
            ),
            listOf(
                "Aksi kararlaştırılmadıkça 45 dakikalık iki eşit devre.",
                "Devre arası: oyuncuların en fazla 15 dakikalık dinlenme hakkı vardır.",
                "Kaybedilen süre (değişiklik, sakatlık, disiplin, oyalama, VAR incelemesi vb.) her devrenin sonuna eklenir.",
                "Verilen veya tekrarlanacak penaltı, süre dolmuş olsa bile tamamlanır."
            )),
        Law(8, "🔔", "The Start and Restart of Play", "Oyunun Başlaması ve Yeniden Başlaması",
            listOf(
                "Kick-off: after a coin toss; the ball must be kicked forward in the traditional sense and clearly move; a goal may be scored directly against the opponents.",
                "Kick-off starts each half, extra-time period and follows a goal.",
                "Dropped ball: used to restart play when the referee stopped it for a reason not covered elsewhere; a goal cannot be scored directly from it.",
                "Check the official 2026/27 text for the latest wording of the dropped ball."
            ),
            listOf(
                "Başlama vuruşu: yazı-tura sonrası; top ileri vurulmalı ve açıkça hareket etmeli; doğrudan rakip kaleye gol atılabilir.",
                "Başlama vuruşu her devreyi, uzatma devresini ve golden sonraki oyunu başlatır.",
                "Hakem atışı: oyun başka bir kuralda belirtilmeyen bir nedenle durdurulunca kullanılır; doğrudan gol sayılmaz.",
                "Hakem atışının en güncel ifadesi için resmî 2026/27 metnine bakınız."
            )),
        Law(9, "↔️", "The Ball In and Out of Play", "Topun Oyunda ve Oyun Dışında Olması",
            listOf(
                "Out of play: the whole ball has crossed the goal line or touchline (on the ground or in the air), or play has been stopped by the referee.",
                "In play at all other times, including when it rebounds off a match official or goalpost and stays on the field.",
                "The ball is in play when it touches a line."
            ),
            listOf(
                "Oyun dışı: topun tamamı kale çizgisini veya yan çizgiyi (yerden ya da havadan) geçmiştir veya hakem oyunu durdurmuştur.",
                "Diğer tüm durumlarda oyundadır; hakemden veya direkten dönüp sahada kalan top da oyundadır.",
                "Çizgiye değen top oyundadır."
            )),
        Law(10, "🏁", "Determining the Outcome of a Match", "Maçın Sonucunun Belirlenmesi",
            listOf(
                "A goal is scored when the whole ball passes over the goal line between the posts and under the crossbar, if no offence was committed by the scoring team.",
                "The team that scores more goals wins; otherwise the match is a draw.",
                "Penalty shoot-out: each team takes five kicks alternately; then sudden death. Only players on the field at the end of the match may take part."
            ),
            listOf(
                "Golcü takım ihlal yapmamışsa, topun tamamı direkler arasında ve üst direğin altında kale çizgisini geçince gol olur.",
                "Daha çok gol atan takım kazanır; yoksa berabere biter.",
                "Penaltı atışları: her takım sırayla beş atış yapar; eşitlik sürerse ani ölüm. Yalnızca maç bittiğinde sahada olan oyuncular katılır."
            )),
        Law(11, "🚩", "Offside", "Ofsayt",
            listOf(
                "A player is in an offside position if any part of the head, body or feet is in the opponents' half (excluding the halfway line) and nearer to the opponents' goal line than both the ball and the second-last opponent.",
                "Hands and arms are not considered; the position is judged when the ball is played or touched by a teammate.",
                "Being in an offside position is not an offence by itself; the player must become involved in active play.",
                "No offside offence directly from a goal kick, throw-in or corner kick.",
                "Punishment: indirect free kick where the offence occurred."
            ),
            listOf(
                "Başın, vücudun veya ayakların herhangi bir kısmı rakip yarı sahadaysa (orta çizgi hariç) ve rakip kale çizgisine hem topdan hem sondan ikinci rakipten daha yakınsa oyuncu ofsayt pozisyonundadır.",
                "El ve kollar sayılmaz; pozisyon, top takım arkadaşı tarafından oynandığı veya dokunulduğu anda değerlendirilir.",
                "Ofsayt pozisyonunda olmak tek başına ihlal değildir; oyuncunun aktif oyuna müdahil olması gerekir.",
                "Kale vuruşu, taç atışı ve köşe vuruşundan doğrudan ofsayt olmaz.",
                "Ceza: ihlalin olduğu yerden endirekt serbest vuruş."
            )),
        Law(12, "🟨", "Fouls and Misconduct", "Fauller ve Kural Dışı Davranışlar",
            listOf(
                "Direct free kick (or penalty inside own penalty area) for careless, reckless or excessive-force offences: charging, jumping at, kicking, pushing, striking, tackling, tripping, handball, holding, spitting.",
                "Indirect free kick: dangerous play, impeding without contact, dissent, preventing the goalkeeper from releasing the ball, and goalkeeper offences with the ball in the hands.",
                "Yellow card: e.g. unsporting behaviour, dissent, persistent offences, delaying the restart, tactical fouls that stop a promising attack.",
                "Red card: serious foul play, violent conduct, spitting, denying an obvious goal-scoring opportunity (DOGSO), offensive language, second yellow card."
            ),
            listOf(
                "Dikkatsiz, pervasız veya aşırı güç kullanan şu ihlallerde direkt serbest vuruş (kendi ceza sahasında penaltı): çarpma, üstüne atlama, tekme, itme, vurma, müdahale, düşürme, elle oynama, tutma, tükürme.",
                "Endirekt serbest vuruş: tehlikeli oyun, temassız engelleme, itiraz, kalecinin topu bırakmasını engelleme ve kalecinin topu elindeyken yaptığı ihlaller.",
                "Sarı kart: örneğin sportmenlik dışı davranış, itiraz, ısrarlı ihlal, yeniden başlatmayı geciktirme, umut verici bir atağı durduran taktik faul.",
                "Kırmızı kart: ağır faul, şiddet içeren davranış, tükürme, net gol fırsatını engelleme, aşağılayıcı sözler, ikinci sarı kart."
            )),
        Law(13, "🎯", "Free Kicks", "Serbest Vuruşlar",
            listOf(
                "Direct free kick: a goal can be scored directly against the opponents. Indirect free kick: the referee raises an arm; a goal counts only after the ball touches another player.",
                "The ball must be stationary; it is in play when kicked and clearly moves.",
                "Opponents must be at least 9.15 m (10 yards) from the ball until it is in play.",
                "A free kick kicked directly into the kicker's own goal is a corner kick for the opponents."
            ),
            listOf(
                "Direkt serbest vuruş: doğrudan rakip kaleye gol olabilir. Endirekt serbest vuruş: hakem kolunu kaldırır; gol, top başka bir oyuncuya değdikten sonra sayılır.",
                "Top hareketsiz olmalıdır; vurulup açıkça hareket edince oyuna girer.",
                "Top oyuna girene kadar rakipler en az 9,15 m (10 yarda) uzakta durmalıdır.",
                "Serbest vuruşun doğrudan vuruşu yapanın kendi kalesine girmesi rakip lehine köşe vuruşudur."
            )),
        Law(14, "🥅", "The Penalty Kick", "Penaltı Atışı",
            listOf(
                "The ball is placed on the penalty mark; the defending goalkeeper stays on the goal line between the posts, facing the kicker, with part of one foot touching or in line with the goal line until the ball is kicked.",
                "All other players stay outside the penalty area, behind the penalty mark and at least 9.15 m from it.",
                "The kicker must kick the ball forward; feinting after the run-up is complete is an offence.",
                "The kick is completed even if the half ends."
            ),
            listOf(
                "Top penaltı noktasına konur; savunan kaleci, atış yapılana kadar yüzü vurucuya dönük, ayağının bir kısmı kale çizgisine değerek veya hizasında direkler arasında durur.",
                "Diğer tüm oyuncular ceza sahası dışında, penaltı noktasının gerisinde ve noktadan en az 9,15 m uzakta bekler.",
                "Vurucu topu ileri vurmalıdır; koşu tamamlandıktan sonra yapılan aldatma hareketi ihlaldir.",
                "Devre bitse bile atış tamamlanır."
            )),
        Law(15, "🤾", "The Throw-in", "Taç Atışı",
            listOf(
                "Awarded to the opponents of the player who last touched the ball when the whole ball crosses the touchline.",
                "The thrower faces the field, has part of each foot on the touchline or on the ground outside it, and throws with both hands from behind and over the head.",
                "Opponents must stand at least 2 m from the point of the throw-in.",
                "No goal can be scored directly from a throw-in: into the opponents' goal = goal kick; into own goal = corner kick."
            ),
            listOf(
                "Topun tamamı yan çizgiyi geçtiğinde, topa son dokunan oyuncunun rakiplerine verilir.",
                "Atan oyuncu sahaya dönüktür, her ayağının bir kısmı yan çizgide veya dışındaki zeminde olur ve topu iki elle, başının arkasından ve üstünden atar.",
                "Rakipler atış noktasından en az 2 m uzakta durmalıdır.",
                "Taç atışından doğrudan gol olmaz: rakip kaleye girerse kale vuruşu, kendi kalesine girerse köşe vuruşu."
            )),
        Law(16, "🧤", "The Goal Kick", "Kale Vuruşu",
            listOf(
                "Awarded when the whole ball crosses the goal line (without a goal) after last being touched by an attacker.",
                "The ball is in play when it is kicked and clearly moves; it does not have to leave the penalty area.",
                "Opponents must stay outside the penalty area until the ball is in play.",
                "A goal can be scored directly against the opponents only."
            ),
            listOf(
                "Top, son olarak bir hücum oyuncusuna değdikten sonra tamamen kale çizgisini geçerse (gol olmadan) verilir.",
                "Top vurulup açıkça hareket edince oyuna girer; ceza sahasından çıkması gerekmez.",
                "Top oyuna girene kadar rakipler ceza sahası dışında kalmalıdır.",
                "Yalnızca rakip kaleye doğrudan gol olabilir."
            )),
        Law(17, "📐", "The Corner Kick", "Köşe Vuruşu",
            listOf(
                "Awarded when the whole ball crosses the goal line (without a goal) after last being touched by a defender.",
                "The ball is placed in the corner area nearest to where it went out; the corner flagpost must not be moved.",
                "Opponents must be at least 9.15 m from the corner arc until the ball is in play.",
                "A goal can be scored directly against the opponents only."
            ),
            listOf(
                "Top, son olarak bir savunma oyuncusuna değdikten sonra tamamen kale çizgisini geçerse (gol olmadan) verilir.",
                "Top, çıktığı yere en yakın köşe alanına konur; köşe bayrağı yerinden oynatılamaz.",
                "Top oyuna girene kadar rakipler köşe yayından en az 9,15 m uzakta olmalıdır.",
                "Yalnızca rakip kaleye doğrudan gol olabilir."
            ))
    )
}

@Composable
fun LawBody(tr: Boolean, law: Law) {
    Column {
        for (p in law.points(tr)) {
            Row(Modifier.padding(vertical = 3.dp)) {
                Text("•  ", color = C.Yellow, fontSize = 14.sp)
                Text(p, color = Color(0xFFCBD5E1), fontSize = 14.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RulesScreen(tr: Boolean, startLaw: Int, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var open by remember { mutableStateOf(startLaw) }
    val scroll = rememberScrollState()
    val density = LocalDensity.current.density
    LaunchedEffect(Unit) {
        if (startLaw > 0) scroll.scrollTo((startLaw * 76 * density).toInt())
    }
    val q = query.trim().lowercase()
    val shown = Rules.laws.filter { law ->
        q.isEmpty() || law.no.toString() == q ||
            law.title(tr).lowercase().contains(q) ||
            law.points(tr).any { it.lowercase().contains(q) }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackBtn(onBack)
            Spacer(Modifier.width(12.dp))
            Text("📖 " + tx(tr, "Rule book", "Kural kitabı"), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            label = { Text(tx(tr, "Search the laws (e.g. offside, 9.15)", "Kurallarda ara (örn. ofsayt, 9,15)")) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Column(Modifier.weight(1f).verticalScroll(scroll)) {
            for (law in shown) {
                val idx = law.no - 1
                val isOpen = open == idx || q.isNotEmpty()
                AppCard(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    border = if (isOpen) C.Yellow else C.Border,
                    onClick = { open = if (open == idx) -1 else idx }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(law.emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                tx(tr, "Law", "Kural") + " ${law.no} · " + law.title(tr),
                                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(if (isOpen) "▲" else "▼", color = C.Muted)
                        }
                        if (isOpen) {
                            Spacer(Modifier.height(8.dp))
                            LawBody(tr, law)
                        }
                    }
                }
            }
            if (shown.isEmpty()) {
                Text(tx(tr, "No law matches your search.", "Aramanla eşleşen kural yok."), color = C.Muted)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                tx(tr, "Short summaries. The official IFAB Laws of the Game are binding.",
                    "Kısa özetlerdir. Bağlayıcı olan resmî IFAB Oyun Kuralları'dır."),
                color = C.Muted, fontSize = 12.sp
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
