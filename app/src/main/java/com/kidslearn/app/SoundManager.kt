package com.kidslearn.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * Sons du jeu, 100 % générés dans l'appli (aucun fichier audio à ajouter) :
 *  - voix : TextToSpeech d'Android (français et arabe)
 *  - effets : petites notes synthétisées (succès, erreur, clic, bulle)
 *  - musique de fond : mélodie joyeuse en boucle
 *  - vibrations légères (si le téléphone en a une)
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    /** Faux si la voix arabe n'est pas installée sur le téléphone. */
    var arabicOk by mutableStateOf(true)
        private set

    @Volatile private var musicOn = true
    @Volatile private var musicRunning = false
    @Volatile var hapticOn = true
    private var musicTrack: AudioTrack? = null
    private var musicThread: Thread? = null

    private val vibrator: Vibrator? = try {
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } catch (_: Exception) {
        null
    }

    init {
        tts = TextToSpeech(appContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.setSpeechRate(0.85f) // un peu lent pour un enfant de 5 ans
                tts?.setPitch(1.15f)
                val r = tts?.isLanguageAvailable(Lang.AR.locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                arabicOk = r >= TextToSpeech.LANG_AVAILABLE
            } else {
                arabicOk = false
            }
        }
    }

    // ---------- Voix ----------

    fun speak(text: String, lang: Lang) {
        if (!ttsReady) return
        val engine = tts ?: return
        val locale = if (lang == Lang.AR) Lang.AR.locale else Lang.FR.locale
        engine.language = locale
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kids-${System.nanoTime()}")
    }

    // ---------- Vibrations ----------

    @Suppress("DEPRECATION")
    fun vibrate(ms: Long) {
        if (!hapticOn) return
        val v = vibrator ?: return
        try {
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(ms, 80))
            } else {
                v.vibrate(ms)
            }
        } catch (_: Exception) {
        }
    }

    // ---------- Effets ----------

    fun playClick() {
        vibrate(10)
        playNotes(listOf(660.0 to 70))
    }

    fun playSuccess() {
        vibrate(35)
        playNotes(listOf(523.25 to 110, 659.25 to 110, 783.99 to 110, 1046.5 to 220))
    }

    fun playError() {
        vibrate(40)
        playNotes(listOf(330.0 to 140, 262.0 to 220))
    }

    fun playStar() {
        vibrate(15)
        playNotes(listOf(880.0 to 80, 1174.66 to 160))
    }

    fun playPop() {
        vibrate(20)
        playNotes(listOf(700.0 to 40, 1100.0 to 70))
    }

    private fun playNotes(notes: List<Pair<Double, Int>>) {
        thread(start = true, isDaemon = true) {
            val pcm = synth(notes, volume = 0.5)
            playPcm(pcm)
        }
    }

    // ---------- Musique ----------

    fun setMusic(enabled: Boolean) {
        musicOn = enabled
        if (enabled) startMusic() else stopMusic()
    }

    fun isMusicOn() = musicOn

    fun startMusic() {
        if (!musicOn || musicRunning) return
        musicRunning = true
        musicThread = thread(start = true, isDaemon = true) {
            val c = 261.63; val d = 293.66; val e = 329.63; val g = 392.0; val a = 440.0
            val melody = listOf(
                c to 300, e to 300, g to 300, e to 300,
                a to 300, g to 300, e to 300, d to 300,
                c to 300, d to 300, e to 300, g to 300,
                e to 300, d to 300, c to 600
            )
            val pcm = synth(melody, volume = 0.12)
            val track = buildTrack(pcm)
            musicTrack = track
            track.setLoopPoints(0, pcm.size, -1)
            track.play()
        }
    }

    fun stopMusic() {
        musicRunning = false
        try {
            musicTrack?.stop()
            musicTrack?.release()
        } catch (_: Exception) {
        }
        musicTrack = null
    }

    fun pauseMusic() {
        try { musicTrack?.pause() } catch (_: Exception) {}
    }

    fun resumeMusic() {
        if (!musicOn) return
        if (musicTrack == null) { musicRunning = false; startMusic() }
        else try { musicTrack?.play() } catch (_: Exception) {}
    }

    fun release() {
        stopMusic()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    // ---------- Synthèse ----------

    private val sampleRate = 22050

    private fun synth(notes: List<Pair<Double, Int>>, volume: Double): ShortArray {
        val total = notes.sumOf { (it.second * sampleRate) / 1000 }
        val out = ShortArray(total)
        var pos = 0
        for ((freq, ms) in notes) {
            val n = (ms * sampleRate) / 1000
            for (i in 0 until n) {
                val t = i.toDouble() / sampleRate
                // enveloppe douce pour éviter les "clics"
                val attack = minOf(1.0, i / (sampleRate * 0.01))
                val release = minOf(1.0, (n - i) / (sampleRate * 0.04))
                val env = attack * release
                val v = sin(2 * PI * freq * t) * 0.8 + sin(2 * PI * freq * 2 * t) * 0.2
                out[pos + i] = (v * env * volume * Short.MAX_VALUE).toInt().toShort()
            }
            pos += n
        }
        return out
    }

    private fun buildTrack(pcm: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        return track
    }

    private fun playPcm(pcm: ShortArray) {
        try {
            val track = buildTrack(pcm)
            track.play()
            Thread.sleep((pcm.size * 1000L / sampleRate) + 80)
            track.release()
        } catch (_: Exception) {
        }
    }
}
