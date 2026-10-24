package com.kidslearn.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Cache-cache : des lettres (ou des chiffres) déjà appris se cachent derrière des décors.
 * L'enfant doit trouver celui qu'on lui demande. Touche un décor pour regarder derrière !
 */
@Composable
fun HideSeekScreen(
    pool: List<Item>,
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String = "",
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    val spots = remember { listOf("🌳", "🏠", "⛺", "🪨", "🌲", "🚪") }
    var round by remember { mutableIntStateOf(0) }
    val hidden = remember(round) { pool.shuffled().take(6) }
    val target = remember(round) { hidden.random() }
    val opened = remember(round) { mutableStateListOf<Int>() }
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    var idle by remember(round) { mutableIntStateOf(0) }

    val askText = when (lang) {
        Lang.AR -> "أين ${target.shortSpoken()}؟ ابحث عنه!"
        Lang.EN -> "Where is ${target.shortSpoken()}? Find it!"
        else -> "Où est ${target.shortSpoken()} ? Cherche-le !"
    }
    val askLang = if (lang == Lang.MIX) Lang.FR else lang

    LaunchedEffect(round) {
        delay(400)
        sound.speak(askText, askLang)
    }
    // Aide : après un moment sans réponse, on répète la consigne
    LaunchedEffect(round, idle) {
        delay(9000)
        if (!found) {
            sound.speak(askText, askLang)
            idle += 1
        }
    }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess()
            sound.speak(p, askLang)
            onStar()
            delay(2400)
            round += 1
        }
    }

    fun tap(i: Int) {
        if (found || i in opened) return
        sound.playPop()
        opened.add(i)
        idle += 1
        val it = hidden[i]
        if (it.symbol == target.symbol) {
            found = true
            message = "🎉 " + praise(lang)
        } else {
            wrongs += 1
            sound.playError()
            val said = it.shortSpoken()
            message = when (lang) {
                Lang.AR -> "هذا $said. حاول مرة أخرى!"
                Lang.EN -> "That is $said. Try again!"
                else -> "C'est $said. Cherche encore !"
            }
            sound.speak(message, askLang)
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Cache-cache", "الغميضة", "Hide and seek"), stars = stars, onBack = onBack)
            Text(
                askText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                message,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (found) Green else Pink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 16.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                hidden.indices.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { i ->
                            val isOpen = i in opened
                            // Après 2 erreurs, la bonne cachette tremble de couleur : on aide l'enfant
                            val helpGlow = wrongs >= 2 && !found && hidden[i].symbol == target.symbol
                            BouncyBox(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                color = if (isOpen) Color.White
                                else if (helpGlow) Yellow else tileColors[i % tileColors.size],
                                shape = RoundedCornerShape(28.dp),
                                onClick = { tap(i) }
                            ) {
                                if (isOpen) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(hidden[i].emoji.takeIf { it != NUMBER_EMOJI } ?: "⭐", fontSize = 40.sp)
                                        Text(
                                            hidden[i].symbol,
                                            fontSize = 56.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (hidden[i].symbol == target.symbol) Green else Purple
                                        )
                                    }
                                } else {
                                    Text(spots[i % spots.size], fontSize = 72.sp)
                                }
                            }
                        }
                    }
                }
            }
            if (found) ConfettiRain()
        }
    }
}
