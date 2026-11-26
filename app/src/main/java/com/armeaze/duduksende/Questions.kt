package com.armeaze.duduksende

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.random.Random

class Q(
    val qEn: String, val qTr: String,
    val oEn: List<String>, val oTr: List<String>,
    val ans: Int,
    val wEn: String, val wTr: String
) {
    fun q(tr: Boolean) = if (tr) qTr else qEn
    fun o(tr: Boolean) = if (tr) oTr else oEn
    fun w(tr: Boolean) = if (tr) wTr else wEn

    var id: String = ""
    var cat: Int = 0
    var diff: Int = 1
    fun t(c: Int, d: Int): Q { cat = c; diff = d; return this }
}

object Questions {
    private val base: List<Q> = listOf(
        Q("How many minutes does each half last in a standard match?",
            "Standart bir maçta her devre kaç dakika sürer?",
            listOf("40", "45", "50", "60"), listOf("40", "45", "50", "60"), 1,
            "A match has two equal halves of 45 minutes unless agreed otherwise.",
            "Aksi kararlaştırılmadıkça maç, 45 dakikalık iki eşit devreden oluşur."),
        Q("What is the minimum number of players a team needs for a match to continue?",
            "Bir maçın devam edebilmesi için takımda en az kaç oyuncu olmalıdır?",
            listOf("6", "7", "8", "9"), listOf("6", "7", "8", "9"), 1,
            "A match cannot start or continue if either team has fewer than seven players.",
            "Bir takımda 7'den az oyuncu kalırsa maç başlayamaz veya devam edemez."),
        Q("What is the maximum length of the half-time interval?",
            "Devre arasının en fazla süresi nedir?",
            listOf("5 minutes", "10 minutes", "15 minutes", "20 minutes"),
            listOf("5 dakika", "10 dakika", "15 dakika", "20 dakika"), 2,
            "Players are entitled to an interval that must not exceed 15 minutes.",
            "Oyuncuların devre arası hakkı vardır ve bu süre 15 dakikayı geçemez."),
        Q("What is the circumference of a size 5 match ball?",
            "5 numara bir maç topunun çevresi ne kadardır?",
            listOf("58–60 cm", "68–70 cm", "75–78 cm", "80–82 cm"),
            listOf("58–60 cm", "68–70 cm", "75–78 cm", "80–82 cm"), 1,
            "The ball is spherical with a circumference of 68–70 cm.",
            "Top küresel olmalı ve çevresi 68–70 cm arasında olmalıdır."),
        Q("How wide is a standard goal between the posts?",
            "Standart bir kale direkler arasında kaç metre genişliğindedir?",
            listOf("6.32 m", "7.32 m", "8.32 m", "9.15 m"),
            listOf("6,32 m", "7,32 m", "8,32 m", "9,15 m"), 1,
            "The goal is 7.32 m wide and 2.44 m high.",
            "Kale 7,32 m genişliğinde ve 2,44 m yüksekliğindedir."),
        Q("How far is the penalty mark from the goal line?",
            "Penaltı noktası kale çizgisine kaç metre uzaklıktadır?",
            listOf("9.15 m", "10 m", "11 m", "12 m"),
            listOf("9,15 m", "10 m", "11 m", "12 m"), 2,
            "The penalty mark is 11 m (12 yards) from the midpoint between the goalposts.",
            "Penaltı noktası, direkler arasının ortasından 11 m uzaktadır."),
        Q("What is the minimum distance opponents must keep from a free kick?",
            "Serbest vuruşta rakipler topa en az kaç metre uzakta durmalıdır?",
            listOf("5 m", "7.32 m", "9.15 m", "11 m"),
            listOf("5 m", "7,32 m", "9,15 m", "11 m"), 2,
            "Opponents must be at least 9.15 m (10 yards) from the ball until it is in play.",
            "Top oyuna girene kadar rakipler en az 9,15 m uzakta olmalıdır."),
        Q("In which situation can a player NOT be penalised for offside?",
            "Hangi durumda bir oyuncu ofsayttan cezalandırılamaz?",
            listOf("Standing in their own half", "Standing near the goalkeeper",
                "Standing in front of the opponents' penalty area", "Receiving a long pass"),
            listOf("Kendi yarı sahasındayken", "Kalecinin yakınındayken",
                "Rakip ceza sahasının önündeyken", "Uzun pas alırken"), 0,
            "A player cannot be in an offside position in their own half of the field.",
            "Bir oyuncu kendi yarı sahasında ofsayt pozisyonunda olamaz."),
        Q("A player in an offside position receives the ball directly from a throw-in. Decision?",
            "Ofsayt pozisyonundaki oyuncu topu doğrudan taç atışından alıyor. Karar nedir?",
            listOf("Offside, indirect free kick", "No offence – offside doesn't apply",
                "Offside, direct free kick", "Throw-in is retaken"),
            listOf("Ofsayt, endirekt serbest vuruş", "İhlal yok – ofsayt uygulanmaz",
                "Ofsayt, direkt serbest vuruş", "Taç atışı tekrarlanır"), 1,
            "There is no offside offence directly from a goal kick, throw-in or corner kick.",
            "Kale vuruşu, taç atışı ve köşe vuruşundan doğrudan alınan toplarda ofsayt yoktur."),
        Q("When is a player in an offside position?",
            "Bir oyuncu ne zaman ofsayt pozisyonundadır?",
            listOf("Nearer to the opponents' goal line than both the ball and the second-last opponent",
                "Whenever they are in the opponents' half",
                "Whenever they are ahead of the ball",
                "When they are further from the goal line than the second-last defender"),
            listOf("Rakip kale çizgisine hem topdan hem de sondan ikinci rakipten daha yakınsa",
                "Rakip yarı sahadayken her zaman",
                "Topun önündeyken her zaman",
                "Sondan ikinci savunmacıdan kale çizgisine daha uzaksa"), 0,
            "Being in an offside position is not an offence by itself; the player must also become involved in active play.",
            "Ofsayt pozisyonunda olmak tek başına ihlal değildir; oyuncunun oyuna müdahil olması gerekir."),
        Q("A goalkeeper takes a goal kick and it goes straight into their own goal. Decision?",
            "Kaleci kale vuruşu yapıyor ve top doğrudan kendi kalesine giriyor. Karar nedir?",
            listOf("Goal", "Corner kick to the opponents", "Goal kick retaken", "Indirect free kick"),
            listOf("Gol", "Rakip lehine köşe vuruşu", "Kale vuruşu tekrarı", "Endirekt serbest vuruş"), 1,
            "If the ball from a goal kick enters the kicker's own goal, a corner kick is awarded to the opponents.",
            "Kale vuruşundan top doğrudan kendi kaleye girerse rakip takıma köşe vuruşu verilir."),
        Q("A player throws the ball in directly into the opponents' goal. Decision?",
            "Bir oyuncu taç atışını doğrudan rakip kaleye atıyor. Karar nedir?",
            listOf("Goal", "Goal kick", "Corner kick", "Throw-in retaken"),
            listOf("Gol", "Kale vuruşu", "Köşe vuruşu", "Taç atışı tekrarı"), 1,
            "A goal cannot be scored directly from a throw-in; a goal kick is awarded.",
            "Taç atışından doğrudan gol olmaz; kale vuruşu verilir."),
        Q("Can a goal be scored directly from a corner kick?",
            "Köşe vuruşundan doğrudan gol olur mu?",
            listOf("Yes, against the opposing team", "No, another player must touch it first",
                "Only if it touches the post", "Only in a penalty shoot-out"),
            listOf("Evet, rakip takıma karşı", "Hayır, önce başka bir oyuncu dokunmalı",
                "Sadece direğe değerse", "Sadece penaltı atışlarında"), 0,
            "A goal can be scored directly from a corner kick, but only against the opponents.",
            "Köşe vuruşundan doğrudan gol olur, ancak yalnızca rakip kaleye."),
        Q("At a kick-off, in which direction may the ball be played?",
            "Başlama vuruşunda top hangi yöne oynanabilir?",
            listOf("Forward only", "Any direction", "Backward only", "Sideways only"),
            listOf("Sadece ileri", "Herhangi bir yöne", "Sadece geri", "Sadece yana"), 1,
            "The ball may be kicked in any direction at kick-off; it must clearly move.",
            "Başlama vuruşunda top her yöne oynanabilir; topun belirgin şekilde hareket etmesi yeterlidir."),
        Q("What must the goalkeeper do when a penalty kick is taken?",
            "Penaltı atışı yapılırken kaleci ne yapmalıdır?",
            listOf("Keep at least part of one foot on or in line with the goal line",
                "Stand at least 1 m behind the goal line", "Stay in the centre of the goal",
                "Keep both feet fully on the line"),
            listOf("Ayağının en az bir kısmı kale çizgisinde veya çizgi hizasında olmalı",
                "Kale çizgisinin en az 1 m gerisinde durmalı", "Kalenin ortasında kalmalı",
                "İki ayağı da tamamen çizgide olmalı"), 0,
            "The goalkeeper must face the kicker and keep at least part of one foot touching, or in line with, the goal line.",
            "Kaleci vurucuya dönük olmalı ve en az bir ayağının bir kısmı kale çizgisine değmeli veya çizgi hizasında olmalıdır."),
        Q("Which offence is punished with a direct free kick?",
            "Aşağıdakilerden hangisi direkt serbest vuruşla cezalandırılır?",
            listOf("Dangerous play", "Impeding an opponent without contact",
                "Holding an opponent", "Offside"),
            listOf("Tehlikeli oyun", "Temas olmadan rakibi engelleme",
                "Rakibi tutmak", "Ofsayt"), 2,
            "Holding an opponent is a direct free kick offence. The others result in indirect free kicks.",
            "Rakibi tutmak direkt serbest vuruş gerektirir. Diğerleri endirekt serbest vuruştur."),
        Q("A teammate deliberately kicks the ball to the goalkeeper, who picks it up inside the box. Decision?",
            "Takım arkadaşı topu bilerek kaleciye vuruyor ve kaleci ceza sahasında elle alıyor. Karar nedir?",
            listOf("Penalty kick", "Indirect free kick to the opponents", "Direct free kick", "Play on"),
            listOf("Penaltı", "Rakip lehine endirekt serbest vuruş", "Direkt serbest vuruş", "Oyun devam"), 1,
            "Handling a ball deliberately kicked to the goalkeeper by a teammate gives an indirect free kick.",
            "Takım arkadaşının bilerek ayakla verdiği topu elle almak endirekt serbest vuruş gerektirir."),
        Q("A goalkeeper deliberately handles the ball outside their penalty area. Decision?",
            "Kaleci ceza sahası dışında topu bilerek elle oynuyor. Karar nedir?",
            listOf("Direct free kick to the opponents", "Indirect free kick", "Penalty kick", "Play on"),
            listOf("Rakip lehine direkt serbest vuruş", "Endirekt serbest vuruş", "Penaltı", "Oyun devam"), 0,
            "Outside the penalty area the goalkeeper is treated like any other player for handball.",
            "Ceza sahası dışında kaleci, elle oynama konusunda diğer oyuncularla aynı kurallara tabidir."),
        Q("A challenge is judged to be 'reckless'. What is the sanction?",
            "Bir müdahale 'pervasızca' (reckless) kabul edilirse yaptırım nedir?",
            listOf("No card", "Yellow card", "Red card", "Verbal warning only"),
            listOf("Kart yok", "Sarı kart", "Kırmızı kart", "Sadece sözlü uyarı"), 1,
            "Careless = no card, reckless = yellow card, excessive force = red card.",
            "Dikkatsiz = kart yok, pervasız = sarı kart, aşırı güç = kırmızı kart."),
        Q("Which of these is a sending-off (red card) offence?",
            "Aşağıdakilerden hangisi kırmızı kart gerektirir?",
            listOf("Dissent by word", "Spitting at an opponent", "Delaying a restart", "Persistent fouling"),
            listOf("Sözle itiraz", "Rakibe tükürmek", "Oyunu geciktirmek", "Sürekli faul yapmak"), 1,
            "Spitting at anyone is a red card offence. The others are cautionable (yellow).",
            "Herhangi birine tükürmek kırmızı kart gerektirir. Diğerleri sarı kart nedenidir."),
        Q("A defender fouls an attacker inside the box, denying an obvious goal-scoring opportunity while genuinely trying to play the ball. Sanction?",
            "Bir savunmacı ceza sahasında, topa oynamaya çalışırken net gol fırsatını faulle engelliyor. Yaptırım nedir?",
            listOf("Penalty + yellow card", "Penalty + red card", "Indirect free kick", "Penalty, no card"),
            listOf("Penaltı + sarı kart", "Penaltı + kırmızı kart", "Endirekt serbest vuruş", "Penaltı, kart yok"), 0,
            "If there was a genuine attempt to play the ball, the red card is reduced to a yellow.",
            "Topa oynama girişimi varsa kırmızı kart sarıya düşer."),
        Q("A defender deliberately handles the ball to stop a certain goal. Sanction?",
            "Bir savunmacı kesin golü önlemek için topu bilerek elle oynuyor. Yaptırım nedir?",
            listOf("Yellow card", "Red card", "No card – free kick only", "Verbal warning"),
            listOf("Sarı kart", "Kırmızı kart", "Kart yok – sadece serbest vuruş", "Sözlü uyarı"), 1,
            "Denying a goal by deliberate handball is a red card offence.",
            "Golü bilerek elle engellemek kırmızı kart gerektirir."),
        Q("A substitute enters the field without the referee's permission. Usual sanction?",
            "Yedek oyuncu hakemin izni olmadan sahaya giriyor. Olağan yaptırım nedir?",
            listOf("Yellow card", "Red card", "No sanction", "Match abandoned"),
            listOf("Sarı kart", "Kırmızı kart", "Yaptırım yok", "Maç tatil edilir"), 0,
            "Entering the field without permission is a cautionable offence.",
            "İzinsiz sahaya girmek sarı kart gerektiren bir ihlaldir."),
        Q("The referee plays advantage but no benefit appears within a few seconds. What happens?",
            "Hakem avantaj uygular fakat birkaç saniye içinde avantaj oluşmaz. Ne olur?",
            listOf("The original offence is penalised", "Play continues without penalty",
                "Dropped ball", "The match is restarted with a kick-off"),
            listOf("İlk ihlal cezalandırılır", "Oyun cezasız devam eder",
                "Hakem atışı", "Maç başlama vuruşu ile başlar"), 0,
            "If the expected advantage does not develop, the referee penalises the original offence.",
            "Beklenen avantaj oluşmazsa hakem ilk ihlali cezalandırır."),
        Q("Which is required for a correct throw-in?",
            "Doğru bir taç atışı için hangisi gereklidir?",
            listOf("Both hands, from behind and over the head", "One hand is enough",
                "The taker must kneel", "The ball must be kicked"),
            listOf("İki elle, baş arkasından ve üzerinden", "Tek el yeterli",
                "Atan oyuncu diz çökmeli", "Top ayakla oynanmalı"), 0,
            "The thrower faces the field, uses both hands and delivers the ball from behind and over the head.",
            "Atıcı sahaya dönük olmalı, iki elle ve topu baş arkasından geçirerek atmalıdır."),
        Q("When is the ball in play at a goal kick?",
            "Kale vuruşunda top ne zaman oyuna girer?",
            listOf("When it is kicked and clearly moves", "Only after it leaves the penalty area",
                "After a second player touches it", "When the referee whistles again"),
            listOf("Vurulup belirgin şekilde hareket ettiğinde", "Sadece ceza sahasını terk ettiğinde",
                "İkinci bir oyuncu dokunduğunda", "Hakem tekrar düdük çaldığında"), 0,
            "The ball is in play as soon as it is kicked and clearly moves; it need not leave the penalty area.",
            "Top vurulup belirgin şekilde hareket ettiği anda oyundadır; ceza sahasını terk etmesi gerekmez."),
        Q("Under the 2026/27 Laws, to whom is a dropped ball given when the referee stops play?",
            "2026/27 Kurallarına göre hakem oyunu durdurduğunda hakem atışı kime verilir?",
            listOf("The team that would have kept or gained possession", "Always the team that was attacking", "Always the home team", "It is contested by one player from each team"),
            listOf("Topun kontrolünü sürdürecek veya kazanacak olan takıma", "Her zaman hücum eden takıma", "Her zaman ev sahibi takıma", "Her iki takımdan bir oyuncu top için mücadele eder"), 0,
            "Since 2026/27 the dropped ball is given to the team that would have kept or gained possession (including the restart had the ball gone out of play).",
            "2026/27'den itibaren hakem atışı, topun kontrolünü sürdürecek veya kazanacak takıma verilir (top çıksaydı yeniden başlatma hakkı olan takım dahil)."),
        Q("Which of these can the VAR review?",
            "VAR aşağıdakilerden hangisini inceleyebilir?",
            listOf("Throw-in decisions", "Direct red card incidents",
                "Yellow card for time wasting", "Corner kick decisions"),
            listOf("Taç atışı kararları", "Direkt kırmızı kart pozisyonları",
                "Zaman geciktirme sarı kartı", "Köşe vuruşu kararları"), 1,
            "VAR reviews goals, penalty decisions, direct red cards and mistaken identity.",
            "VAR; gol, penaltı, direkt kırmızı kart ve yanlış oyuncuya kart durumlarını inceler."),
        Q("When is a goal scored?",
            "Gol ne zaman olur?",
            listOf("When the whole ball passes over the goal line between the posts and under the crossbar",
                "When any part of the ball crosses the line", "When the ball touches the net",
                "When the ball hits the crossbar"),
            listOf("Topun tamamı direkler arasından ve üst direğin altından kale çizgisini geçtiğinde",
                "Topun herhangi bir kısmı çizgiyi geçtiğinde", "Top ağa değdiğinde",
                "Top üst direğe çarptığında"), 0,
            "The whole ball must wholly cross the line, with no offence by the scoring team.",
            "Topun tamamı çizgiyi geçmeli ve golü atan takım ihlal yapmamış olmalıdır."),
        Q("How many kicks does each team take initially in a penalty shoot-out?",
            "Penaltı atışlarında her takım başlangıçta kaç atış yapar?",
            listOf("3", "5", "7", "10"), listOf("3", "5", "7", "10"), 1,
            "Each team takes five kicks alternately; if still level, rounds continue with equal kicks.",
            "Her takım sırayla 5 atış yapar; eşitlik sürerse eşit sayıda atışla devam edilir.")
    )

    // (category, difficulty) for each question in `base`, same order.
    // categories: 0 Offside, 1 Fouls, 2 Restarts, 3 Penalties, 4 Basics. difficulty: 1 easy, 2 medium, 3 hard
    private val meta = listOf(
        4 to 1, 4 to 2, 4 to 1, 4 to 1, 4 to 1,
        3 to 1, 2 to 1, 0 to 1, 0 to 2, 0 to 2,
        2 to 2, 2 to 2, 2 to 1, 2 to 1, 3 to 2,
        1 to 1, 1 to 3, 1 to 2, 1 to 1, 1 to 1,
        1 to 3, 1 to 2, 1 to 2, 4 to 2, 2 to 1,
        2 to 2, 2 to 3, 4 to 2, 4 to 1, 3 to 1
    )

    private val extra: List<Q> = listOf(
        Q("A player is exactly level with the second-last opponent when the ball is played. Is this offside?",
            "Top oynanırken bir oyuncu sondan ikinci rakiple tam hizada. Bu ofsayt mıdır?",
            listOf("No, being level is not an offside position", "Yes, always offside", "Only if also level with the ball", "Only in the attacking third"),
            listOf("Hayır, hizada olmak ofsayt pozisyonu değildir", "Evet, her zaman ofsayttır", "Sadece topla da hizadaysa", "Sadece hücum bölgesinde"), 0,
            "A player level with the second-last opponent (or the last two opponents) is not in an offside position.",
            "Sondan ikinci rakiple (veya son iki rakiple) aynı hizadaki oyuncu ofsayt pozisyonunda değildir.").t(0, 1),
        Q("A player in an offside position does not touch the ball, interfere with an opponent or gain an advantage. Decision?",
            "Ofsayt pozisyonundaki oyuncu topa dokunmuyor, rakibi engellemiyor ve avantaj sağlamıyor. Karar?",
            listOf("Indirect free kick", "Play on", "Yellow card", "Dropped ball"),
            listOf("Endirekt serbest vuruş", "Oyun devam", "Sarı kart", "Hakem atışı"), 1,
            "Being in an offside position is not an offence; the player must become involved in active play.",
            "Ofsayt pozisyonunda olmak ihlal değildir; oyuncunun aktif oyuna müdahil olması gerekir.").t(0, 2),
        Q("Which body parts are NOT considered when judging an offside position?",
            "Ofsayt pozisyonu değerlendirilirken hangi uzuvlar dikkate alınmaz?",
            listOf("Head", "Feet", "Hands and arms", "Torso"),
            listOf("Baş", "Ayaklar", "Eller ve kollar", "Gövde"), 2,
            "Only body parts with which a goal can legally be scored count; hands and arms are not considered.",
            "Sadece gol atmakta kullanılabilen vücut bölümleri sayılır; eller ve kollar dikkate alınmaz.").t(0, 2),
        Q("A goalkeeper makes a deliberate save and the rebound goes to an attacker who was in an offside position. Decision?",
            "Kaleci bilerek kurtarış yapıyor ve dönen top, ofsayt pozisyonundaki hücum oyuncusuna geliyor. Karar?",
            listOf("Offside offence", "Play on – the save resets offside", "Dropped ball", "Penalty kick"),
            listOf("Ofsayt ihlali", "Oyun devam – kurtarış ofsaytı sıfırlar", "Hakem atışı", "Penaltı atışı"), 0,
            "A deliberate save does not count as deliberate play, so the attacker is penalised for offside.",
            "Bilinçli kurtarış 'kasıtlı oynama' sayılmaz; bu yüzden hücum oyuncusu ofsayttan cezalandırılır.").t(0, 3),
        Q("What is awarded to the defending team for an offside offence?",
            "Ofsayt ihlalinde savunma takımına ne verilir?",
            listOf("Direct free kick", "Indirect free kick", "Penalty kick", "Dropped ball"),
            listOf("Direkt serbest vuruş", "Endirekt serbest vuruş", "Penaltı atışı", "Hakem atışı"), 1,
            "Offside is punished with an indirect free kick for the opposing team.",
            "Ofsayt, rakip takıma verilen endirekt serbest vuruşla cezalandırılır.").t(0, 1),
        Q("In which direction must a penalty kick be taken?",
            "Penaltı atışı hangi yöne yapılmalıdır?",
            listOf("Forward", "Any direction", "Backward", "Sideways"),
            listOf("İleri", "Herhangi bir yön", "Geri", "Yana"), 0,
            "The ball must be kicked forward.",
            "Top ileri doğru vurulmalıdır.").t(3, 1),
        Q("At a penalty kick in open play the taker accidentally plays the ball twice (it touches the standing leg) and the ball goes into the goal. Decision?",
            "Maç içindeki bir penaltıda vurucu topa yanlışlıkla iki kez dokunuyor (top dayanak bacağına çarpıyor) ve top kaleye giriyor. Karar?",
            listOf("The kick is retaken", "Goal", "Indirect free kick to the defending team", "Goal kick"),
            listOf("Atış tekrarlanır", "Gol", "Savunma takımına endirekt serbest vuruş", "Kale vuruşu"), 0,
            "Under the 2026/27 Laws an accidental double touch that scores means the kick is retaken.",
            "2026/27 Kurallarında, gol olan kazara çift dokunuşta atış tekrarlanır.").t(3, 3),
        Q("The goalkeeper leaves the goal line too early and saves a penalty. Decision?",
            "Kaleci kale çizgisinden erken ayrılıyor ve penaltıyı kurtarıyor. Karar?",
            listOf("Goal kick", "Kick is retaken and the goalkeeper is cautioned", "Play on", "Indirect free kick"),
            listOf("Kale vuruşu", "Atış tekrarlanır ve kaleciye sarı kart gösterilir", "Oyun devam", "Endirekt serbest vuruş"), 1,
            "If the goalkeeper's offence affects a missed or saved kick, it is retaken and the goalkeeper is cautioned.",
            "Kalecinin ihlali kaçan veya kurtarılan atışı etkilerse atış tekrarlanır ve kaleci sarı kart görür.").t(3, 3),
        Q("Where must a defender commit a direct free kick offence for a penalty kick to be awarded?",
            "Penaltı verilebilmesi için savunmacı direkt serbest vuruş gerektiren ihlali nerede yapmalıdır?",
            listOf("Inside their own penalty area", "Anywhere in their own half", "Inside the goal area only", "Within 20 m of the goal"),
            listOf("Kendi ceza sahası içinde", "Kendi yarı sahasının herhangi bir yerinde", "Sadece kale alanında", "Kaleye 20 m mesafede"), 0,
            "A penalty kick is awarded for a direct free kick offence by a defender inside their own penalty area while the ball is in play.",
            "Top oyundayken savunmacı kendi ceza sahası içinde direkt serbest vuruş gerektiren ihlal yaparsa penaltı verilir.").t(3, 1),
        Q("A penalty is awarded just as the half is ending. What happens?",
            "Devre bitmek üzereyken penaltı kararı veriliyor. Ne olur?",
            listOf("Time is extended until the penalty is completed", "The half ends immediately", "The penalty is taken in the next half", "A goal kick is given instead"),
            listOf("Penaltı tamamlanana kadar süre uzatılır", "Devre hemen biter", "Penaltı sonraki devrede kullanılır", "Yerine kale vuruşu verilir"), 0,
            "The half is extended to allow a penalty kick to be taken and completed.",
            "Penaltı atışının kullanılıp sonuçlanabilmesi için devre süresi uzatılır.").t(3, 3)
    )

    // ------------------------------------------------------------------
    //  What's new in the 2026/27 Laws (category 5)
    // ------------------------------------------------------------------
    private val whatsNew: List<Q> = listOf(
        Q("From which date do the IFAB 2026/27 Laws of the Game apply?",
            "IFAB 2026/27 Oyun Kuralları hangi tarihten itibaren geçerlidir?",
            listOf("1 July 2026", "1 January 2027", "1 July 2027", "1 September 2026"),
            listOf("1 Temmuz 2026", "1 Ocak 2027", "1 Temmuz 2027", "1 Eylül 2026"), 0,
            "The 2026/27 Laws came into force on 1 July 2026.",
            "2026/27 Kuralları 1 Temmuz 2026'da yürürlüğe girdi.").t(5, 1),
        Q("How many seconds does a substituted player have to leave the field?",
            "Oyundan çıkan oyuncunun sahadan ayrılması için kaç saniyesi vardır?",
            listOf("5", "10", "15", "30"), listOf("5", "10", "15", "30"), 1,
            "A substituted player must leave the field within 10 seconds.",
            "Oyundan çıkan oyuncu 10 saniye içinde sahadan ayrılmalıdır.").t(5, 1),
        Q("A substituted player exceeds the 10 seconds without a safety or injury reason. What happens to the substitute?",
            "Oyundan çıkan oyuncu güvenlik veya sakatlık nedeni olmadan 10 saniyeyi aşıyor. Yedek oyuncuya ne olur?",
            listOf("Enters immediately", "Cannot enter until the first stoppage after one minute has elapsed", "The team is cautioned", "The substitution is cancelled"),
            listOf("Hemen girer", "Bir dakika geçtikten sonraki ilk duraklamaya kadar giremez", "Takım uyarılır", "Oyuncu değişikliği iptal edilir"), 1,
            "The substitute may only enter at the first stoppage after one minute has elapsed.",
            "Yedek oyuncu, bir dakika geçtikten sonraki ilk duraklamada oyuna girebilir.").t(5, 2),
        Q("A team delays a throw-in and the referee's 5-second countdown is exceeded. Decision?",
            "Bir takım taç atışını geciktiriyor ve hakemin 5 saniyelik geri sayımı aşılıyor. Karar?",
            listOf("Direct free kick", "Throw-in to the opposing team", "Yellow card only", "Corner kick"),
            listOf("Direkt serbest vuruş", "Rakip takıma taç atışı", "Sadece sarı kart", "Köşe vuruşu"), 1,
            "If the countdown is exceeded, the throw-in is awarded to the opposing team.",
            "Geri sayım aşılırsa taç atışı rakip takıma verilir.").t(5, 2),
        Q("A team delays a goal kick and the referee's 5-second countdown is exceeded. Decision?",
            "Bir takım kale vuruşunu geciktiriyor ve hakemin 5 saniyelik geri sayımı aşılıyor. Karar?",
            listOf("Goal kick is retaken", "Corner kick to the opposing team", "Indirect free kick", "Dropped ball"),
            listOf("Kale vuruşu tekrarlanır", "Rakip takıma köşe vuruşu", "Endirekt serbest vuruş", "Hakem atışı"), 1,
            "If the countdown is exceeded at a goal kick, a corner kick is awarded to the opponents.",
            "Kale vuruşunda geri sayım aşılırsa rakip takıma köşe vuruşu verilir.").t(5, 2),
        Q("The goalkeeper controls the ball with the hands for more than 8 seconds. Decision?",
            "Kaleci topu elleriyle 8 saniyeden fazla kontrol ediyor. Karar?",
            listOf("Indirect free kick", "Corner kick to the opponents", "Penalty kick", "Dropped ball"),
            listOf("Endirekt serbest vuruş", "Rakip lehine köşe vuruşu", "Penaltı atışı", "Hakem atışı"), 1,
            "Holding the ball more than 8 seconds is punished with a corner kick for the opponents.",
            "Topu 8 saniyeden fazla elde tutmak rakip lehine köşe vuruşuyla cezalandırılır.").t(5, 2),
        Q("An outfield player receives treatment on the field. What must they do under the 2026/27 protocol?",
            "Bir saha oyuncusu sahada tedavi görüyor. 2026/27 protokolüne göre ne yapmalıdır?",
            listOf("Leave and stay off until one minute after the restart", "Continue immediately", "Leave until half-time", "Leave only if a substitute is available"),
            listOf("Sahadan çıkıp yeniden başlamadan bir dakika sonrasına kadar dışarıda kalmalı", "Hemen devam eder", "Devre arasına kadar dışarıda kalır", "Sadece yedek varsa çıkar"), 0,
            "Treated or injured outfield players must leave and stay off until one minute after the restart, with certain exceptions.",
            "Tedavi gören veya sakatlanan saha oyuncuları, bazı istisnalar dışında, yeniden başlamadan bir dakika sonrasına kadar dışarıda kalmalıdır.").t(5, 2),
        Q("Which player is an exception to the one-minute off-field rule after treatment?",
            "Tedavi sonrası 'bir dakika dışarıda kalma' kuralının istisnası olan oyuncu kimdir?",
            listOf("The goalkeeper", "The captain", "The striker", "Any player in the penalty area"),
            listOf("Kaleci", "Kaptan", "Forvet", "Ceza sahasındaki herhangi bir oyuncu"), 0,
            "The goalkeeper is an exception to the one-minute rule.",
            "Kaleci, bir dakika kuralının istisnasıdır.").t(5, 3),
        Q("In senior international 'A' friendlies, how many substitutes may be used (without a special agreement)?",
            "Kıdemli 'A' milli hazırlık maçlarında (özel anlaşma olmadan) en fazla kaç oyuncu değişikliği yapılabilir?",
            listOf("5", "6", "8", "11"), listOf("5", "6", "8", "11"), 2,
            "Up to 8 substitutes, or up to 11 if both teams agree and inform the referee beforehand.",
            "En fazla 8; iki takım anlaşıp hakemi önceden bilgilendirirse 11'e kadar.").t(5, 2),
        Q("What is the usual maximum number of substitutes in official competitions (outside youth football)?",
            "Resmi müsabakalarda (gençlik futbolu dışında) olağan en fazla oyuncu değişikliği sayısı kaçtır?",
            listOf("3", "5", "7", "9"), listOf("3", "5", "7", "9"), 1,
            "Official competitions may allow up to five substitutes.",
            "Resmi müsabakalarda en fazla beş oyuncu değişikliği yapılabilir.").t(5, 1),
        Q("Under 2026/27, accessories (e.g. jewellery) are permitted if…",
            "2026/27'ye göre aksesuarlar (ör. takı) hangi koşulda izinlidir?",
            listOf("They are not dangerous and are safely covered", "They are never permitted", "They are taped, even if dangerous", "Only the goalkeeper may wear them"),
            listOf("Tehlikeli değilse ve güvenli şekilde kaplanmışsa", "Hiçbir zaman izinli değildir", "Tehlikeli olsa bile bantlanmışsa", "Sadece kaleci takabilir"), 0,
            "Accessories must not be dangerous and must be safely and securely covered; dangerous items must be removed.",
            "Aksesuarlar tehlikeli olmamalı ve güvenli şekilde kaplanmalıdır; tehlikeli olanlar çıkarılmalıdır.").t(5, 2),
        Q("When may the referee apply advantage after an incorrectly taken restart?",
            "Hakem, yanlış kullanılan bir yeniden başlatmadan sonra avantajı ne zaman uygulayabilir?",
            listOf("When the ball is in play and the opponents benefit by regaining possession", "Never; it must always be retaken", "Only after free kicks", "Only in the penalty area"),
            listOf("Top oyundayken ve rakip topu kazanarak avantaj sağlıyorsa", "Asla; her zaman tekrarlanmalı", "Sadece serbest vuruşlardan sonra", "Sadece ceza sahasında"), 0,
            "The 2026/27 Laws clarify that advantage may be applied after an incorrect restart when the ball is in play and the opponents benefit.",
            "2026/27 Kuralları, top oyundayken ve rakip avantaj sağlıyorsa yanlış yeniden başlatmadan sonra avantaj uygulanabileceğini netleştirir.").t(5, 3),
        Q("A yellow or red card is shown to the wrong player. Can the VAR review this under 2026/27?",
            "Yanlış oyuncuya sarı veya kırmızı kart gösteriliyor. 2026/27'de VAR bunu inceleyebilir mi?",
            listOf("Yes – mistaken identity is reviewable", "No – cards are never reviewable", "Only yellow cards", "Only at half-time"),
            listOf("Evet – yanlış oyuncuya kart (kimlik hatası) incelenebilir", "Hayır – kartlar asla incelenemez", "Sadece sarı kartlar", "Sadece devre arasında"), 0,
            "Mistaken identity is reviewable when the offence was committed by another player.",
            "Kartı gerektiren ihlali başka bir oyuncu yaptıysa kimlik hatası VAR tarafından incelenebilir.").t(5, 2),
        Q("A red card results from a clearly incorrect second yellow card. Can the VAR review it?",
            "Açıkça hatalı bir ikinci sarı kart kırmızıya dönüşüyor. VAR inceleyebilir mi?",
            listOf("Yes, 2026/27 allows it", "No", "Only in finals", "Only if the referee asks the captain"),
            listOf("Evet, 2026/27 buna izin verir", "Hayır", "Sadece finallerde", "Sadece hakem kaptana sorarsa"), 0,
            "A red card resulting from a clearly incorrect second yellow is now reviewable.",
            "Açıkça hatalı ikinci sarıdan doğan kırmızı kart artık incelenebilir.").t(5, 3),
        Q("Who owns and controls the footage of referee body cameras?",
            "Hakem vücut kameralarının görüntülerinin sahibi ve kontrolcüsü kimdir?",
            listOf("The competition organiser", "The referee", "The club", "The broadcaster"),
            listOf("Müsabaka organizatörü", "Hakem", "Kulüp", "Yayıncı kuruluş"), 0,
            "Competitions may provide body cameras; the organiser owns and controls the footage.",
            "Müsabakalar vücut kamerası sağlayabilir; görüntülerin sahibi ve kontrolcüsü organizatördür.").t(5, 3),
        Q("At a penalty kick in open play the taker accidentally touches the ball twice and the ball does NOT score. Decision?",
            "Maç içindeki bir penaltıda vurucu topa yanlışlıkla iki kez dokunuyor ve top gol olmuyor. Karar?",
            listOf("Retaken", "Indirect free kick to the defending team", "Goal kick", "Dropped ball"),
            listOf("Atış tekrarlanır", "Savunma takımına endirekt serbest vuruş", "Kale vuruşu", "Hakem atışı"), 1,
            "If an accidental double touch does not score, an indirect free kick is awarded to the defending team.",
            "Kazara çift dokunuş gol olmazsa savunma takımına endirekt serbest vuruş verilir.").t(5, 3),
        Q("In a penalty shoot-out the taker accidentally touches the ball twice and it does NOT score. How is it recorded?",
            "Penaltı atışlarında vurucu kazara topa iki kez dokunuyor ve gol olmuyor. Nasıl kaydedilir?",
            listOf("As missed", "Retaken", "As a goal", "As an indirect free kick"),
            listOf("Kaçırılmış olarak", "Tekrarlanır", "Gol olarak", "Endirekt serbest vuruş olarak"), 0,
            "In a shoot-out an accidental double touch is recorded as missed if it does not score (retaken if it scores).",
            "Atış serisinde gol olmayan kazara çift dokunuş kaçırılmış sayılır (gol olursa tekrarlanır).").t(5, 3),
        Q("In a shoot-out the kicker and goalkeeper commit an offence at the same time. 2026/27 decision?",
            "Penaltı atış serisinde vurucu ve kaleci aynı anda ihlal yapıyor. 2026/27 kararı?",
            listOf("Kick recorded as missed, no automatic caution", "Kick retaken, kicker cautioned", "Goal", "Kicker sent off"),
            listOf("Atış kaçırılmış sayılır, otomatik sarı kart yok", "Atış tekrarlanır, vurucuya sarı kart", "Gol", "Vurucu kırmızı kart görür"), 0,
            "The automatic caution for the kicker was removed; the kick is recorded as missed.",
            "Vurucunun otomatik sarı kartı kaldırıldı; atış kaçırılmış sayılır.").t(5, 3),
        Q("A foul that would deny an obvious goal-scoring opportunity occurs, the referee plays advantage and a goal is scored. Is the offender cautioned?",
            "Net gol fırsatını engelleyecek bir faul oluyor, hakem avantaj veriyor ve gol oluyor. Faulü yapan oyuncuya kart gösterilir mi?",
            listOf("No caution", "Yellow card", "Red card", "Only a verbal warning is recorded and a yellow later"),
            listOf("Kart gösterilmez", "Sarı kart", "Kırmızı kart", "Sözlü uyarı yapılır, sonra sarı kart gösterilir"), 0,
            "Under 2026/27 there is no caution for a DOGSO offence if advantage is played and a goal is scored.",
            "2026/27'de avantaj verilip gol olursa net gol fırsatı ihlali için sarı kart gösterilmez.").t(5, 3),
        Q("Which action is cautionable as delaying the restart (2026/27 examples)?",
            "Hangisi yeniden başlatmayı geciktirmek nedeniyle sarı kart gerektirir (2026/27 örnekleri)?",
            listOf("Faking a throw-in and passing the ball to a team-mate", "Taking a quick free kick", "Throwing the ball in quickly", "Playing advantage"),
            listOf("Taç atışı yapıyormuş gibi yapıp topu takım arkadaşına vermek", "Hızlı serbest vuruş kullanmak", "Taçı hızlıca atmak", "Avantaj oynamak"), 0,
            "Faking a throw-in and passing it to a team-mate is cautionable.",
            "Taç atışı yapıyormuş gibi yapıp topu takım arkadaşına vermek sarı kart gerektirir.").t(5, 2),
        Q("A dropped ball goes directly into the opponents' goal without touching another player. Decision?",
            "Hakem atışı, başka bir oyuncuya değmeden doğrudan rakip kaleye giriyor. Karar?",
            listOf("Goal", "Goal kick", "Dropped ball retaken", "Corner kick"),
            listOf("Gol", "Kale vuruşu", "Hakem atışı tekrarlanır", "Köşe vuruşu"), 1,
            "A goal cannot be scored directly from a dropped ball; a goal kick (or corner kick if it enters the taker's own goal) is awarded.",
            "Hakem atışından doğrudan gol olmaz; kale vuruşu (kendi kaleye girerse köşe vuruşu) verilir.").t(5, 3),
        Q("From when does the 'only the captain may approach the referee' guideline become mandatory in all competitions?",
            "'Hakeme sadece kaptan yaklaşabilir' kuralı tüm müsabakalarda hangi tarihten itibaren zorunlu olacak?",
            listOf("1 July 2026", "1 July 2027", "1 January 2027", "It is already mandatory"),
            listOf("1 Temmuz 2026", "1 Temmuz 2027", "1 Ocak 2027", "Zaten zorunlu"), 1,
            "IFAB gave advance notice: the guideline becomes a compulsory protocol from 1 July 2027.",
            "IFAB ön bildirim yaptı: bu kılavuz 1 Temmuz 2027'den itibaren zorunlu protokol olacak.").t(5, 3)
    )

    // ------------------------------------------------------------------
    //  Card / sanction scenarios (category 1)
    // ------------------------------------------------------------------
    private val sEn = listOf("No card", "Yellow card", "Red card", "Verbal warning only")
    private val sTr = listOf("Kart yok", "Sarı kart", "Kırmızı kart", "Sadece sözlü uyarı")

    private fun sanc(en: String, tr: String, ans: Int, wEn: String, wTr: String, diff: Int): Q =
        Q("$en Sanction?", "$tr Yaptırım nedir?", sEn, sTr, ans, wEn, wTr).t(1, diff)

    private val sanctions: List<Q> = listOf(
        sanc("A player takes off their shirt to celebrate a goal.", "Bir oyuncu gol sevinci için formasını çıkarıyor.", 1,
            "Removing the shirt when celebrating is cautionable.", "Sevinç için formayı çıkarmak sarı kart gerektirir.", 1),
        sanc("A player kicks the ball away after the referee stops play.", "Hakem oyunu durdurduktan sonra bir oyuncu topu uzağa vuruyor.", 1,
            "Delaying the restart is cautionable.", "Yeniden başlatmayı geciktirmek sarı kart gerektirir.", 1),
        sanc("A player shouts at the referee to protest a decision.", "Bir oyuncu hakemin kararını protesto etmek için bağırıyor.", 1,
            "Dissent by word or action is cautionable.", "Sözle veya hareketle itiraz sarı kart gerektirir.", 1),
        sanc("A defender pulls an attacker's shirt to stop a promising attack.", "Bir savunmacı umut vadeden bir atağı durdurmak için hücum oyuncusunun formasını çekiyor.", 1,
            "Stopping a promising attack by an offence is cautionable.", "İhlalle umut vadeden atağı durdurmak sarı kart gerektirir.", 2),
        sanc("A player deliberately handles the ball to stop a promising attack (not a certain goal).", "Bir oyuncu umut vadeden atağı durdurmak için topu bilerek elle oynuyor (kesin gol değil).", 1,
            "Deliberate handball to stop a promising attack is cautionable.", "Umut vadeden atağı durdurmak için bilerek elle oynamak sarı kart gerektirir.", 2),
        sanc("A player fakes a throw-in and passes the ball to a team-mate to waste time.", "Bir oyuncu zaman geçirmek için taç atışı yapıyormuş gibi yapıp topu takım arkadaşına veriyor.", 1,
            "Under 2026/27 this is specified as cautionable delaying of the restart.", "2026/27'de bu, yeniden başlatmayı geciktirmek olarak sarı kart gerektirir.", 2),
        sanc("A defender stands 3 m from the ball at a free kick and refuses to retreat.", "Bir savunmacı serbest vuruşta toptan 3 m uzakta durup geri çekilmiyor.", 1,
            "Failing to respect the required distance is cautionable.", "Gereken mesafeye uymamak sarı kart gerektirir.", 2),
        sanc("A player deliberately leaves the field without the referee's permission.", "Bir oyuncu hakemin izni olmadan bilerek sahadan ayrılıyor.", 1,
            "Leaving the field deliberately without permission is cautionable.", "İzinsiz ve bilerek sahadan ayrılmak sarı kart gerektirir.", 2),
        sanc("A player dives to deceive the referee and win a free kick.", "Bir oyuncu hakemi kandırıp serbest vuruş kazanmak için kendini yere atıyor.", 1,
            "Simulation is unsporting behaviour and cautionable.", "Simülasyon sportmenlik dışı davranıştır ve sarı kart gerektirir.", 1),
        sanc("A player uses a deliberate trick to pass the ball to their goalkeeper with the head to avoid the back-pass rule.", "Bir oyuncu geri pas kuralını aşmak için topu kafayla kalecisine özel bir hileyle veriyor.", 1,
            "Circumventing the Law with a deliberate trick is unsporting behaviour.", "Kuralı hileyle dolanmak sportmenlik dışı davranıştır.", 3),
        sanc("A substituted player walks off very slowly to waste time.", "Oyundan çıkan oyuncu zaman geçirmek için çok yavaş yürüyerek ayrılıyor.", 1,
            "Dawdling when being substituted is cautionable (2026/27 wording).", "Oyundan çıkarken oyalanmak sarı kart gerektirir (2026/27).", 2),
        sanc("A player deliberately takes a free kick from the wrong place to waste time.", "Bir oyuncu zaman geçirmek için serbest vuruşu bilerek yanlış yerden kullanıyor.", 1,
            "Deliberately taking a free kick from the wrong place is cautionable.", "Serbest vuruşu bilerek yanlış yerden kullanmak sarı kart gerektirir.", 3),
        sanc("A player elbows an opponent in the face with excessive force while the ball is out of reach.", "Bir oyuncu top uzaktayken rakibin yüzüne aşırı güçle dirsek atıyor.", 2,
            "This is violent conduct: a red card.", "Bu şiddet içeren davranıştır: kırmızı kart.", 1),
        sanc("A player makes a studs-up tackle that endangers an opponent's safety.", "Bir oyuncu rakibin güvenliğini tehlikeye atan, çivili tarafı ile bir müdahale yapıyor.", 2,
            "Endangering the safety of an opponent is serious foul play.", "Rakibin güvenliğini tehlikeye atmak ağır faul oyunudur.", 2),
        sanc("A player uses offensive, insulting or abusive language towards the referee.", "Bir oyuncu hakeme karşı saldırgan, hakaret içeren veya küfürlü sözler kullanıyor.", 2,
            "Offensive, insulting or abusive language or gestures are red card offences.", "Saldırgan, hakaret içeren veya küfürlü söz ve hareketler kırmızı kart gerektirir.", 1),
        sanc("A player bites an opponent.", "Bir oyuncu rakibini ısırıyor.", 2,
            "Biting is violent conduct: a red card.", "Isırmak şiddet içeren davranıştır: kırmızı kart.", 1),
        sanc("A player head-butts an opponent.", "Bir oyuncu rakibine kafa atıyor.", 2,
            "Head-butting is violent conduct: a red card.", "Kafa atmak şiddet içeren davranıştır: kırmızı kart.", 1),
        sanc("A player who already has a yellow card commits another cautionable offence.", "Sarı kartı olan bir oyuncu ikinci bir sarı kart gerektiren ihlal yapıyor.", 2,
            "A second caution in the same match leads to a red card.", "Aynı maçta ikinci sarı kart, kırmızı karta dönüşür.", 1),
        sanc("A defender inside the penalty area holds an attacker who is clear on goal, with no attempt to play the ball.", "Ceza sahasındaki bir savunmacı, kaleyle yalnız kalan hücum oyuncusunu topa oynamaya çalışmadan tutuyor.", 2,
            "Denying an obvious goal-scoring opportunity by holding (no attempt to play the ball) is a red card, plus a penalty.", "Topa oynama çabası olmadan tutarak net gol fırsatını engellemek kırmızı karttır (ayrıca penaltı).", 3),
        sanc("Two players challenge fairly shoulder to shoulder, both going for the ball.", "İki oyuncu da topa giderken omuz omuza adil bir mücadele yapıyor.", 0,
            "A fair charge is not an offence.", "Adil bir omuz mücadelesi ihlal değildir.", 1),
        sanc("A player makes a careless tackle, showing a lack of attention but no recklessness.", "Bir oyuncu dikkatsiz, ama pervasız olmayan bir müdahale yapıyor.", 0,
            "A careless foul is a free kick without a card.", "Dikkatsiz faul kartsız serbest vuruştur.", 2),
        sanc("A player is penalised for an offside offence.", "Bir oyuncu ofsayt ihlali nedeniyle cezalandırılıyor.", 0,
            "Offside is punished with an indirect free kick, not a card.", "Ofsayt, kartla değil endirekt serbest vuruşla cezalandırılır.", 1)
    )

    // ------------------------------------------------------------------
    private fun tag(prefix: String, l: List<Q>): List<Q> {
        l.forEachIndexed { i, q -> q.id = prefix + i }
        return l
    }

    private val builtIn: List<Q> =
        tag("b", base.mapIndexed { i, q -> q.t(meta[i].first, meta[i].second) }) +
            tag("e", extra) + tag("n", whatsNew) + tag("s", sanctions) + tag("m", MoreQuestions.list)

    var all: List<Q> = builtIn
        private set

    /** Validates and builds a question from JSON (used for assets/questions.json and AI answers). */
    fun parse(o: JSONObject): Q? {
        return try {
            val a1 = o.getJSONArray("oEn")
            val a2 = o.getJSONArray("oTr")
            val oEn = (0 until a1.length()).map { a1.getString(it) }
            val oTr = (0 until a2.length()).map { a2.getString(it) }
            val ans = o.getInt("ans")
            val qEn = o.getString("qEn")
            val qTr = o.getString("qTr")
            if (oEn.size !in 2..4 || oEn.size != oTr.size || ans !in oEn.indices) return null
            if (qEn.isBlank() || qTr.isBlank() || oEn.any { it.isBlank() } || oTr.any { it.isBlank() }) return null
            val q = Q(qEn, qTr, oEn, oTr, ans, o.optString("wEn", ""), o.optString("wTr", ""))
            q.id = o.optString("id", "")
            q.cat = o.optInt("cat", 4).coerceIn(0, Cats.count() - 1)
            q.diff = o.optInt("diff", 2).coerceIn(1, 3)
            q
        } catch (e: Exception) {
            null
        }
    }

    /** Loads extra questions from app/src/main/assets/questions.json (AI-generated bank). */
    fun loadFromText(txt: String) {
        try {
            val arr = JSONArray(txt)
            val seen = builtIn.map { it.qEn.lowercase() }.toMutableSet()
            val out = mutableListOf<Q>()
            for (i in 0 until arr.length()) {
                val q = parse(arr.getJSONObject(i)) ?: continue
                if (!seen.add(q.qEn.lowercase())) continue
                if (q.id.isBlank()) q.id = "j$i"
                if (!q.id.startsWith("j")) q.id = "j" + q.id
                out.add(q)
            }
            all = builtIn + out
        } catch (e: Exception) {
        }
    }

    fun count(cat: Int): Int = all.count { it.cat == cat }

    fun byId(id: String): Q? {
        if (id.startsWith("g:")) {
            val p = id.split(":")
            if (p.size != 4) return null
            val kind = p[1].toIntOrNull() ?: return null
            val seed = p[2].toLongOrNull() ?: return null
            val param = p[3].toIntOrNull() ?: return null
            return Gen.make(kind, seed, param)
        }
        return all.firstOrNull { it.id == id }
    }

    /** 5 questions for a category level: easier levels prefer easier questions; 2 are freshly generated. */
    fun forLevel(cat: Int, level: Int): List<Q> {
        val statics = all.filter { it.cat == cat }.shuffled()
        val target = intArrayOf(1, 2, 3, 3, 0)[level - 1]
        val sorted = if (target == 0) statics else statics.sortedBy { abs(it.diff - target) }
        val out = mutableListOf<Q>()
        out.addAll(sorted.take(3))
        for (k in 0 until 2) {
            val g = Gen.randomFor(cat)
            if (g != null) out.add(g)
        }
        if (out.size < 5) out.addAll(sorted.drop(3).take(5 - out.size))
        return out.shuffled()
    }

    /** Same 5 questions for everyone on the same day. */
    fun daily(): List<Q> {
        val day = Dates.today()
        return all.shuffled(Random(day)).take(5)
    }

    /** Question ids for a duel: the same ids on both devices; each device shows its own language. */
    fun duelIds(seed: Long): List<String> {
        val r = Random(seed)
        val statics = all.shuffled(r).take(3).map { it.id }
        val gens = listOf(0, 1, 2).shuffled(r).take(2).map { "g:$it:${r.nextInt(1_000_000)}:0" }
        return (statics + gens).shuffled(r)
    }
}
