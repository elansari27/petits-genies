package com.kidslearn.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch

// =====================================================================
//  Composants communs des jeux : cadre, tuile-réponse animée
// =====================================================================

/** Langue utilisée pour la voix (le mode MIX parle français pour les consignes). */
fun speakLang(lang: Lang) = if (lang == Lang.MIX) Lang.FR else lang

/** Cadre commun : décor, barre du haut, consigne (touchable pour la réécouter), retour d'information. */
@Composable
fun GameShell(
    title: String,
    prompt: String,
    message: String,
    good: Boolean,
    confetti: Boolean,
    stars: Int,
    onBack: () -> Unit,
    onRepeat: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = title, stars = stars, onBack = onBack)
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .then(if (onRepeat != null) Modifier.clickable { onRepeat() } else Modifier)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    (if (onRepeat != null) "🔊 " else "") + prompt,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Purple,
                    textAlign = TextAlign.Center
                )
            }
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
    }
}

/**
 * Grosse tuile de réponse animée : si c'est juste elle saute et se balance de joie,
 * si c'est faux elle secoue la tête. [glow] fait pulser la bonne réponse (aide après 2 erreurs).
 */
@Composable
fun AnswerTile(
    modifier: Modifier,
    color: Color,
    correct: Boolean,
    glow: Boolean = false,
    enabled: Boolean = true,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dx = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    val rot = remember { Animatable(0f) }
    val tr = rememberInfiniteTransition(label = "glow")
    val pulse by tr.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        modifier.graphicsLayer {
            translationX = dx.value
            val s = pop.value * (if (glow) pulse else 1f)
            scaleX = s
            scaleY = s
            rotationZ = rot.value
        }
    ) {
        BouncyBox(
            modifier = Modifier.fillMaxSize(),
            color = if (glow) Yellow else color,
            shape = RoundedCornerShape(32.dp),
            onClick = {
                if (enabled) {
                    if (correct) {
                        scope.launch {
                            pop.animateTo(1.25f, tween(150))
                            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        scope.launch {
                            rot.animateTo(8f, tween(110))
                            rot.animateTo(-8f, tween(110))
                            rot.animateTo(0f, tween(110))
                        }
                        onCorrect()
                    } else {
                        scope.launch {
                            for (i in 0 until 3) {
                                dx.animateTo(16f, tween(60))
                                dx.animateTo(-16f, tween(60))
                            }
                            dx.animateTo(0f, tween(50))
                        }
                        onWrong()
                    }
                }
            }
        ) { content() }
    }
}

// ---------- Trouver l'intrus ----------

@Composable
fun OddOneOutScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val setup = remember(round) {
        val two = Things.families.shuffled().take(2)
        val oddThing = two[1].things.random()
        val tiles = (two[0].things.shuffled().take(2) + oddThing).shuffled()
        Pair(tiles, oddThing)
    }
    val (tiles, odd) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val ask = t(lang, "Trouve l'intrus !", "اِبْحَثْ عَنِ الغَرِيب!", "Find the odd one out!")
    val sl = speakLang(lang)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p + " " + odd.name(sl), sl); onStar()
            delay(2600); round += 1
        }
    }

    GameShell(
        t(lang, "L'intrus", "الغَرِيب", "Odd one out"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tiles.forEachIndexed { i, th ->
                AnswerTile(
                    modifier = Modifier.weight(1f).height(170.dp),
                    color = tileColors[i % tileColors.size],
                    correct = th === odd,
                    glow = wrongs >= 2 && !found && th === odd,
                    enabled = !found,
                    onCorrect = { found = true; message = "🎉 " + praise(lang) },
                    onWrong = {
                        wrongs += 1; sound.playError()
                        message = retry(lang); sound.speak(message, sl)
                    }
                ) { Text(th.emoji, fontSize = 64.sp) }
            }
        }
        Spacer(Modifier.height(48.dp))
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
        val answer = if (round % 3 == 2) c else b
        val choices = (listOf(answer) + syms.filter { it != answer }.take(2)).shuffled()
        Triple(seq, answer, choices)
    }
    val (seq, answer, choices) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val ask = t(lang, "Qu'est-ce qui vient après ?", "مَاذَا يَأْتِي بَعْدَ ذَلِك؟", "What comes next?")
    val sl = speakLang(lang)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2200); round += 1
        }
    }

    GameShell(
        t(lang, "Logique", "المَنْطِق", "Logic"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
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
                AnswerTile(
                    modifier = Modifier.weight(1f).height(130.dp),
                    color = tileColors[i % tileColors.size],
                    correct = e == answer,
                    glow = wrongs >= 2 && !found && e == answer,
                    enabled = !found,
                    onCorrect = { found = true; message = "🎉 " + praise(lang) },
                    onWrong = {
                        wrongs += 1; sound.playError()
                        message = retry(lang); sound.speak(message, sl)
                    }
                ) { Text(e, fontSize = 56.sp) }
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

// ---------- Ombres : à qui est cette ombre ? ----------

@Composable
fun ShadowScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val setup = remember(round) {
        val all = Things.all.shuffled()
        val answer = all[0]
        Pair(answer, (listOf(answer) + all.drop(1).take(2)).shuffled())
    }
    val (answer, choices) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val ask = t(lang, "À qui est cette ombre ?", "لِمَنْ هَذَا الظِّلّ؟", "Whose shadow is this?")
    val sl = speakLang(lang)
    val reveal = remember { Animatable(0f) }

    LaunchedEffect(round) { reveal.snapTo(0f); delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            reveal.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p + " " + answer.name(sl), sl); onStar()
            delay(2600); round += 1
        }
    }

    GameShell(
        t(lang, "Les ombres", "الظِّلَال", "Shadows"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            // L'emoji est peint en noir (mode SrcIn) tant que l'enfant n'a pas trouvé
            val shadowMod = if (found) Modifier.graphicsLayer {
                scaleX = 1f + 0.2f * (1f - reveal.value); scaleY = scaleX
            } else Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(Color(0xFF2B2A5E), blendMode = BlendMode.SrcIn)
                }
            Text(answer.emoji, fontSize = 150.sp, modifier = shadowMod)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            choices.forEachIndexed { i, th ->
                AnswerTile(
                    modifier = Modifier.weight(1f).height(130.dp),
                    color = tileColors[i % tileColors.size],
                    correct = th === answer,
                    glow = wrongs >= 2 && !found && th === answer,
                    enabled = !found,
                    onCorrect = { found = true; message = "🎉 " + praise(lang) },
                    onWrong = {
                        wrongs += 1; sound.playError()
                        message = retry(lang); sound.speak(message, sl)
                    }
                ) { Text(th.emoji, fontSize = 56.sp) }
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}
