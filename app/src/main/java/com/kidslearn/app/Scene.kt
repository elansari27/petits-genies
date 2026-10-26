package com.kidslearn.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Le gestionnaire de sons et l'animal-compagnon sont disponibles dans tous les écrans. */
val LocalSound = compositionLocalOf<SoundManager?> { null }
val LocalBuddy = compositionLocalOf { "🦁" }

/** Les amis que l'enfant peut choisir comme compagnon. */
val buddies = listOf("🦁", "🐼", "🐰", "🐻", "🦊", "🐯", "🐸", "🐵")

/**
 * Décor commun à tous les jeux : ciel dégradé, soleil, nuages qui dérivent, collines et fleurs.
 * L'animal choisi par l'enfant accompagne chaque jeu : il parle (bulle), saute quand l'enfant
 * réussit et secoue la tête quand il se trompe.
 */
@Composable
fun GameBackground(buddy: Boolean = true, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        SceneryCanvas()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            content()
            if (buddy) BuddyCompanion()
        }
    }
}

@Composable
private fun SceneryCanvas() {
    val tr = rememberInfiniteTransition(label = "scenery")
    val drift by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift"
    )
    val sway by tr.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "sway"
    )
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFF8ED8FF), Color(0xFFD9F3FF), Color(0xFFFFF6E5))),
            size = Size(w, h)
        )
        // soleil
        drawCircle(Color(0x33FFE066), radius = w * 0.20f, center = Offset(w * 0.86f, h * 0.09f))
        drawCircle(Color(0xFFFFD43B), radius = w * 0.10f, center = Offset(w * 0.86f, h * 0.09f))
        // nuages qui dérivent lentement
        fun cloud(cx: Float, cy: Float, s: Float) {
            val c = Color(0xCCFFFFFF)
            drawOval(c, Offset(cx - 70f * s, cy - 18f * s), Size(140f * s, 40f * s))
            drawCircle(c, 30f * s, Offset(cx - 25f * s, cy - 24f * s))
            drawCircle(c, 38f * s, Offset(cx + 12f * s, cy - 30f * s))
        }
        val span = w + 360f
        val d = resources_density(this)
        for ((i, row) in listOf(0.14f to 1.0f, 0.30f to 0.7f, 0.22f to 0.85f).withIndex()) {
            val x = ((drift * span * (0.6f + 0.3f * i) + i * span / 3f) % span) - 180f
            cloud(x, h * row.first, row.second * d)
        }
        // collines
        drawCircle(Color(0xFFA5E887), radius = w * 0.75f, center = Offset(w * 0.15f, h * 1.02f))
        drawCircle(Color(0xFF7ED957), radius = w * 0.70f, center = Offset(w * 0.95f, h * 1.05f))
        drawCircle(Color(0xFF5CC845), radius = w * 0.55f, center = Offset(w * 0.5f, h * 1.12f))
        // fleurs qui se balancent un peu
        val flowerColors = listOf(Color(0xFFFF5FA2), Color(0xFFFFD43B), Color(0xFFFFFFFF), Color(0xFFFF9F43))
        for (i in 0 until 9) {
            val fx = w * (0.06f + 0.11f * i)
            val fy = h * (0.955f + 0.012f * ((i * 7) % 4))
            val sw = sway * 3f * (if (i % 2 == 0) 1 else -1)
            drawLine(Color(0xFF2E9E3A), Offset(fx, fy + 22f), Offset(fx + sw, fy), strokeWidth = 4f)
            drawCircle(flowerColors[i % flowerColors.size], 9f, Offset(fx + sw, fy))
            drawCircle(Color(0xFFFFE066), 4f, Offset(fx + sw, fy))
        }
    }
}

private fun resources_density(scope: androidx.compose.ui.graphics.drawscope.DrawScope): Float =
    scope.density.coerceAtLeast(1f)

/** Choix de l'animal compagnon (sur l'accueil). */
@Composable
fun BuddyPicker(current: String, onPick: (String) -> Unit) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
    ) {
        buddies.forEach { b ->
            BouncyBox(
                modifier = Modifier.size(44.dp),
                color = if (b == current) Yellow else Color.White.copy(alpha = 0.75f),
                shape = androidx.compose.foundation.shape.CircleShape,
                onClick = { onPick(b) }
            ) { Text(b, fontSize = 24.sp) }
        }
    }
}

@Composable
private fun BuddyCompanion() {
    val sound = LocalSound.current ?: return
    val emoji = LocalBuddy.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val jump = remember { Animatable(0f) }
    val spin = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    var bubble by remember { mutableStateOf("") }
    var talking by remember { mutableStateOf(false) }

    // Il dit tout ce que la voix dit (consignes, félicitations, "réessaie"...)
    LaunchedEffect(sound.spokeTick) {
        val txt = sound.lastSpoken
        if (txt.isNotBlank()) {
            bubble = txt
            talking = true
            delay(3200)
            talking = false
        }
    }
    // Il saute de joie quand l'enfant réussit
    LaunchedEffect(sound.cheerTick) {
        if (sound.cheerTick > 0) {
            scope.launch { spin.snapTo(0f); spin.animateTo(360f, tween(650)) }
            jump.snapTo(0f)
            jump.animateTo(-46f, tween(200))
            jump.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    // Il secoue la tête quand l'enfant se trompe
    LaunchedEffect(sound.oopsTick) {
        if (sound.oopsTick > 0) {
            for (i in 0 until 3) {
                shake.animateTo(14f, tween(70))
                shake.animateTo(-14f, tween(70))
            }
            shake.animateTo(0f, tween(60))
        }
    }
    val tr = rememberInfiniteTransition(label = "buddy")
    val bob by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (talking) 260 else 1100), RepeatMode.Reverse),
        label = "bob"
    )

    Box(Modifier.fillMaxSize().padding(top = 78.dp, start = 6.dp)) {
        Box(
            Modifier
                .align(Alignment.TopStart)
                .graphicsLayer {
                    translationY = jump.value - bob * (if (talking) 8f else 4f)
                    translationX = shake.value
                    rotationZ = spin.value
                }
        ) { Text(emoji, fontSize = 46.sp) }
        if (talking && bubble.isNotBlank()) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 58.dp, top = 4.dp)
                    .widthIn(max = 230.dp)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    bubble,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B2A8C),
                    textAlign = TextAlign.Center,
                    maxLines = 3
                )
            }
        }
    }
}
