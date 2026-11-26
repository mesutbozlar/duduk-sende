package com.armeaze.duduksende

enum class Kind { CATEGORY, DAILY, PRACTICE, TIME, SUDDEN, DUEL, VIDEO }
enum class Period { DAILY, WEEKLY, MONTHLY, ALL }

data class QuizSpec(val kind: Kind, val cat: Int = 0, val level: Int = 1, val ids: List<String>? = null)
data class Rewards(val xp: Int, val coins: Int, val passed: Boolean, val dailyBonus: Boolean)
data class LbRow(val name: String, val xp: Int, val quizzes: Int, val accuracy: Int, val isMe: Boolean)

object Cats {
    val emoji = listOf("🚩", "🟨", "🔄", "🥅", "📘", "🆕")
    val enNames = listOf("Offside", "Fouls", "Restarts", "Penalties", "Basics", "What's New 26/27")
    val trNames = listOf("Ofsayt", "Fauller", "Yeniden Başlatmalar", "Penaltılar", "Temel Kurallar", "26/27 Yenilikleri")
    fun count() = emoji.size
    fun name(tr: Boolean, c: Int): String = if (tr) trNames[c] else enNames[c]
}

object Levels {
    val XP = intArrayOf(10, 15, 20, 25, 30)          // XP per correct answer
    val NEED = intArrayOf(0, 0, 300, 700, 1200)       // total XP needed to unlock
    val SECONDS = intArrayOf(0, 0, 0, 20, 12)         // seconds per question (0 = no limit)
    val COINS = intArrayOf(0, 0, 150, 300, 500)       // coins to unlock early
    val icons = listOf("🟢", "🔵", "🟠", "🔴", "🟣")
    val enNames = listOf("Easy", "Medium", "Hard", "Very hard", "Expert")
    val trNames = listOf("Kolay", "Orta", "Zor", "Çok zor", "Uzman")
    fun name(tr: Boolean, l: Int): String = if (tr) trNames[l - 1] else enNames[l - 1]
}

object Ranks {
    val need = intArrayOf(0, 500, 1500, 3000, 6000)
    val enNames = listOf("Rookie", "Linesman", "Referee", "VAR Official", "Elite Referee")
    val trNames = listOf("Çaylak", "Yan Hakem", "Hakem", "VAR Hakemi", "Elit Hakem")
    fun index(xp: Int): Int {
        var r = 0
        for (i in need.indices) if (xp >= need[i]) r = i
        return r
    }
    fun name(tr: Boolean, i: Int): String = if (tr) trNames[i] else enNames[i]
}
