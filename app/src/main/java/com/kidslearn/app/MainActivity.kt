package com.kidslearn.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {

    private lateinit var sound: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sound = SoundManager(this)
        val prefs = getSharedPreferences("kids", Context.MODE_PRIVATE)

        setContent {
            var screen by remember { mutableStateOf(Screen.HOME) }
            var lang by remember { mutableStateOf(Lang.FR) }
            var earned by remember { mutableIntStateOf(prefs.getInt("stars", 0)) }
            var spent by remember { mutableIntStateOf(prefs.getInt("spent", 0)) }
            var owned by remember {
                mutableStateOf<Set<String>>(prefs.getStringSet("stickers", emptySet())?.toSet() ?: emptySet())
            }
            var musicOn by remember { mutableStateOf(prefs.getBoolean("music", true)) }
            var profile by remember { mutableStateOf(ProfileStore.load(this@MainActivity)) }
            var streak by remember { mutableIntStateOf(ProfileStore.touchStreak(this@MainActivity)) }
            var buddy by remember { mutableStateOf(prefs.getString("buddy", "🦁") ?: "🦁") }

            // Mode « Jouer » : l'appli enchaîne des jeux tirés au hasard (3 étoiles par jeu)
            var autoMode by remember { mutableStateOf(false) }
            var autoStars by remember { mutableIntStateOf(0) }
            var switching by remember { mutableStateOf(false) }
            var showBanner by remember { mutableStateOf(false) }
            var history by remember { mutableStateOf(listOf<Screen>()) }
            var showExit by remember { mutableStateOf(false) }

            // Les étoiles gagnées servent de monnaie : solde = gagnées - dépensées
            val balance = earned - spent

            fun nextAuto(): Screen {
                // évite de rejouer l'un des 4 derniers jeux : on varie !
                val candidates = autoGames.filter { it !in history.takeLast(4) }.ifEmpty { autoGames }
                val s = candidates.random()
                history = (history + s).takeLast(8)
                return s
            }

            fun addStar() {
                earned += 1
                prefs.edit().putInt("stars", earned).apply()
                if (autoMode && !switching) {
                    autoStars += 1
                    if (autoStars >= 3) switching = true
                }
            }

            LaunchedEffect(switching) {
                if (switching) {
                    delay(1700)          // laisse finir la célébration
                    showBanner = true
                    delay(1400)
                    autoStars = 0
                    screen = nextAuto()
                    showBanner = false
                    switching = false
                }
            }

            fun buy(s: Sticker) {
                if (s.id !in owned && balance >= s.price) {
                    spent += s.price
                    owned = owned + s.id
                    prefs.edit()
                        .putInt("spent", spent)
                        .putStringSet("stickers", owned)
                        .apply()
                }
            }

            val back: () -> Unit = {
                autoMode = false
                switching = false
                showBanner = false
                screen = Screen.MENU
            }

            // Musique : suit le cycle de vie de l'appli
            DisposableEffect(Unit) {
                sound.setMusic(musicOn)
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> sound.pauseMusic()
                        Lifecycle.Event.ON_RESUME -> sound.resumeMusic()
                        else -> {}
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            // Retour : jeu -> menu -> accueil -> question « Tu veux quitter ? »
            BackHandler(enabled = true) {
                when {
                    showExit -> showExit = false
                    screen == Screen.HOME -> showExit = true
                    screen == Screen.MENU -> screen = Screen.HOME
                    else -> back()
                }
            }

            // Arabe : toute l'interface se lit de droite à gauche
            // Exception : les jeux "géométriques" (relier, tracer, moitiés...) restent en gauche-droite,
            // car Compose inverse les offset() en RTL alors que les doigts/Canvas restent absolus.
            val geometric = screen == Screen.CONNECT || screen == Screen.MATCH ||
                screen == Screen.BASKET || screen == Screen.TRACE ||
                screen == Screen.TREASURE || screen == Screen.BUBBLES || screen == Screen.HALVES
            val direction =
                if (lang == Lang.AR && screen != Screen.HOME && !geometric) LayoutDirection.Rtl
                else LayoutDirection.Ltr

            KidsTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides direction,
                    LocalSound provides sound,
                    LocalBuddy provides buddy
                ) {
                    Box(Modifier.fillMaxSize()) {
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            stars = balance,
                            streak = streak,
                            childName = profile.name,
                            avatar = profile.avatar,
                            sound = sound,
                            musicOn = musicOn,
                            onToggleMusic = {
                                musicOn = !musicOn
                                prefs.edit().putBoolean("music", musicOn).apply()
                                sound.setMusic(musicOn)
                            },
                            buddy = buddy,
                            onPickBuddy = {
                                buddy = it
                                prefs.edit().putString("buddy", it).apply()
                                sound.playPop()
                            },
                            onPickLang = {
                                sound.playClick()
                                lang = it
                                screen = Screen.MENU
                            }
                        )

                        Screen.MENU -> MenuScreen(
                            lang = lang,
                            stars = balance,
                            sound = sound,
                            onBack = { screen = Screen.HOME },
                            onPick = {
                                sound.playClick()
                                if (it == Screen.AUTO) {
                                    autoMode = true
                                    autoStars = 0
                                    screen = nextAuto()
                                } else {
                                    screen = it
                                }
                            }
                        )

                        Screen.AUTO -> {}

                        Screen.LEARN_LETTERS -> LearnScreen(
                            items = Content.letters(lang), lang = lang, sound = sound,
                            onBack = back, onTest = { screen = Screen.QUIZ_LETTERS }
                        )

                        Screen.LEARN_NUMBERS -> LearnScreen(
                            items = Content.numbers(lang), lang = lang, sound = sound,
                            onBack = back, onTest = { screen = Screen.QUIZ_NUMBERS }
                        )

                        Screen.ANIM_LEARN -> LearnScreen(
                            items = animalItems(lang), lang = lang, sound = sound,
                            onBack = back, onTest = { screen = Screen.ANIM_QUIZ }
                        )

                        // 4-5 ans : 3 propositions au lieu de 4 (moins de charge mentale)
                        Screen.QUIZ_LETTERS -> QuizScreen(
                            pool = Content.letters(lang), lang = lang, sound = sound, stars = balance,
                            distractors = if (profile.age <= 5) 2 else 3, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.QUIZ_NUMBERS -> QuizScreen(
                            pool = Content.numbers(lang), lang = lang, sound = sound, stars = balance,
                            distractors = if (profile.age <= 5) 2 else 3, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.ANIM_QUIZ -> QuizScreen(
                            pool = animalItems(lang), lang = lang, sound = sound, stars = balance,
                            distractors = 2, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.COUNT -> CountScreen(
                            lang = lang, sound = sound, stars = balance, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.COLORS -> ColorsGameScreen(
                            lang = lang, sound = sound, stars = balance, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.SHAPES -> ShapesScreen(
                            lang = lang, sound = sound, stars = balance, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.MEMORY -> MemoryScreen(
                            lang = lang, sound = sound, stars = balance, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.FIRST_LETTER -> FirstLetterScreen(
                            lang = lang, sound = sound, stars = balance, childName = profile.name,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.PARENTS -> ParentScreen(
                            lang = lang,
                            sound = sound,
                            profile = profile,
                            balance = balance,
                            stickersOwned = owned.size,
                            streak = streak,
                            onSaveProfile = {
                                profile = it
                                ProfileStore.save(this@MainActivity, it)
                            },
                            onResetProgress = {
                                earned = 0
                                spent = 0
                                owned = emptySet()
                                streak = 0
                                prefs.edit()
                                    .putInt("stars", 0)
                                    .putInt("spent", 0)
                                    .putStringSet("stickers", emptySet())
                                    .apply()
                                ProfileStore.resetProgress(this@MainActivity)
                            },
                            onOpenVoiceSettings = { openVoiceSettings(this@MainActivity) },
                            onBack = back
                        )

                        Screen.BIG_SMALL -> BigSmallScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.CONNECT -> ConnectScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.BASKET -> BasketScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.TRACE -> TraceScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.MATCH -> MatchScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.ODD -> OddOneOutScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.SORT -> SortScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.LOGIC -> LogicScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.SHADOW -> ShadowScreen(lang, sound, balance, profile.name, { addStar() }, back)
                        Screen.HALVES -> HalvesScreen(lang, sound, balance, profile.name, { addStar() }, back)

                        Screen.HIDE_LETTERS -> HideSeekScreen(
                            pool = Content.letters(lang), lang = lang, sound = sound, stars = balance,
                            childName = profile.name, onStar = { addStar() }, onBack = back
                        )
                        Screen.HIDE_NUMBERS -> HideSeekScreen(
                            pool = Content.numbers(lang), lang = lang, sound = sound, stars = balance,
                            childName = profile.name, onStar = { addStar() }, onBack = back
                        )
                        Screen.HIDE_ANIMALS -> HideSeekScreen(
                            pool = animalItems(lang), lang = lang, sound = sound, stars = balance,
                            childName = profile.name, onStar = { addStar() }, onBack = back
                        )

                        Screen.STORY -> StoryScreen(
                            lang = lang, sound = sound, stars = balance,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.TREASURE -> TreasureScreen(
                            lang = lang, sound = sound, stars = balance,
                            onStar = { addStar() }, onBack = back
                        )

                        Screen.BUBBLES -> BubblesScreen(
                            lang = lang, sound = sound, stars = balance, onBack = back
                        )

                        Screen.STICKERS -> StickerScreen(
                            lang = lang, sound = sound, balance = balance, owned = owned,
                            onBuy = { buy(it) }, onBack = back
                        )
                    }
                    if (showBanner) SwitchBanner()
                    if (showExit) ExitDialog(onYes = { finish() }, onNo = { showExit = false })
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sound.release()
    }
}

/** Les jeux proposés dans le mode « Jouer » (tirage au sort, sans répétition proche). */
private val autoGames = listOf(
    Screen.QUIZ_LETTERS, Screen.QUIZ_NUMBERS, Screen.ANIM_QUIZ, Screen.COUNT, Screen.COLORS,
    Screen.SHAPES, Screen.MEMORY, Screen.FIRST_LETTER, Screen.BIG_SMALL, Screen.CONNECT,
    Screen.BASKET, Screen.TRACE, Screen.MATCH, Screen.HIDE_LETTERS, Screen.HIDE_NUMBERS,
    Screen.HIDE_ANIMALS, Screen.ODD, Screen.SORT, Screen.LOGIC, Screen.SHADOW, Screen.HALVES
)

enum class Screen {
    HOME, MENU, AUTO,
    LEARN_LETTERS, LEARN_NUMBERS, ANIM_LEARN, QUIZ_LETTERS, QUIZ_NUMBERS, ANIM_QUIZ, COUNT,
    COLORS, SHAPES, MEMORY, FIRST_LETTER, PARENTS,
    BIG_SMALL, CONNECT, BASKET,
    TRACE, MATCH, STORY, TREASURE, BUBBLES, STICKERS,
    HIDE_LETTERS, HIDE_NUMBERS, HIDE_ANIMALS, ODD, SORT, LOGIC, SHADOW, HALVES
}
