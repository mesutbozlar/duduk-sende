package com.armeaze.duduksende

import kotlin.random.Random

/**
 * Deterministic question generators: the same (kind, seed, param) always gives the same
 * question, so ids like "g:0:12345:0" can be shared between two devices (duel).
 * kind 0 = offside position (numbers), 1 = restart into goal, 2 = offence -> restart, 3 = fact
 */
object Gen {
    class Fact(
        val cat: Int, val qEn: String, val qTr: String, val unitEn: String, val unitTr: String,
        val correct: String, val wrong: List<String>, val wEn: String, val wTr: String
    )

    val facts: List<Fact> = listOf(
        Fact(3, "How far is the penalty mark from the goal line?", "Penaltı noktası kale çizgisine ne kadar uzaktır?", "m", "m", "11", listOf("9.15", "10", "12", "16.5", "5.5"),
            "The penalty mark is 11 m from the midpoint between the goalposts.", "Penaltı noktası, direkler arasının ortasından 11 m uzaktadır."),
        Fact(3, "At a penalty kick, how far from the penalty mark must all other players be?", "Penaltı atışında, vurucu ve kaleci dışındaki oyuncular penaltı noktasından en az ne kadar uzakta olmalıdır?", "m", "m", "9.15", listOf("5.5", "7.32", "11", "16.5"),
            "All other players must be outside the penalty area, behind the mark and at least 9.15 m from it.", "Diğer tüm oyuncular ceza sahası dışında, noktanın gerisinde ve en az 9,15 m uzakta olmalıdır."),
        Fact(3, "How far does the penalty area extend from the goal line?", "Ceza sahası kale çizgisinden itibaren ne kadar uzanır?", "m", "m", "16.5", listOf("11", "18.32", "20", "5.5", "9.15"),
            "The penalty area extends 16.5 m from the goal line.", "Ceza sahası kale çizgisinden 16,5 m uzanır."),
        Fact(4, "How far does the goal area extend from the goal line?", "Kale alanı kale çizgisinden ne kadar uzanır?", "m", "m", "5.5", listOf("4", "6", "9.15", "11"),
            "The goal area extends 5.5 m from the goal line.", "Kale alanı kale çizgisinden 5,5 m uzanır."),
        Fact(4, "What is the radius of the centre circle?", "Orta yuvarlağın yarıçapı nedir?", "m", "m", "9.15", listOf("5.5", "7.32", "10", "11"),
            "The centre circle has a radius of 9.15 m.", "Orta yuvarlağın yarıçapı 9,15 m'dir."),
        Fact(4, "How wide is a standard goal between the posts?", "Standart bir kale direkler arasında ne kadar geniştir?", "m", "m", "7.32", listOf("6.32", "7", "8.32", "9.15"),
            "The goal is 7.32 m wide.", "Kale 7,32 m genişliğindedir."),
        Fact(4, "How high is a standard goal?", "Standart bir kale ne kadar yüksektir?", "m", "m", "2.44", listOf("2.14", "2.34", "2.5", "2.74"),
            "The goal is 2.44 m high.", "Kale 2,44 m yüksekliğindedir."),
        Fact(4, "What is the radius of the corner arc?", "Köşe yayının yarıçapı nedir?", "m", "m", "1", listOf("0.5", "1.5", "2", "3"),
            "The corner arc has a radius of 1 m.", "Köşe yayının yarıçapı 1 m'dir."),
        Fact(4, "What is the maximum width of the field lines and goalposts?", "Saha çizgilerinin ve kale direklerinin en fazla genişliği nedir?", "cm", "cm", "12", listOf("8", "10", "15", "20"),
            "Lines, goalposts and crossbar must not be wider than 12 cm.", "Çizgiler, direkler ve üst direk 12 cm'den geniş olamaz."),
        Fact(4, "What is the weight of the ball at the start of the match?", "Maçın başında topun ağırlığı ne kadar olmalıdır?", "g", "g", "410–450", listOf("350–390", "450–500", "380–420", "500–550"),
            "The ball weighs 410–450 g at the start of the match.", "Top maçın başında 410–450 g ağırlığında olmalıdır."),
        Fact(4, "What is the circumference of the match ball?", "Maç topunun çevresi ne kadardır?", "cm", "cm", "68–70", listOf("58–60", "62–64", "72–74", "75–78"),
            "The ball's circumference is 68–70 cm.", "Topun çevresi 68–70 cm'dir."),
        Fact(4, "What is the length range of a pitch for international matches?", "Uluslararası maçlarda saha uzunluğu aralığı nedir?", "m", "m", "100–110", listOf("90–100", "105–120", "110–120", "95–105"),
            "For international matches the length must be 100–110 m.", "Uluslararası maçlarda saha uzunluğu 100–110 m olmalıdır."),
        Fact(4, "What is the width range of a pitch for international matches?", "Uluslararası maçlarda saha genişliği aralığı nedir?", "m", "m", "64–75", listOf("50–60", "60–70", "70–80", "68–80"),
            "For international matches the width must be 64–75 m.", "Uluslararası maçlarda saha genişliği 64–75 m olmalıdır."),
        Fact(4, "How long is each half of a standard match?", "Standart bir maçta her devre ne kadar sürer?", "minutes", "dakika", "45", listOf("30", "40", "50", "60"),
            "A match has two equal halves of 45 minutes.", "Maç, 45 dakikalık iki eşit devreden oluşur."),
        Fact(4, "What is the maximum length of the half-time interval?", "Devre arasının en fazla süresi nedir?", "minutes", "dakika", "15", listOf("5", "10", "20", "30"),
            "The interval must not exceed 15 minutes.", "Devre arası 15 dakikayı geçemez."),
        Fact(4, "What is the minimum number of players a team needs for a match to continue?", "Maçın devam edebilmesi için takımda en az kaç oyuncu olmalıdır?", "players", "oyuncu", "7", listOf("5", "6", "8", "9"),
            "A match cannot continue if a team has fewer than seven players.", "Bir takımda 7'den az oyuncu kalırsa maç devam edemez."),
        Fact(2, "How far must opponents be from a free kick or corner kick?", "Serbest vuruşta ve köşe vuruşunda rakipler en az ne kadar uzakta olmalıdır?", "m", "m", "9.15", listOf("5", "7.32", "10", "11"),
            "Opponents must be at least 9.15 m away until the ball is in play.", "Top oyuna girene kadar rakipler en az 9,15 m uzakta olmalıdır."),
        Fact(2, "How far must opponents stand from the point of a throw-in?", "Taç atışında rakipler atış noktasından en az ne kadar uzakta durmalıdır?", "m", "m", "2", listOf("1", "3", "5", "9.15"),
            "Opponents must be at least 2 m from the point where the throw-in is taken.", "Rakipler, taç atışı noktasından en az 2 m uzakta olmalıdır."),
        Fact(2, "At a dropped ball, how far must the other players stay from the ball?", "Hakem atışında diğer oyuncular toptan en az ne kadar uzakta durmalıdır?", "m", "m", "4", listOf("2", "5", "6", "9.15"),
            "All other players must be at least 4 m away until the ball is in play.", "Diğer tüm oyuncular top oyuna girene kadar en az 4 m uzakta olmalıdır."),
        Fact(5, "2026/27: how many seconds does a substituted player have to leave the field?", "2026/27: Oyundan çıkan oyuncunun sahadan ayrılması için kaç saniyesi vardır?", "seconds", "saniye", "10", listOf("5", "15", "20", "30"),
            "A substituted player must leave within 10 seconds.", "Oyundan çıkan oyuncu 10 saniye içinde ayrılmalıdır."),
        Fact(5, "2026/27: how long is the referee's countdown when a team delays a throw-in or goal kick?", "2026/27: Bir takım taç atışını veya kale vuruşunu geciktirirse hakemin geri sayımı kaç saniyedir?", "seconds", "saniye", "5", listOf("3", "8", "10", "15"),
            "The referee signals a 5-second countdown with a raised hand.", "Hakem el kaldırarak 5 saniyelik geri sayım yapar."),
        Fact(5, "After how many seconds of the goalkeeper controlling the ball with the hands is a corner kick awarded?", "Kaleci topu elleriyle kaç saniyeden fazla kontrol ederse köşe vuruşu verilir?", "seconds", "saniye", "8", listOf("5", "6", "10", "15"),
            "More than 8 seconds of control with the hands gives a corner kick to the opponents.", "8 saniyeyi aşan elle kontrol, rakibe köşe vuruşu kazandırır.")
    )

    private fun opt(v: String, unit: String, tr: Boolean): String {
        val s = "$v $unit"
        return if (tr) s.replace('.', ',') else s
    }

    fun make(kind: Int, seed: Long, param: Int): Q {
        val q = when (kind) {
            0 -> offside(seed)
            1 -> restart(seed)
            2 -> offence(seed)
            else -> fact(seed, param)
        }
        q.id = "g:$kind:$seed:$param"
        return q
    }

    /** A random generated question for a category, or null if that category has no generator. */
    fun randomFor(cat: Int): Q? {
        val seed = Random.nextInt(1_000_000).toLong()
        return when (cat) {
            0 -> make(0, seed, 0)
            1 -> make(2, seed, 0)
            2 -> make(1, seed, 0)
            else -> {
                val idx = facts.indices.filter { facts[it].cat == cat }
                if (idx.isEmpty()) null else make(3, seed, idx[Random.nextInt(idx.size)])
            }
        }
    }

    fun hasFor(cat: Int): Boolean = true

    private fun fact(seed: Long, idx: Int): Q {
        val f = facts[idx % facts.size]
        val r = Random(seed)
        val vals = listOf(f.correct) + f.wrong.shuffled(r).take(3)
        return Q(
            f.qEn, f.qTr,
            vals.map { opt(it, f.unitEn, false) }, vals.map { opt(it, f.unitTr, true) },
            0, f.wEn, f.wTr
        ).t(f.cat, 1)
    }

    private fun offside(seed: Long): Q {
        val r = Random(seed)
        val outcome = r.nextInt(4)
        val a: Int
        val b: Int
        val d: Int
        when (outcome) {
            0 -> { d = r.nextInt(12, 40); a = d - r.nextInt(1, 6); b = a + r.nextInt(2, 25) }
            1 -> { a = r.nextInt(52, 70); d = a + r.nextInt(3, 15); b = a + r.nextInt(3, 25) }
            2 -> { d = r.nextInt(20, 45); a = d - r.nextInt(1, 6); b = a - r.nextInt(0, 8) }
            else -> { d = r.nextInt(10, 40); a = d + r.nextInt(0, 7); b = a + r.nextInt(3, 20) }
        }
        val reasonEn = when (outcome) {
            0 -> "He is nearer to the opponents' goal line than both the ball and the second-last opponent, and he is in the opponents' half."
            1 -> "A player cannot be in an offside position in their own half."
            2 -> if (b == a) "Being level with the ball is not an offside position." else "He is behind the ball: the ball is nearer to the goal line."
            else -> if (a == d) "Being level with the second-last opponent is not an offside position." else "He is not nearer to the goal line than the second-last opponent."
        }
        val reasonTr = when (outcome) {
            0 -> "Hem topa hem de sondan ikinci rakibe göre rakip kale çizgisine daha yakın ve rakip yarı sahada."
            1 -> "Bir oyuncu kendi yarı sahasında ofsayt pozisyonunda olamaz."
            2 -> if (b == a) "Topla aynı hizada olmak ofsayt pozisyonu değildir." else "Top oyuncunun önündedir: top kale çizgisine daha yakındır."
            else -> if (a == d) "Sondan ikinci rakiple aynı hizada olmak ofsayt pozisyonu değildir." else "Sondan ikinci rakipten kale çizgisine daha yakın değildir."
        }
        return Q(
            "A teammate plays the ball. Distances to the opponents' goal line: ball $b m, attacker $a m, second-last opponent $d m (halfway line = 50 m). Is the attacker in an offside position? (Ignore involvement in play.)",
            "Bir takım arkadaşı topu oynuyor. Rakip kale çizgisine uzaklıklar: top $b m, hücum oyuncusu $a m, sondan ikinci rakip $d m (orta çizgi 50 m). Hücum oyuncusu ofsayt pozisyonunda mı? (Oyuna müdahale durumunu dikkate alma.)",
            listOf("Yes, in an offside position", "No – he is in his own half", "No – he is not nearer than the ball", "No – he is not nearer than the second-last opponent"),
            listOf("Evet, ofsayt pozisyonunda", "Hayır – kendi yarı sahasında", "Hayır – toptan daha yakın değil", "Hayır – sondan ikinci rakipten daha yakın değil"),
            outcome, reasonEn, reasonTr
        ).t(0, if (outcome == 0 || outcome == 1) 2 else 3)
    }

    private val rEn = listOf("kick-off", "goal kick", "direct free kick", "indirect free kick", "throw-in", "corner kick")
    private val rTr = listOf("başlama vuruşu", "kale vuruşu", "direkt serbest vuruş", "endirekt serbest vuruş", "taç atışı", "köşe vuruşu")

    private fun restart(seed: Long): Q {
        val r = Random(seed)
        val t = r.nextInt(6)
        val own = r.nextBoolean()
        val ans = if (own) 2 else if (t == 3 || t == 4) 1 else 0
        val wEn = when (ans) {
            0 -> "A goal can be scored directly from a ${rEn[t]} against the opponents."
            1 -> "A goal cannot be scored directly from a ${rEn[t]}; a goal kick is awarded."
            else -> "If the ball goes directly into the taker's own goal, a corner kick is awarded to the opponents."
        }
        val wTr = when (ans) {
            0 -> "Bu vuruş türünden rakip kaleye doğrudan gol olur."
            1 -> "Bu vuruş türünden doğrudan gol olmaz; kale vuruşu verilir."
            else -> "Top doğrudan atanın kendi kalesine girerse rakibe köşe vuruşu verilir."
        }
        return Q(
            "A ${rEn[t]} is taken and the ball goes directly into ${if (own) "the taker's own goal" else "the opponents' goal"} without touching any other player. Decision?",
            "Bir ${rTr[t]} kullanılıyor ve top başka bir oyuncuya dokunmadan doğrudan ${if (own) "atanın kendi kalesine" else "rakip kaleye"} giriyor. Karar?",
            listOf("Goal", "Goal kick", "Corner kick to the opponents", "Kick is retaken"),
            listOf("Gol", "Kale vuruşu", "Rakip lehine köşe vuruşu", "Vuruş tekrarlanır"),
            ans, wEn, wTr
        ).t(2, 2)
    }

    private val dEn = listOf("kicks an opponent", "trips an opponent", "pushes an opponent", "holds an opponent", "charges an opponent recklessly", "jumps at an opponent", "strikes an opponent", "deliberately handles the ball")
    private val dTr = listOf("rakibe tekme atıyor", "rakibe çelme takıyor", "rakibi itiyor", "rakibi tutuyor", "rakibe pervasızca çarpıyor", "rakibin üzerine atlıyor", "rakibe vuruyor", "topu bilerek elle oynuyor")
    private val iEn = listOf("plays in a dangerous manner without contact", "impedes an opponent's progress without contact", "prevents the goalkeeper from releasing the ball from the hands")
    private val iTr = listOf("temassız şekilde tehlikeli oyun oynuyor", "temas olmadan rakibin ilerleyişini engelliyor", "kalecinin topu elinden bırakmasını engelliyor")

    private fun offence(seed: Long): Q {
        val r = Random(seed)
        val direct = r.nextInt(3) != 0
        val loc = r.nextInt(4)
        val k = if (direct) r.nextInt(dEn.size) else r.nextInt(iEn.size)
        val vEn = if (direct) dEn[k] else iEn[k]
        val vTr = if (direct) dTr[k] else iTr[k]
        val qEn = when (loc) {
            0 -> "A defender $vEn inside their own penalty area. Decision?"
            1 -> "A defender $vEn just outside their own penalty area. Decision?"
            2 -> "An attacker $vEn inside the opponents' penalty area. Decision?"
            else -> "A player $vEn in the middle of the pitch. Decision?"
        }
        val qTr = when (loc) {
            0 -> "Bir savunmacı kendi ceza sahası içinde $vTr. Karar?"
            1 -> "Bir savunmacı kendi ceza sahasının hemen dışında $vTr. Karar?"
            2 -> "Bir hücum oyuncusu rakip ceza sahası içinde $vTr. Karar?"
            else -> "Bir oyuncu sahanın ortasında $vTr. Karar?"
        }
        val ans = if (!direct) 2 else if (loc == 0) 0 else 1
        val wEn = when (ans) {
            0 -> "A direct free kick offence by a defender inside their own penalty area is a penalty kick."
            1 -> "This is a direct free kick offence: a direct free kick for the opponents."
            else -> "This is an indirect free kick offence: an indirect free kick for the opponents."
        }
        val wTr = when (ans) {
            0 -> "Savunmacının kendi ceza sahasında yaptığı direkt serbest vuruş ihlali penaltıdır."
            1 -> "Bu, direkt serbest vuruş ihlalidir: rakip lehine direkt serbest vuruş."
            else -> "Bu, endirekt serbest vuruş ihlalidir: rakip lehine endirekt serbest vuruş."
        }
        return Q(
            qEn, qTr,
            listOf("Penalty kick", "Direct free kick to the opponents", "Indirect free kick to the opponents", "Dropped ball"),
            listOf("Penaltı atışı", "Rakip lehine direkt serbest vuruş", "Rakip lehine endirekt serbest vuruş", "Hakem atışı"),
            ans, wEn, wTr
        ).t(1, if (direct && loc != 0) 1 else 2)
    }
}
