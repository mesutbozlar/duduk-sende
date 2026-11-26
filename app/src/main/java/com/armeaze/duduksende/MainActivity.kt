package com.armeaze.duduksende

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

sealed class Screen {
    object Onboarding : Screen()
    object Main : Screen()
    object DuelLobby : Screen()
    object Progress : Screen()
    object Creator : Screen()
    data class RuleBook(val law: Int) : Screen()
    data class Levels(val cat: Int) : Screen()
    data class Quiz(val spec: QuizSpec, val run: Int) : Screen()
    data class Result(val spec: QuizSpec, val correct: Int, val answered: Int, val rw: Rewards) : Screen()
    data class DuelResult(val match: DuelMatch, val my: Int, val rw: Rewards) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(AndroidKV(this))
        Sfx.init(this, store)
        try {
            val txt = assets.open("questions.json").bufferedReader().use { it.readText() }
            Questions.loadFromText(txt)
        } catch (e: Exception) {
        }
        // Swap these later for the real online leaderboard / ads / duel / AI (see Services.kt)
        val services = AppServices(
            leaderboard = LocalLeaderboardRepo(store),
            ads = NoAdProvider(),
            duel = BotDuelRepo(),
            ai = if (AiConfig.PROXY_URL.isNotBlank()) ProxyVariator(AiConfig.PROXY_URL, AiConfig.TOKEN) else NoVariator()
        )
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = C.Yellow,
                    onPrimary = Color(0xFF1A1A1A),
                    background = C.Navy,
                    onBackground = Color.White,
                    surface = C.Navy,
                    onSurface = Color.White,
                    surfaceVariant = C.Card
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = C.Navy, contentColor = Color.White) {
                    App(store, services)
                }
            }
        }
    }
}

@Composable
fun App(store: Store, services: AppServices) {
    var lang by remember { mutableStateOf(store.lang) }
    var screen by remember { mutableStateOf<Screen>(if (store.onboarded) Screen.Main else Screen.Onboarding) }
    var tab by remember { mutableStateOf(0) }
    var run by remember { mutableStateOf(0) }
    var duel by remember { mutableStateOf<DuelMatch?>(null) }
    val scope = rememberCoroutineScope()
    val tr = lang == "tr"
    val setLang: (String) -> Unit = { l -> lang = l; store.lang = l }

    fun start(spec: QuizSpec) {
        run += 1
        screen = Screen.Quiz(spec, run)
    }

    BackHandler(enabled = screen !is Screen.Main) {
        val s = screen
        screen = if (s is Screen.Quiz && s.spec.kind == Kind.CATEGORY) Screen.Levels(s.spec.cat) else Screen.Main
    }

    when (val s = screen) {
        is Screen.Onboarding -> OnboardingScreen(tr, setLang) {
            store.onboarded = true
            screen = Screen.Main
        }
        is Screen.Main -> MainScreen(
            tr, store, services, tab, { tab = it }, setLang,
            onSpec = { start(it) },
            onCat = { screen = Screen.Levels(it) },
            onDuel = { screen = Screen.DuelLobby },
            onCreator = { screen = Screen.Creator },
            onRules = { screen = Screen.RuleBook(-1) },
            onProgress = { screen = Screen.Progress }
        )
        is Screen.Progress -> ProgressScreen(tr, store, onBack = { screen = Screen.Main }, onSpec = { start(it) })
        is Screen.Creator -> CreatorScreen(tr, onBack = { screen = Screen.Main }, onSpec = { start(it) })
        is Screen.RuleBook -> RulesScreen(tr, s.law, onBack = { screen = Screen.Main })
        is Screen.DuelLobby -> DuelLobbyScreen(
            tr, store, services,
            onBack = { screen = Screen.Main },
            onStart = { m ->
                duel = m
                start(QuizSpec(Kind.DUEL, ids = m.questionIds))
            }
        )
        is Screen.Levels -> LevelsScreen(
            tr, store, services, s.cat,
            onBack = { screen = Screen.Main },
            onPlay = { l -> start(QuizSpec(Kind.CATEGORY, s.cat, l)) }
        )
        is Screen.Quiz -> key(s) {
            val exit: () -> Unit = {
                screen = if (s.spec.kind == Kind.CATEGORY) Screen.Levels(s.spec.cat) else Screen.Main
            }
            val finish: (Int, Int) -> Unit = { c, a ->
                val m = duel
                val isDuel = s.spec.kind == Kind.DUEL && m != null
                var bonusXp = 0
                var bonusCoins = 0
                if (isDuel && m != null) {
                    val opp = m.opponentScore ?: 0
                    if (c > opp) { bonusXp = 40; bonusCoins = 15 }
                    else if (c == opp) { bonusXp = 20; bonusCoins = 5 }
                    else bonusXp = 10
                }
                val rw = store.recordQuiz(s.spec, c, a, bonusXp, bonusCoins)
                scope.launch { services.leaderboard.submit(store.name, rw.xp, c, a) }
                screen = if (isDuel && m != null) Screen.DuelResult(m, c, rw) else Screen.Result(s.spec, c, a, rw)
            }
            if (s.spec.kind == Kind.VIDEO) {
                VideoQuizScreen(tr, exit, finish)
            } else {
                QuizScreen(tr, store, services, s.spec, onExit = exit, onFinish = finish)
            }
        }
        is Screen.Result -> {
            val canNext = s.spec.kind == Kind.CATEGORY && s.rw.passed && s.spec.level < 5 &&
                store.levelUnlocked(s.spec.cat, s.spec.level + 1)
            Box(Modifier.fillMaxSize()) {
                ResultScreen(
                    tr, store, s.spec, s.correct, s.answered, s.rw, canNext,
                    onAgain = { start(s.spec) },
                    onNext = { start(QuizSpec(Kind.CATEGORY, s.spec.cat, s.spec.level + 1)) },
                    onBack = {
                        screen = if (s.spec.kind == Kind.CATEGORY) Screen.Levels(s.spec.cat) else Screen.Main
                    }
                )
                if (s.answered > 0 && s.correct * 100 / s.answered >= 60) Confetti()
            }
        }
        is Screen.DuelResult -> DuelResultScreen(
            tr, store, s.match, s.my, s.rw,
            onAgain = { screen = Screen.DuelLobby },
            onBack = { screen = Screen.Main }
        )
    }
}
