package com.armeaze.duduksende

class HistRow(val day: String, val kind: String, val correct: Int, val answered: Int)

class Store(private val kv: KV) {

    init {
        if (!kv.has("coins")) put("coins", 30)
    }

    var lang: String
        get() = kv.getString("lang") ?: if (Plat.systemLang() == "tr") "tr" else "en"
        set(v) { kv.putString("lang", v) }

    var name: String
        get() = kv.getString("name") ?: "Player"
        set(v) { kv.putString("name", v) }

    var onboarded: Boolean
        get() = int("onboarded") == 1
        set(v) { put("onboarded", if (v) 1 else 0) }

    var soundOn: Boolean
        get() = int("sound", 1) == 1
        set(v) { put("sound", if (v) 1 else 0) }

    var vibOn: Boolean
        get() = int("vib", 1) == 1
        set(v) { put("vib", if (v) 1 else 0) }

    fun int(k: String, d: Int = 0): Int = kv.getInt(k, d)
    fun put(k: String, v: Int) { kv.putInt(k, v) }
    fun add(k: String, v: Int) { put(k, int(k) + v) }
    fun best(k: String, v: Int) { if (v > int(k)) put(k, v) }
    private fun str(k: String): String? = kv.getString(k)
    private fun putStr(k: String, v: String) { kv.putString(k, v) }

    fun levelUnlocked(cat: Int, level: Int): Boolean =
        level <= 2 || int("xp") >= Levels.NEED[level - 1] || int("unlock_${cat}_$level") == 1

    fun dailyDone(): Boolean = str("daily_day") == Dates.iso(Dates.today())

    fun streak(): Int {
        val last = str("last_day") ?: return 0
        val d = Dates.parse(last) ?: return 0
        val today = Dates.today()
        return if (d == today || d == today - 1L) int("streak") else 0
    }

    private fun touchStreak() {
        val today = Dates.today()
        val last = str("last_day")?.let { Dates.parse(it) }
        if (last == today) return
        if (last == today - 1L) put("streak", int("streak") + 1) else put("streak", 1)
        putStr("last_day", Dates.iso(today))
    }

    private fun readLog(): MutableMap<String, IntArray> {
        val m = mutableMapOf<String, IntArray>()
        val raw = str("log") ?: ""
        for (e in raw.split(";")) {
            if (e.isBlank()) continue
            val parts = e.split(",")
            if (parts.size != 5) continue
            m[parts[0]] = IntArray(4) { i -> parts[i + 1].toIntOrNull() ?: 0 }
        }
        return m
    }

    private fun addDayLog(xp: Int, correct: Int, answered: Int) {
        val today = Dates.today()
        val m = readLog()
        val a = m.getOrPut(Dates.iso(today)) { IntArray(4) }
        a[0] += xp; a[1] += 1; a[2] += correct; a[3] += answered
        val cutoff = Dates.iso(today - 62L)
        val kept = m.filterKeys { it >= cutoff }
        putStr("log", kept.entries.joinToString(";") { e -> e.key + "," + e.value.joinToString(",") })
    }

    /** returns [xp, quizzes, correct, answered] for the period */
    fun periodStats(period: Period): IntArray {
        if (period == Period.ALL) {
            return intArrayOf(int("xp"), int("played"), int("correct"), int("answered"))
        }
        val today = Dates.today()
        val from: Long = when (period) {
            Period.DAILY -> today
            Period.WEEKLY -> today - 6L
            else -> today - 29L
        }
        val cutoff = Dates.iso(from)
        val out = IntArray(4)
        for ((day, a) in readLog()) {
            if (day >= cutoff) for (i in 0..3) out[i] += a[i]
        }
        return out
    }

    // ---- v1.4: konu bazlı istatistik, yanlış sorular, quiz geçmişi ----

    /** Her cevaplanan soru için çağrılır: konu başarısı ve "Yanlışlarım" listesi güncellenir. */
    fun recordAnswer(cat: Int, ok: Boolean, id: String) {
        if (cat < 0 || cat >= Cats.count()) return
        add("cat_a_$cat", 1)
        if (ok) add("cat_c_$cat", 1)
        if (id.isBlank() || id.contains("~") || id.contains(",")) return
        val list = wrongIds().toMutableList()
        if (ok) {
            list.remove(id)
        } else if (!list.contains(id)) {
            list.add(0, id)
        }
        putStr("wrong_ids", list.take(40).joinToString(","))
    }

    fun wrongIds(): List<String> = (str("wrong_ids") ?: "").split(",").filter { it.isNotBlank() }

    /** [doğru, cevaplanan] */
    fun catStats(cat: Int): IntArray = intArrayOf(int("cat_c_$cat"), int("cat_a_$cat"))

    private fun addHistory(kind: String, correct: Int, answered: Int) {
        val e = Dates.iso(Dates.today()) + "," + kind + "," + correct + "," + answered
        val old = (str("hist") ?: "").split(";").filter { it.isNotBlank() }
        putStr("hist", (listOf(e) + old).take(20).joinToString(";"))
    }

    fun history(): List<HistRow> {
        val out = mutableListOf<HistRow>()
        for (e in (str("hist") ?: "").split(";")) {
            val p = e.split(",")
            if (p.size != 4) continue
            out.add(HistRow(p[0], p[1], p[2].toIntOrNull() ?: 0, p[3].toIntOrNull() ?: 0))
        }
        return out.take(10)
    }

    fun recordQuiz(spec: QuizSpec, correct: Int, answered: Int, bonusXp: Int = 0, bonusCoins: Int = 0): Rewards {
        val per = when (spec.kind) {
            Kind.CATEGORY -> Levels.XP[spec.level - 1]
            Kind.DAILY -> 10
            Kind.PRACTICE -> 8
            Kind.TIME, Kind.SUDDEN -> 5
            Kind.DUEL -> 12
            Kind.VIDEO -> 15
        }
        var xp = correct * per + bonusXp
        var coins = correct + bonusCoins
        var passed = false
        var bonus = false
        when (spec.kind) {
            Kind.CATEGORY -> {
                passed = correct >= 3
                val k = "best_${spec.cat}_${spec.level}"
                if (correct > int(k)) put(k, correct)
                if (correct == 5) xp += 20
            }
            Kind.DAILY -> if (!dailyDone()) {
                bonus = true
                xp += 100
                coins += 20
                putStr("daily_day", Dates.iso(Dates.today()))
            }
            Kind.TIME -> best("best_time", correct)
            Kind.SUDDEN -> best("best_sudden", correct)
            Kind.VIDEO -> best("best_video", correct)
            else -> {}
        }
        add("xp", xp)
        add("coins", coins)
        add("played", 1)
        add("correct", correct)
        add("answered", answered)
        touchStreak()
        addDayLog(xp, correct, answered)
        addHistory(spec.kind.name, correct, answered)
        return Rewards(xp, coins, passed, bonus)
    }
}
