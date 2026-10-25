package com.kidslearn.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
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
        // Moteur Google en priorité (meilleure voix arabe) ; sinon moteur par défaut du téléphone.
        startEngine(preferGoogle = true)
    }

    private fun startEngine(preferGoogle: Boolean) {
        ttsReady = false
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                onEngineReady()
            } else if (preferGoogle) {
                try { tts?.shutdown() } catch (_: Exception) {}
                startEngine(preferGoogle = false) // Google TTS absent : moteur par défaut
            } else {
                ttsReady = false
                arabicOk = false
            }
        }
        tts = try {
            if (preferGoogle) TextToSpeech(appContext, listener, "com.google.android.tts")
            else TextToSpeech(appContext, listener)
        } catch (_: Exception) {
            null
        }
        if (tts == null && preferGoogle) startEngine(preferGoogle = false)
    }

    private fun onEngineReady() {
        ttsReady = true
        tts?.setPitch(1.1f)
        voiceCache.clear()
        val r = tts?.isLanguageAvailable(Lang.AR.locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        arabicOk = r >= TextToSpeech.LANG_AVAILABLE
        installListener()
    }

    /** Choisit la meilleure voix installée pour une langue : hors-ligne, haute qualité, bien installée. */
    private val voiceCache = HashMap<String, Voice?>()

    private fun bestVoice(engine: TextToSpeech, lang: Lang): Voice? {
        val code = lang.locale.language
        return voiceCache.getOrPut(code) {
            try {
                val all = engine.voices?.filter { it.locale.language == code } ?: emptyList()
                all.sortedWith(
                    compareBy<Voice>(
                        { it.features?.contains("notInstalled") == true },
                        { it.isNetworkConnectionRequired },
                        { -it.quality }
                    )
                ).firstOrNull()
            } catch (_: Exception) {
                null
            }
        }
    }

    // ---------- Voix ----------

    private val pending = ArrayDeque<Pair<String, Lang>>()
    private val speechLock = Any()
    @Volatile private var generation = 0

    private fun installListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}
            override fun onDone(utteranceId: String) { next(utteranceId) }
            override fun onError(utteranceId: String) { next(utteranceId) }
        })
    }

    private fun next(utteranceId: String) {
        val g = utteranceId.substringBefore('-').removePrefix("g").toIntOrNull() ?: return
        if (g != generation) return // ancienne phrase interrompue : on l'ignore
        speakNext(g)
    }

    private fun speakNext(g: Int) {
        val engine = tts ?: return
        val seg: Pair<String, Lang>
        synchronized(speechLock) {
            if (g != generation) return
            seg = pending.removeFirstOrNull() ?: return
        }
        val (text, lang) = seg
        val avail = engine.isLanguageAvailable(lang.locale)
        if (avail < TextToSpeech.LANG_AVAILABLE) {
            if (lang == Lang.AR) arabicOk = false
            speakNext(g) // voix absente : on passe au morceau suivant
            return
        }
        engine.language = lang.locale
        bestVoice(engine, lang)?.let { engine.setVoice(it) }
        engine.setSpeechRate(if (lang == Lang.AR) 0.8f else 0.85f)
        val r = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "g$g-${System.nanoTime()}")
        if (r != TextToSpeech.SUCCESS) speakNext(g)
    }

    /**
     * Lit un texte à voix haute. Le texte est nettoyé (emojis retirés) puis découpé par
     * écriture : l'arabe est lu avec la voix arabe, le latin avec la voix française ou anglaise.
     * [lang] indique la langue voulue ; en mode MIX le français sert pour le latin.
     */
    fun speak(text: String, lang: Lang) {
        if (!ttsReady) return
        val engine = tts ?: return
        val segs = splitSegments(text, lang)
        val g: Int
        synchronized(speechLock) {
            generation++
            g = generation
            pending.clear()
            pending.addAll(segs)
        }
        try { engine.stop() } catch (_: Exception) {}
        speakNext(g)
    }

    fun stopSpeech() {
        synchronized(speechLock) { generation++; pending.clear() }
        try { tts?.stop() } catch (_: Exception) {}
    }

    /** Les chiffres 0-10 sont remplacés par leur mot : la voix ne se trompe plus de langue. */
    private fun digitsToWords(text: String, lang: Lang): String =
        Regex("\\d+").replace(text) { m ->
            val n = m.value.toIntOrNull()
            if (n != null && n in 0..10) Content.digitsFor(lang)[n].spoken else m.value
        }

    private fun isArabicChar(c: Char): Boolean =
        c in '\u0600'..'\u06FF' || c in '\u0750'..'\u077F' ||
            c in '\uFB50'..'\uFDFF' || c in '\uFE70'..'\uFEFF'

    private fun splitSegments(raw: String, hint: Lang): List<Pair<String, Lang>> {
        val latin = if (hint == Lang.EN) Lang.EN else Lang.FR
        val out = ArrayList<Pair<String, Lang>>()
        var sb = StringBuilder()
        var cur: Lang? = null
        fun flush() {
            val txt = sb.toString().trim()
            if (txt.any { it.isLetterOrDigit() }) {
                val l = cur ?: if (hint == Lang.AR) Lang.AR else latin
                out.add(digitsToWords(txt, l) to l)
            }
            sb = StringBuilder()
            cur = null
        }
        for (ch in raw) {
            if (ch == '\u0640' || ch == '\u200D' || ch in '\uFE00'..'\uFE0F') continue
            val isMark = Character.getType(ch) == Character.NON_SPACING_MARK.toInt()
            val keep = ch.isLetterOrDigit() || ch.isWhitespace() || isMark || ch in ".,!?;:'’-،؟"
            if (!keep) continue
            val script: Lang? = when {
                isMark -> null
                isArabicChar(ch) && ch.isLetter() -> Lang.AR
                ch.isLetter() -> latin
                else -> null
            }
            if (script != null) {
                if (cur != null && cur != script) flush()
                cur = script
            }
            sb.append(ch)
        }
        flush()
        return out
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
        stopSpeech()
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
