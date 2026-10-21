package com.kidslearn.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            var stars by remember { mutableIntStateOf(prefs.getInt("stars", 0)) }
            var musicOn by remember { mutableStateOf(prefs.getBoolean("music", true)) }

            fun addStar() {
                stars += 1
                prefs.edit().putInt("stars", stars).apply()
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

            KidsTheme {
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        stars = stars,
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
                        stars = stars,
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
                        onBack = { screen = Screen.MENU }
                    )

                    Screen.LEARN_NUMBERS -> LearnScreen(
                        items = Content.numbers(lang),
                        lang = lang,
                        sound = sound,
                        onBack = { screen = Screen.MENU }
                    )

                    Screen.QUIZ_LETTERS -> QuizScreen(
                        pool = Content.letters(lang),
                        lang = lang,
                        sound = sound,
                        stars = stars,
                        onStar = { addStar() },
                        onBack = { screen = Screen.MENU }
                    )

                    Screen.QUIZ_NUMBERS -> QuizScreen(
                        pool = Content.numbers(lang),
                        lang = lang,
                        sound = sound,
                        stars = stars,
                        onStar = { addStar() },
                        onBack = { screen = Screen.MENU }
                    )

                    Screen.COUNT -> CountScreen(
                        lang = lang,
                        sound = sound,
                        stars = stars,
                        onStar = { addStar() },
                        onBack = { screen = Screen.MENU }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sound.release()
    }
}

enum class Screen { HOME, MENU, LEARN_LETTERS, LEARN_NUMBERS, QUIZ_LETTERS, QUIZ_NUMBERS, COUNT }
