package com.kidslearn.app

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Hexagone « pointe en haut » : les rangées décalées s'emboîtent en nid d'abeille. */
val HexShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w / 2f, 0f)
    lineTo(w, h * 0.25f)
    lineTo(w, h * 0.75f)
    lineTo(w / 2f, h)
    lineTo(0f, h * 0.75f)
    lineTo(0f, h * 0.25f)
    close()
}

/** Une tuile hexagonale colorée : grand emoji + petit libellé. */
@Composable
fun HexTile(
    emoji: String,
    label: String,
    color: Color,
    width: Dp,
    onClick: () -> Unit
) {
    BouncyBox(
        modifier = Modifier.requiredSize(width = width, height = width * 1.1547f),
        color = color,
        shape = HexShape,
        onClick = onClick
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = (width.value * 0.36f).sp)
            Text(
                label,
                fontSize = (width.value * 0.115f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Zizou : la mascotte originale de l'application. Une petite créature ronde et bleue
 * avec une antenne-étoile, des joues roses, qui rebondit, cligne des yeux et salue.
 * Entièrement dessinée dans l'application (aucune image, aucun personnage existant).
 */
@Composable
fun Zizou(size: Dp = 160.dp, modifier: Modifier = Modifier, happy: Boolean = true) {
    val tr = rememberInfiniteTransition(label = "zizou")
    val bounce by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce"
    )
    val blink by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Restart),
        label = "blink"
    )
    val wave by tr.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "wave"
    )
    val eyesClosed = blink > 0.95f
    val sizeV = size.value
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .size(size)
                .graphicsLayer { translationY = -bounce * sizeV * 0.06f }
        ) {
            val w = this.size.width
            val h = this.size.height
            val body = Color(0xFF5BC0F8)
            val belly = Color(0xFFBDE8FF)
            val dark = Color(0xFF2B2A5E)
            // ombre
            drawOval(Color(0x22000000), Offset(w * 0.2f, h * 0.9f), Size(w * 0.6f, h * 0.07f))
            // bras qui salue
            drawLine(body, Offset(w * 0.86f, h * 0.60f), Offset(w * 0.97f, h * (0.44f + 0.05f * wave)), strokeWidth = w * 0.09f, cap = StrokeCap.Round)
            drawLine(body, Offset(w * 0.14f, h * 0.62f), Offset(w * 0.04f, h * 0.70f), strokeWidth = w * 0.09f, cap = StrokeCap.Round)
            // pieds
            drawOval(Color(0xFFFFA63D), Offset(w * 0.25f, h * 0.84f), Size(w * 0.22f, h * 0.09f))
            drawOval(Color(0xFFFFA63D), Offset(w * 0.53f, h * 0.84f), Size(w * 0.22f, h * 0.09f))
            // antenne + étoile (un gros point doré entouré de pointes)
            drawLine(dark, Offset(w * 0.5f, h * 0.20f), Offset(w * 0.5f, h * 0.08f), strokeWidth = w * 0.025f, cap = StrokeCap.Round)
            val sc = Offset(w * 0.5f, h * 0.07f)
            for (a in 0 until 8) {
                val ang = a * (Math.PI / 4.0)
                val len = if (a % 2 == 0) w * 0.08f else w * 0.05f
                drawLine(
                    Color(0xFFFFD43B), sc,
                    Offset(sc.x + (Math.cos(ang) * len).toFloat(), sc.y + (Math.sin(ang) * len).toFloat()),
                    strokeWidth = w * 0.03f, cap = StrokeCap.Round
                )
            }
            drawCircle(Color(0xFFFFD43B), radius = w * 0.045f, center = sc)
            // corps
            drawOval(body, Offset(w * 0.12f, h * 0.18f), Size(w * 0.76f, h * 0.70f))
            drawOval(belly, Offset(w * 0.28f, h * 0.52f), Size(w * 0.44f, h * 0.32f))
            // yeux
            val ey = h * 0.42f
            for (ex in listOf(w * 0.36f, w * 0.64f)) {
                if (eyesClosed) {
                    drawLine(dark, Offset(ex - w * 0.05f, ey), Offset(ex + w * 0.05f, ey), strokeWidth = w * 0.025f, cap = StrokeCap.Round)
                } else {
                    drawCircle(Color.White, radius = w * 0.085f, center = Offset(ex, ey))
                    drawCircle(dark, radius = w * 0.045f, center = Offset(ex + w * 0.01f, ey + h * 0.005f))
                    drawCircle(Color.White, radius = w * 0.015f, center = Offset(ex + w * 0.025f, ey - h * 0.015f))
                }
            }
            // joues
            drawCircle(Color(0x88FF7EB6), radius = w * 0.05f, center = Offset(w * 0.26f, h * 0.53f))
            drawCircle(Color(0x88FF7EB6), radius = w * 0.05f, center = Offset(w * 0.74f, h * 0.53f))
            // bouche
            if (happy) {
                drawArc(
                    dark, 20f, 140f, false,
                    Offset(w * 0.40f, h * 0.48f), Size(w * 0.20f, h * 0.12f),
                    style = Stroke(width = w * 0.028f, cap = StrokeCap.Round)
                )
            } else {
                drawLine(dark, Offset(w * 0.44f, h * 0.57f), Offset(w * 0.56f, h * 0.57f), strokeWidth = w * 0.028f, cap = StrokeCap.Round)
            }
        }
    }
}

// =====================================================================
//  Glisser-déposer : l'objet SUIT vraiment le doigt
// =====================================================================

/** Vrai si le point [p] (coordonnées écran) est dans [r]. */
fun rectHas(r: androidx.compose.ui.geometry.Rect, p: Offset): Boolean =
    p.x >= r.left && p.x <= r.right && p.y >= r.top && p.y <= r.bottom

/**
 * Un objet que l'enfant peut attraper et déplacer. Il suit le doigt (translation graphique,
 * jamais inversée en arabe), grossit un peu pendant le déplacement, et revient en rebondissant
 * si [onDrop] renvoie faux.
 *
 * [onDrop] reçoit la position du doigt (coordonnées écran) et la distance parcourue en pixels ;
 * il renvoie vrai si le dépôt est accepté (l'objet reste alors où il a été lâché).
 * [onTap] permet aussi de jouer d'un simple toucher (plus facile pour les petits).
 */
@Composable
fun Draggable(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTap: (() -> Unit)? = null,
    onDragState: (Boolean) -> Unit = {},
    onDrop: (pointer: Offset, distance: Float) -> Boolean,
    content: @Composable () -> Unit
) {
    // Toujours utiliser les dernières lambdas (le geste n'est pas relancé à chaque recomposition)
    val curDrop by androidx.compose.runtime.rememberUpdatedState(onDrop)
    val curTap by androidx.compose.runtime.rememberUpdatedState(onTap)
    val curState by androidx.compose.runtime.rememberUpdatedState(onDragState)
    val ax = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    val ay = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var origin by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }
    var startLocal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }
    var dragging by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var startX by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    var startY by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    val grow by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (dragging) 1.18f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy),
        label = "grow"
    )

    Box(
        modifier = modifier.androidx_onPositioned { origin = it }
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = ax.value
                    translationY = ay.value
                }
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { local ->
                                dragging = true
                                curState(true)
                                startLocal = local
                                startX = ax.value
                                startY = ay.value
                            },
                            onDragEnd = {
                                dragging = false
                                curState(false)
                                val pointer = origin + Offset(ax.value, ay.value) + startLocal
                                val dist = Offset(ax.value - startX, ay.value - startY).getDistance()
                                val ok = curDrop(pointer, dist)
                                if (!ok) {
                                    scope.launch {
                                        ax.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy))
                                    }
                                    scope.launch {
                                        ay.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy))
                                    }
                                }
                            },
                            onDragCancel = {
                                dragging = false
                                curState(false)
                                scope.launch { ax.animateTo(0f) }
                                scope.launch { ay.animateTo(0f) }
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                scope.launch {
                                    ax.snapTo(ax.value + amount.x)
                                    ay.snapTo(ay.value + amount.y)
                                }
                            }
                        )
                    }
                }
                .pointerInput(enabled, onTap != null) {
                    if (enabled && onTap != null) {
                        detectTapGestures(onTap = { curTap?.invoke() })
                    }
                }
        ) {
            Box(Modifier.graphicsLayer { scaleX = grow; scaleY = grow }) { content() }
        }
    }
}

private fun Modifier.androidx_onPositioned(block: (Offset) -> Unit): Modifier =
    this.onGloballyPositioned { block(it.positionInRoot()) }


// =====================================================================
//  Boîtes de dialogue ludiques
// =====================================================================

/** « Tu veux quitter ? » avec Zizou et deux gros boutons OUI / NON. */
@Composable
fun ExitDialog(onYes: () -> Unit, onNo: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .background(Color.White, RoundedCornerShape(36.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Zizou(130.dp, happy = false)
            Text(
                "Tu veux quitter ?\nهَلْ تُرِيدُ الخُرُوج؟\nQuit the app?",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3B2A8C),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.padding(top = 18.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
            ) {
                BouncyBox(Modifier.size(width = 130.dp, height = 70.dp), Pink, RoundedCornerShape(35.dp), onYes) {
                    Text("✅ Oui · نعم", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                BouncyBox(Modifier.size(width = 130.dp, height = 70.dp), Green, RoundedCornerShape(35.dp), onNo) {
                    Text("❌ Non · لا", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}

/** Bandeau « nouveau jeu » affiché entre deux jeux en mode surprise. */
@Composable
fun SwitchBanner() {
    Box(Modifier.fillMaxSize().background(Color(0x88000000)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(36.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Zizou(140.dp)
            Text(
                "Nouveau jeu ! 🎲\nلُعْبَةٌ جَدِيدَة!\nNew game!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3B2A8C),
                textAlign = TextAlign.Center
            )
        }
    }
}
