package com.kidslearn.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================
// Jeux "comme sur le Play Store" — v3
//  - Petit ou grand : comparer les tailles
//  - Relier : tracer un trait de la lettre à l'image (glisser)
//  - Le panier : glisser-déposer les objets dans la corbeille
//  - ConfettiRain : pluie d'emojis à chaque victoire
// ============================================================

// ---------- Confettis ----------

private class ConfettiP(
    val emoji: String, val x: Float, val delay: Float,
    val life: Float, val size: Int
)

private val confettiEmojis = listOf("🎉", "⭐", "✨", "🌟", "🎈", "💫", "😄", "🦄", "🎊")

/** Pluie de confettis qui dure ~2,5 s. Se place en overlay, ne bloque pas les touches. */
@Composable
fun ConfettiRain() {
    val particles = remember {
        List(28) {
            ConfettiP(
                emoji = confettiEmojis.random(),
                x = (5..90).random() / 100f,
                delay = (0..40).random() / 100f,
                life = (45..80).random() / 100f,
                size = (18..34).random()
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(2400, easing = LinearEasing))
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val t = ((progress.value - p.delay) / p.life).coerceIn(0f, 1f)
            if (progress.value >= p.delay && t < 1f) {
                Text(
                    text = p.emoji,
                    fontSize = p.size.sp,
                    modifier = Modifier.offset(
                        x = maxWidth * (p.x + 0.04f * sin(t * 12f + p.x * 20f)),
                        y = maxHeight * (p.delay * 0.25f + t * 1.05f)
                    )
                )
            }
        }
    }
}

// ---------- Jeu : petit ou grand ----------

@Composable
fun BigSmallScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val emoji = remember(round) { Content.countEmojis.random() }
    val sizes = remember(round) { listOf(38, 66, 104).shuffled() }
    val wantBig = remember(round) { java.util.Random().nextBoolean() }
    var solved by remember(round) { mutableStateOf(false) }
    var feedback by remember(round) { mutableStateOf("") }
    val targetSize = if (wantBig) sizes.maxOrNull() else sizes.minOrNull()
    val correct = sizes.indexOf(targetSize ?: sizes.first())

    val question = t(
        lang,
        if (wantBig) "Touche le PLUS GRAND !" else "Touche le PLUS PETIT !",
        if (wantBig) "المس الأكبر!" else "المس الأصغر!"
    )

    LaunchedEffect(round) {
        delay(300)
        sound.speak(question, lang)
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(2000)
            round += 1
        }
    }

    Box(Modifier.fillMaxSize()) {
        GameBackground {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                TopBar(
                    title = t(lang, "Petit ou grand ?", "صغير أم كبير؟"),
                    stars = stars,
                    onBack = onBack
                )
                BouncyBox(
                    modifier = Modifier.size(width = 220.dp, height = 80.dp),
                    color = Orange,
                    onClick = { sound.speak(question, lang) }
                ) { Text("🔊 👆", fontSize = 34.sp) }
                Spacer(Modifier.height(8.dp))
                Text(
                    feedback,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (solved) Green else Pink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(44.dp)
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sizes.forEachIndexed { i, s ->
                        BouncyBox(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            color = if (solved && i == correct) Green else Color.White,
                            shape = RoundedCornerShape(28.dp),
                            onClick = {
                                if (solved) return@BouncyBox
                                if (i == correct) {
                                    solved = true
                                    val p = praiseNamed(lang, childName)
                                    feedback = "🎉 $p"
                                    sound.playSuccess()
                                    sound.speak(p, lang)
                                    onStar()
                                } else {
                                    feedback = retry(lang)
                                    sound.playError()
                                    sound.vibrate(100)
                                }
                            }
                        ) {
                            Text(emoji, fontSize = s.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        if (solved) ConfettiRain()
    }
}

// ---------- Jeu : relier la lettre à l'image ----------

@Composable
fun ConnectScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val pairs = remember(round) { Content.letters(lang).shuffled().take(3) }
    val rightOrder = remember(round) { pairs.shuffled() }
    var matched by remember(round) { mutableStateOf(setOf<String>()) }
    var dragFrom by remember(round) { mutableStateOf<Int?>(null) }
    var dragPos by remember(round) { mutableStateOf<Offset?>(null) }
    var feedback by remember(round) { mutableStateOf("") }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current.density

    fun leftAnchor(i: Int) = Offset(boardSize.width * 0.14f, boardSize.height * (0.2f + 0.3f * i))
    fun rightAnchor(j: Int) = Offset(boardSize.width * 0.86f, boardSize.height * (0.2f + 0.3f * j))

    LaunchedEffect(round) {
        delay(300)
        sound.speak(t(lang, "Relie chaque lettre à son image !", "اربط كل حرف بصورته!"), lang)
    }

    LaunchedEffect(matched, round) {
        if (matched.size == 3) {
            val p = praiseNamed(lang, childName)
            feedback = "🏆 $p"
            sound.playSuccess()
            sound.speak(p, lang)
            onStar()
            delay(2600)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Relie !", "اربط!"), stars = stars, onBack = onBack)
            Text(
                if (feedback.isEmpty())
                    t(lang, "Glisse de la lettre à l'image", "اسحب من الحرف إلى الصورة")
                else feedback,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (matched.size == 3) Green else Purple,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 12.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(Color.White, RoundedCornerShape(32.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { boardSize = it }
                        .pointerInput(round) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                val li = (0..2).firstOrNull { i ->
                                    pairs[i].symbol !in matched &&
                                        (leftAnchor(i) - down.position).getDistance() < 100f * density
                                }
                                if (li != null) {
                                    dragFrom = li
                                    sound.playClick()
                                    do {
                                        val ev = awaitPointerEvent()
                                        dragPos = ev.changes.first().position
                                    } while (ev.changes.any { it.pressed })
                                    val end = dragPos
                                    if (end != null) {
                                        val rj = (0..2).firstOrNull { j ->
                                            (rightAnchor(j) - end).getDistance() < 130f * density
                                        }
                                        if (rj != null && rightOrder[rj].symbol == pairs[li].symbol) {
                                            matched = matched + pairs[li].symbol
                                            feedback = praiseNamed(lang, childName)
                                            sound.playSuccess()
                                            onStar()
                                        } else if (rj != null) {
                                            feedback = retry(lang)
                                            sound.playError()
                                        }
                                    }
                                    dragFrom = null
                                    dragPos = null
                                }
                            }
                        }
                ) {
                    matched.forEach { sym ->
                        val i = pairs.indexOfFirst { it.symbol == sym }
                        val j = rightOrder.indexOfFirst { it.symbol == sym }
                        if (i >= 0 && j >= 0) {
                            drawLine(
                                Green,
                                leftAnchor(i),
                                rightAnchor(j),
                                strokeWidth = 14f * density
                            )
                        }
                    }
                    val df = dragFrom
                    val dp = dragPos
                    if (df != null && dp != null) {
                        drawLine(Orange, leftAnchor(df), dp, strokeWidth = 14f * density)
                    }
                }
                // Lettres à gauche
                pairs.forEachIndexed { i, item ->
                    val a = leftAnchor(i)
                    val done = item.symbol in matched
                    Text(
                        text = item.symbol,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (done) Green else Purple,
                        modifier = Modifier.offset {
                            IntOffset(a.x.roundToInt() - 40, a.y.roundToInt() - 44)
                        }
                    )
                }
                // Images à droite
                rightOrder.forEachIndexed { j, item ->
                    val a = rightAnchor(j)
                    Text(
                        text = item.emoji,
                        fontSize = 58.sp,
                        modifier = Modifier.offset {
                            IntOffset(a.x.roundToInt() - 42, a.y.roundToInt() - 44)
                        }
                    )
                }
                if (matched.size == 3) ConfettiRain()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ---------- Jeu : glisser dans le panier ----------

@Composable
fun BasketScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val items = remember(round) { Content.countEmojis.shuffled().take(3) }
    var placed by remember(round) { mutableStateOf(setOf<String>()) }
    var solved by remember(round) { mutableStateOf(false) }
    var zone by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current.density

    fun startX(i: Int) = zone.width * (0.2f + 0.3f * i) - 42f * density
    val startY = 40f * density
    val itemPx = 84f * density

    fun inBasket(center: Offset): Boolean {
        val z = zone
        if (z.width == 0) return false
        val cx = z.width / 2f
        val top = z.height - (150f + 20f) * density
        return center.y > top &&
            center.x > cx - 160f * density &&
            center.x < cx + 160f * density
    }

    LaunchedEffect(round) {
        delay(300)
        sound.speak(t(lang, "Mets tout dans le panier !", "ضع كل شيء في السلة!"), lang)
    }

    LaunchedEffect(placed, round) {
        if (placed.size == 3) {
            solved = true
            val p = praiseNamed(lang, childName)
            sound.playSuccess()
            sound.speak(p, lang)
            onStar()
            delay(2600)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Le panier", "السلة"), stars = stars, onBack = onBack)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .onSizeChanged { zone = it }
            ) {
                // Le panier
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .size(width = 280.dp, height = 150.dp)
                        .background(
                            if (placed.size == 3) Green else Orange,
                            RoundedCornerShape(
                                topStart = 24.dp, topEnd = 24.dp,
                                bottomStart = 44.dp, bottomEnd = 44.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧺", fontSize = 64.sp)
                        Text(
                            "${placed.size} / 3",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
                // Objets à glisser
                items.forEachIndexed { i, emo ->
                    if (emo !in placed) {
                        var off by remember(round, i, zone.width) {
                            mutableStateOf(Offset(startX(i), startY))
                        }
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(off.x.roundToInt(), off.y.roundToInt()) }
                                .size(84.dp)
                                .pointerInput(round, i) {
                                    detectDragGestures(
                                        onDragStart = { sound.playClick() },
                                        onDrag = { change, amt ->
                                            off = Offset(
                                                (off.x + amt.x).coerceIn(0f, zone.width - itemPx),
                                                (off.y + amt.y).coerceIn(0f, zone.height - itemPx)
                                            )
                                            change.consume()
                                        },
                                        onDragEnd = {
                                            val center = Offset(
                                                off.x + itemPx / 2f,
                                                off.y + itemPx / 2f
                                            )
                                            if (inBasket(center)) {
                                                placed = placed + emo
                                                sound.playPop()
                                                sound.playStar()
                                                onStar()
                                            } else {
                                                off = Offset(startX(i), startY)
                                                sound.playError()
                                            }
                                        },
                                        onDragCancel = {
                                            off = Offset(startX(i), startY)
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emo, fontSize = 60.sp)
                        }
                    }
                }
                if (solved) ConfettiRain()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
