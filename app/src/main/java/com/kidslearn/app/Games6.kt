package com.kidslearn.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// =====================================================================
//  Nouveaux jeux : Trouver l'intrus, Tri, Logique, Ombres (puzzle)
// =====================================================================

/** Une catégorie d'objets, avec son nom dans chaque langue. */
private class Cat(val fr: String, val ar: String, val en: String, val icon: String, val items: List<String>) {
    fun name(lang: Lang) = t(lang, fr, ar, en)
}

private val cats = listOf(
    Cat("Animaux", "حيوانات", "Animals", "🐾", listOf("🐶", "🐱", "🐰", "🐻", "🐼", "🦁", "🐮", "🐷")),
    Cat("Fruits", "فواكه", "Fruits", "🍎", listOf("🍎", "🍌", "🍇", "🍓", "🍊", "🍉", "🍐", "🍒")),
    Cat("Transports", "مواصلات", "Vehicles", "🚗", listOf("🚗", "🚌", "🚲", "✈️", "🚂", "🚀", "🚁", "⛵")),
    Cat("Nourriture", "طعام", "Food", "🍕", listOf("🍕", "🍔", "🍟", "🍦", "🍪", "🥕", "🍞", "🧀"))
)

private fun speakLang(lang: Lang) = if (lang == Lang.MIX) Lang.FR else lang

/** Cadre commun : fond, barre du haut, consigne, retour d'information, confettis. */
@Composable
private fun GameShell(
    title: String,
    prompt: String,
    message: String,
    good: Boolean,
    confetti: Boolean,
    stars: Int,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = title, stars = stars, onBack = onBack)
            Text(
                prompt,
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
                color = if (good) Green else Pink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 16.dp)
            )
            content()
        }
        if (confetti) ConfettiRain()
    }
}

/** Grosse tuile de choix. [glow] met en évidence la bonne réponse après des erreurs (aide). */
@Composable
private fun ChoiceTile(
    modifier: Modifier,
    color: Color,
    glow: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    BouncyBox(
        modifier = modifier,
        color = if (glow) Yellow else color,
        shape = RoundedCornerShape(32.dp),
        onClick = onClick
    ) { content() }
}

// ---------- Trouver l'intrus ----------

@Composable
fun OddOneOutScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val setup = remember(round) {
        val two = cats.shuffled().take(2)
        val main = two[0]; val other = two[1]
        val tiles = (main.items.shuffled().take(2) + other.items.random()).shuffled()
        Triple(tiles, tiles.first { it in other.items && it !in main.items }, main)
    }
    val (tiles, odd, _) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val ask = t(lang, "Trouve l'intrus !", "جد الغريب!", "Find the odd one out!")
    val sl = speakLang(lang)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2300); round += 1
        }
    }

    GameShell(
        t(lang, "L'intrus", "الغريب", "Odd one out"), ask, message, found, found, stars, onBack
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tiles.forEachIndexed { i, e ->
                ChoiceTile(
                    modifier = Modifier.weight(1f).height(160.dp),
                    color = tileColors[i % tileColors.size],
                    glow = wrongs >= 2 && !found && e == odd,
                    onClick = {
                        if (!found) {
                            if (e == odd) { found = true; message = "🎉 " + praise(lang) }
                            else {
                                wrongs += 1; sound.playError()
                                message = retry(lang); sound.speak(message, sl)
                            }
                        }
                    }
                ) { Text(e, fontSize = 64.sp) }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ---------- Tri : à quelle famille appartient l'objet ? ----------

@Composable
fun SortScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val pair = remember(round / 4) { cats.shuffled().take(2) }
    val item = remember(round) {
        val c = pair.random()
        c to c.items.random()
    }
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val ask = t(lang, "Dans quel panier va-t-il ?", "في أي سلة يذهب؟", "Which basket does it go in?")
    val sl = speakLang(lang)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(1800); round += 1
        }
    }

    GameShell(t(lang, "Le tri", "الفرز", "Sorting"), ask, message, found, found, stars, onBack) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { Text(item.second, fontSize = 130.sp) }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pair.forEachIndexed { i, c ->
                ChoiceTile(
                    modifier = Modifier.weight(1f).height(150.dp),
                    color = if (i == 0) Blue else Orange,
                    glow = wrongs >= 2 && !found && c === item.first,
                    onClick = {
                        if (!found) {
                            if (c === item.first) { found = true; message = "🎉 " + praise(lang) }
                            else {
                                wrongs += 1; sound.playError()
                                message = retry(lang); sound.speak(message, sl)
                            }
                        }
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧺", fontSize = 48.sp)
                        Text(c.icon, fontSize = 32.sp)
                        Text(c.name(lang), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ---------- Logique : que vient-il après ? ----------

@Composable
fun LogicScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    val pool = remember { listOf("🔴", "🔵", "🟢", "🟡", "⭐", "🌙", "🐶", "🐱", "🍎", "🍌") }
    var round by remember { mutableIntStateOf(0) }
    val setup = remember(round) {
        val syms = pool.shuffled()
        val a = syms[0]; val b = syms[1]; val c = syms[2]
        // 3 niveaux : ABAB?, AABAAB?, ABCAB?
        val seq = when (round % 3) {
            0 -> listOf(a, b, a, b, a)
            1 -> listOf(a, a, b, a, a)
            else -> listOf(a, b, c, a, b)
        }
        val answer = when (round % 3) { 0 -> b; 1 -> b; else -> c }
        val choices = (listOf(answer) + syms.filter { it != answer }.take(2)).shuffled()
        Triple(seq, answer, choices)
    }
    val (seq, answer, choices) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val ask = t(lang, "Qu'est-ce qui vient après ?", "ماذا يأتي بعد ذلك؟", "What comes next?")
    val sl = speakLang(lang)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2200); round += 1
        }
    }

    GameShell(t(lang, "Logique", "المنطق", "Logic"), ask, message, found, found, stars, onBack) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            seq.forEach { Text(it, fontSize = 40.sp) }
            Text(if (found) answer else "❓", fontSize = 40.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            choices.forEachIndexed { i, e ->
                ChoiceTile(
                    modifier = Modifier.weight(1f).height(130.dp),
                    color = tileColors[i % tileColors.size],
                    glow = wrongs >= 2 && !found && e == answer,
                    onClick = {
                        if (!found) {
                            if (e == answer) { found = true; message = "🎉 " + praise(lang) }
                            else {
                                wrongs += 1; sound.playError()
                                message = retry(lang); sound.speak(message, sl)
                            }
                        }
                    }
                ) { Text(e, fontSize = 56.sp) }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ---------- Ombres : quelle est cette ombre ? ----------

@Composable
fun ShadowScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val setup = remember(round) {
        val all = cats.flatMap { it.items }.distinct().shuffled()
        val answer = all[0]
        Pair(answer, (listOf(answer) + all.drop(1).take(2)).shuffled())
    }
    val (answer, choices) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val ask = t(lang, "À qui est cette ombre ?", "لمن هذا الظل؟", "Whose shadow is this?")
    val sl = speakLang(lang)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2200); round += 1
        }
    }

    GameShell(t(lang, "Les ombres", "الظلال", "Shadows"), ask, message, found, found, stars, onBack) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            // L'emoji est peint en noir (mode SrcIn) tant que l'enfant n'a pas trouvé
            val shadowMod = if (found) Modifier else Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(Color(0xFF2B2A5E), blendMode = BlendMode.SrcIn)
                }
            Text(answer, fontSize = 150.sp, modifier = shadowMod)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            choices.forEachIndexed { i, e ->
                ChoiceTile(
                    modifier = Modifier.weight(1f).height(130.dp),
                    color = tileColors[i % tileColors.size],
                    glow = wrongs >= 2 && !found && e == answer,
                    onClick = {
                        if (!found) {
                            if (e == answer) { found = true; message = "🎉 " + praise(lang) }
                            else {
                                wrongs += 1; sound.playError()
                                message = retry(lang); sound.speak(message, sl)
                            }
                        }
                    }
                ) { Text(e, fontSize = 56.sp) }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}
