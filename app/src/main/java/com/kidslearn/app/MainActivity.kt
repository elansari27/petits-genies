package com.kidslearn.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
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

            // Les étoiles gagnées servent de monnaie : solde = gagnées - dépensées
            val balance = earned - spent

            fun addStar() {
                earned += 1
                prefs.edit().putInt("stars", earned).apply()
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

            BackHandler(enabled = screen != Screen.HOME) {
                screen = if (screen == Screen.MENU) Screen.HOME else Screen.MENU
            }

            // Arabe : toute l'interface se lit de droite à gauche
            // Exception : les jeux "géométriques" (relier, glisser, tracer...) restent en gauche-droite,
            // car Compose inverse les offset() en RTL alors que les doigts/Canvas restent absolus.
            val geometric = screen == Screen.CONNECT || screen == Screen.MATCH ||
                screen == Screen.BASKET || screen == Screen.TRACE ||
                screen == Screen.TREASURE || screen == Screen.BUBBLES
            val direction =
                if (lang == Lang.AR && screen != Screen.HOME && !geometric) LayoutDirection.Rtl
                else LayoutDirection.Ltr

            KidsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
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
                                screen = it
                            }
                        )

                        Screen.LEARN_LETTERS -> LearnScreen(
                            items = Content.letters(lang),
                            lang = lang,
                            sound = sound,
                            onBack = { screen = Screen.MENU },
                            onTest = { screen = Screen.QUIZ_LETTERS }
                        )

                        Screen.LEARN_NUMBERS -> LearnScreen(
                            items = Content.numbers(lang),
                            lang = lang,
                            sound = sound,
                            onBack = { screen = Screen.MENU },
                            onTest = { screen = Screen.QUIZ_NUMBERS }
                        )

                        Screen.HIDE_LETTERS -> HideSeekScreen(
                            pool = Content.letters(lang),
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.ODD -> OddOneOutScreen(lang, sound, balance, profile.name, { addStar() }, { screen = Screen.MENU })
                        Screen.SORT -> SortScreen(lang, sound, balance, profile.name, { addStar() }, { screen = Screen.MENU })
                        Screen.LOGIC -> LogicScreen(lang, sound, balance, profile.name, { addStar() }, { screen = Screen.MENU })
                        Screen.SHADOW -> ShadowScreen(lang, sound, balance, profile.name, { addStar() }, { screen = Screen.MENU })

                        Screen.HIDE_NUMBERS -> HideSeekScreen(
                            pool = Content.numbers(lang),
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        // 4-5 ans : 3 propositions au lieu de 4 (moins de charge mentale)
                        Screen.QUIZ_LETTERS -> QuizScreen(
                            pool = Content.letters(lang),
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            distractors = if (profile.age <= 5) 2 else 3,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.QUIZ_NUMBERS -> QuizScreen(
                            pool = Content.numbers(lang),
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            distractors = if (profile.age <= 5) 2 else 3,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.COUNT -> CountScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        // ---- Nouveaux jeux adaptés à 4-5 ans ----
                        Screen.COLORS -> ColorsGameScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.SHAPES -> ShapesScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.MEMORY -> MemoryScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.FIRST_LETTER -> FirstLetterScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
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
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.BIG_SMALL -> BigSmallScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.CONNECT -> ConnectScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.BASKET -> BasketScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.TRACE -> TraceScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            childName = profile.name,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.MATCH -> MatchScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.STORY -> StoryScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.TREASURE -> TreasureScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            onStar = { addStar() },
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.BUBBLES -> BubblesScreen(
                            lang = lang,
                            sound = sound,
                            stars = balance,
                            onBack = { screen = Screen.MENU }
                        )

                        Screen.STICKERS -> StickerScreen(
                            lang = lang,
                            sound = sound,
                            balance = balance,
                            owned = owned,
                            onBuy = { buy(it) },
                            onBack = { screen = Screen.MENU }
                        )
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

enum class Screen {
    HOME, MENU,
    LEARN_LETTERS, LEARN_NUMBERS, QUIZ_LETTERS, QUIZ_NUMBERS, COUNT,
    COLORS, SHAPES, MEMORY, FIRST_LETTER, PARENTS,
    BIG_SMALL, CONNECT, BASKET,
    TRACE, MATCH, STORY, TREASURE, BUBBLES, STICKERS,
    HIDE_LETTERS, HIDE_NUMBERS, ODD, SORT, LOGIC, SHADOW
}
