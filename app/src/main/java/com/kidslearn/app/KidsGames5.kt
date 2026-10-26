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

