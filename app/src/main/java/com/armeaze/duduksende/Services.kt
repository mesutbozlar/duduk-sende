package com.armeaze.duduksende

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

// =====================================================================
//  ONLINE LEADERBOARD HOOK
//  Today: LocalLeaderboardRepo (this device only).
//  Later: write FirebaseLeaderboardRepo : LeaderboardRepo and pass it in MainActivity.
// =====================================================================
interface LeaderboardRepo {
    val isOnline: Boolean
    suspend fun submit(name: String, xp: Int, correct: Int, answered: Int)
    suspend fun top(period: Period): List<LbRow>
}

class LocalLeaderboardRepo(private val store: Store) : LeaderboardRepo {
    override val isOnline: Boolean = false
    override suspend fun submit(name: String, xp: Int, correct: Int, answered: Int) {}
    override suspend fun top(period: Period): List<LbRow> {
        val s = store.periodStats(period)
        val acc = if (s[3] > 0) s[2] * 100 / s[3] else 0
        return listOf(LbRow(store.name, s[0], s[1], acc, true))
    }
}

// =====================================================================
//  ADS HOOK  (AdMob later: implement AdProvider, set available = true)
// =====================================================================
interface AdProvider {
    val available: Boolean
    fun showRewarded(onResult: (Boolean) -> Unit)
}

class NoAdProvider : AdProvider {
    override val available: Boolean = false
    override fun showRewarded(onResult: (Boolean) -> Unit) { onResult(false) }
}

// =====================================================================
//  DUEL HOOK
//  Both players get the SAME question ids; each client renders the question
//  in its own language (Q has TR + EN text), so a Turkish and an English
//  player can duel on the same question.
//  Today: BotDuelRepo (practice bot). Later: FirebaseDuelRepo : DuelRepo
//  (matchmaking queue + realtime result exchange).
// =====================================================================
data class DuelMatch(
    val opponentName: String,
    val opponentLang: String,
    val questionIds: List<String>,
    val opponentScore: Int?
)

interface DuelRepo {
    val isOnline: Boolean
    suspend fun findMatch(myName: String, myLang: String): DuelMatch
}

class BotDuelRepo : DuelRepo {
    override val isOnline: Boolean = false
    override suspend fun findMatch(myName: String, myLang: String): DuelMatch {
        delay(1800)
        val r = Random(System.nanoTime())
        val bots = listOf("Mehmet", "James", "Ayşe", "Oliver", "Emre", "Sophie")
        val i = r.nextInt(bots.size)
        val lang = if (i % 2 == 0) "tr" else "en"
        return DuelMatch(bots[i], lang, Questions.duelIds(r.nextInt(1_000_000).toLong()), r.nextInt(2, 6))
    }
}

// =====================================================================
//  AI QUESTION VARIATION HOOK
//  The app never contains an API key. It calls YOUR proxy (see tools/ai-proxy),
//  which holds the key and asks Claude to rewrite a base question as a fresh variant.
//  Set AiConfig.PROXY_URL to enable. Empty = disabled (base questions are used).
// =====================================================================
object AiConfig {
    const val PROXY_URL = ""   // e.g. "https://duduk-ai.yourname.workers.dev"
    const val TOKEN = ""       // optional shared secret sent as "Bearer <TOKEN>"
}

interface QuestionVariator {
    val enabled: Boolean
    suspend fun variant(base: Q): Q?
}

class NoVariator : QuestionVariator {
    override val enabled: Boolean = false
    override suspend fun variant(base: Q): Q? = null
}

class ProxyVariator(private val url: String, private val token: String) : QuestionVariator {
    override val enabled: Boolean = url.isNotBlank()
    override suspend fun variant(base: Q): Q? = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("qEn", base.qEn); put("qTr", base.qTr)
                put("oEn", JSONArray(base.oEn)); put("oTr", JSONArray(base.oTr))
                put("ans", base.ans); put("wEn", base.wEn); put("wTr", base.wTr)
                put("cat", base.cat); put("diff", base.diff)
            }
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 6000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                if (token.isNotBlank()) setRequestProperty("Authorization", "Bearer $token")
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            if (conn.responseCode != 200) return@withContext null
            val txt = conn.inputStream.bufferedReader().use { it.readText() }
            val q = Questions.parse(JSONObject(txt))
            if (q != null) {
                q.id = base.id + "~ai"
                q.cat = base.cat
                q.diff = base.diff
            }
            q
        } catch (e: Exception) {
            null
        }
    }
}

class AppServices(
    val leaderboard: LeaderboardRepo,
    val ads: AdProvider,
    val duel: DuelRepo,
    val ai: QuestionVariator
)
