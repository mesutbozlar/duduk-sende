package com.armeaze.duduksende

import android.content.Context

/*
 * PLATFORM KATMANI
 * ----------------
 * Telefona (Android / ileride iPhone) özel her şey bu dosyada ve Sfx.kt / Ui.kt (shareText) içinde toplanmıştır.
 * Geri kalan kodun (soru motoru, ekranlar, video analizi) hiçbir yerinde "android." veya "java." yoktur;
 * böylece iPhone sürümüne geçerken yalnızca bu küçük katman yeniden yazılır.
 */

/** Basit anahtar-değer deposu. Android'de SharedPreferences, iPhone'da NSUserDefaults olacak. */
interface KV {
    fun has(k: String): Boolean
    fun getInt(k: String, d: Int): Int
    fun putInt(k: String, v: Int)
    fun getString(k: String): String?
    fun putString(k: String, v: String)
}

class AndroidKV(ctx: Context) : KV {
    private val p = ctx.getSharedPreferences("whistlequiz", Context.MODE_PRIVATE)
    override fun has(k: String) = p.contains(k)
    override fun getInt(k: String, d: Int) = p.getInt(k, d)
    override fun putInt(k: String, v: Int) { p.edit().putInt(k, v).apply() }
    override fun getString(k: String): String? = p.getString(k, null)
    override fun putString(k: String, v: String) { p.edit().putString(k, v).apply() }
}

/** Platforma özel iki küçük işlev. */
object Plat {
    fun systemLang(): String = java.util.Locale.getDefault().language
    fun todayEpochDay(): Long = java.time.LocalDate.now().toEpochDay()
}

/** Tarih işlemleri (saf Kotlin). Gün numarası: 1970-01-01 = 0. Metin biçimi: yyyy-MM-dd. */
object Dates {
    fun today(): Long = Plat.todayEpochDay()

    fun iso(epochDay: Long): String {
        val z = epochDay + 719468L
        val era = (if (z >= 0) z else z - 146096L) / 146097L
        val doe = z - era * 146097L
        val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
        val y = yoe + era * 400L
        val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
        val mp = (5L * doy + 2L) / 153L
        val d = doy - (153L * mp + 2L) / 5L + 1L
        val m = if (mp < 10L) mp + 3L else mp - 9L
        val yy = if (m <= 2L) y + 1L else y
        return yy.toString().padStart(4, '0') + "-" + m.toString().padStart(2, '0') + "-" + d.toString().padStart(2, '0')
    }

    fun parse(s: String): Long? {
        val p = s.split("-")
        if (p.size != 3) return null
        val yy = p[0].toLongOrNull() ?: return null
        val m = p[1].toLongOrNull() ?: return null
        val d = p[2].toLongOrNull() ?: return null
        if (m !in 1L..12L || d !in 1L..31L) return null
        val y = if (m <= 2L) yy - 1L else yy
        val era = (if (y >= 0) y else y - 399L) / 400L
        val yoe = y - era * 400L
        val mm = if (m > 2L) m - 3L else m + 9L
        val doy = (153L * mm + 2L) / 5L + d - 1L
        val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
        return era * 146097L + doe - 719468L
    }
}
