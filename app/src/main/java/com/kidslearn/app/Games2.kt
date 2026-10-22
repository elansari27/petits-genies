package com.kidslearn.app

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =====================================================================
//  1. TRACER : suivre la lettre ou le chiffre avec le doigt
// =====================================================================

private class GlyphData(val image: ImageBitmap, val targets: List<Offset>)

/** Dessine le symbole dans une image et repère des points à "colorier" avec le doigt. */
private fun buildGlyph(text: String, size: IntSize, density: Float): GlyphData? {
    if (size.width <= 0 || size.height <= 0) return null
    val bmp = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = android.graphics.Color.WHITE
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = size.height * 0.72f
    val textWidth = paint.measureText(text)
    val maxWidth = size.width * 0.85f
    if (textWidth > maxWidth) paint.textSize = paint.textSize * maxWidth / textWidth
    val fm = paint.fontMetrics
    val baseline = size.height / 2f - (fm.ascent + fm.descent) / 2f
    canvas.drawText(text, size.width / 2f, baseline, paint)

    val w = size.width
    val h = size.height
    val pixels = IntArray(w * h)
    bmp.getPixels(pixels, 0, w, 0, 0, w, h)
    val step = (18 * density).toInt().coerceAtLeast(8)
    val points = ArrayList<Offset>()
    var y = step / 2
    while (y < h) {
        var x = step / 2
        while (x < w) {
            if ((pixels[y * w + x] ushr 24) > 128) points.add(Offset(x.toFloat(), y.toFloat()))
            x += step
        }
        y += step
    }
    return GlyphData(bmp.asImageBitmap(), points)
}

private val rainbow = listOf(Pink, Orange, Yellow, Green, Blue, Purple)

private fun DrawScope.sparkle(c: Offset, r: Float, color: Color) {
    val k = r * 0.28f
    val p = Path()
    p.moveTo(c.x, c.y - r)
    p.lineTo(c.x + k, c.y - k)
    p.lineTo(c.x + r, c.y)
    p.lineTo(c.x + k, c.y + k)
    p.lineTo(c.x, c.y + r)
    p.lineTo(c.x - k, c.y + k)
    p.lineTo(c.x - r, c.y)
    p.lineTo(c.x - k, c.y - k)
    p.close()
    drawPath(p, color)
}

@Composable
fun TraceScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    val items = remember(lang) {
        (Content.letters(lang) + Content.numbers(lang))
            .map { it.copy(symbol = it.symbol.replace("ـ", "")) }
    }
    val rtl = lang == Lang.AR
    var index by remember { mutableIntStateOf(0) }
    val item = items[index]
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current.density
    val glyph = remember(item.symbol, canvasSize) { buildGlyph(item.symbol, canvasSize, density) }
    val covered = remember(glyph) { BooleanArray(glyph?.targets?.size ?: 0) }
    var count by remember(glyph) { mutableIntStateOf(0) }
    var done by remember(index) { mutableStateOf(false) }
    val trail = remember { mutableStateListOf<Offset>() }
    val hitRadius = 34f * density
    val needed = (((glyph?.targets?.size ?: 1) * 0.8f).toInt()).coerceAtLeast(1)

    fun touch(p: Offset) {
        trail.add(p)
        if (trail.size > 14) trail.removeAt(0)
        val g = glyph ?: return
        if (done) return
        var gained = 0
        for (i in g.targets.indices) {
            if (!covered[i] && (g.targets[i] - p).getDistance() < hitRadius) {
                covered[i] = true
                gained++
            }
        }
        if (gained > 0) {
            val before = count
            count += gained
            if (count / 8 != before / 8) sound.playStar()
            if (count >= needed) done = true
        }
    }

    LaunchedEffect(done) {
        if (done) {
            sound.playSuccess()
            sound.speak(praiseFor(lang), if (lang == Lang.AR) Lang.AR else Lang.FR)
            onStar()
            delay(2200)
            index = (index + 1) % items.size
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Trace avec ton doigt", "ارسم بإصبعك"), stars = stars, onBack = onBack)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(32.dp))
                    .onSizeChanged { canvasSize = it }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(glyph) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                touch(down.position)
                                do {
                                    val ev = awaitPointerEvent()
                                    ev.changes.forEach {
                                        touch(it.position)
                                        it.consume()
                                    }
                                } while (ev.changes.any { it.pressed })
                                trail.clear()
                            }
                        }
                ) {
                    @Suppress("UNUSED_VARIABLE")
                    val subscribe = count
                    glyph?.let { g ->
                        drawImage(g.image, colorFilter = ColorFilter.tint(Purple.copy(alpha = 0.18f)))
                        val r = 13f * density
                        g.targets.forEachIndexed { i, o ->
                            if (covered[i]) drawCircle(rainbow[i % rainbow.size], radius = r, center = o)
                        }
                    }
                    trail.forEachIndexed { i, o ->
                        val f = (i + 1f) / trail.size
                        sparkle(o, (6f + 16f * f) * density, Yellow.copy(alpha = f))
                    }
                }
                if (done) {
                    Text("🎉", fontSize = 120.sp, modifier = Modifier.align(Alignment.Center))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BouncyBox(
                    modifier = Modifier.size(72.dp),
                    color = Purple,
                    shape = CircleShape,
                    onClick = {
                        sound.playClick()
                        index = (index - 1 + items.size) % items.size
                    }
                ) { Text(if (rtl) "➡️" else "⬅️", fontSize = 32.sp) }
                BouncyBox(
                    modifier = Modifier.size(72.dp),
                    color = Orange,
                    shape = CircleShape,
                    onClick = { sound.speak(item.shortSpoken(), item.lang) }
                ) { Text("🔊", fontSize = 32.sp) }
                BouncyBox(
                    modifier = Modifier.size(72.dp),
                    color = Purple,
                    shape = CircleShape,
                    onClick = {
                        sound.playClick()
                        index = (index + 1) % items.size
                    }
                ) { Text(if (rtl) "⬅️" else "➡️", fontSize = 32.sp) }
            }
        }
    }
}

// =====================================================================
//  2. ASSOCIER : glisser la lettre sur la bonne image
// =====================================================================

@Composable
fun MatchScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    val pool = remember(lang) { Content.letters(lang) }
    var round by remember { mutableIntStateOf(0) }
    val target = remember(round) { pool.random() }
    val options = remember(round) {
        val others = pool.filter { it.emoji != target.emoji }
            .distinctBy { it.emoji }
            .shuffled()
            .take(2)
        (others + target).shuffled()
    }
    var solved by remember(round) { mutableStateOf(false) }
    var feedback by remember(round) { mutableStateOf("") }
    var dx by remember(round) { mutableFloatStateOf(0f) }
    var dy by remember(round) { mutableFloatStateOf(0f) }
    var letterRect by remember(round) { mutableStateOf(Rect.Zero) }
    var hover by remember(round) { mutableIntStateOf(-1) }
    val rects = remember(round) { HashMap<Int, Rect>() }
    val scope = rememberCoroutineScope()

    fun hitIndex(): Int {
        val c = letterRect.center
        for ((i, r) in rects) if (r.contains(c)) return i
        return -1
    }

    fun springBack() {
        val sx = dx
        val sy = dy
        scope.launch {
            Animatable(sx).animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { dx = value }
        }
        scope.launch {
            Animatable(sy).animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { dy = value }
        }
    }

    fun drop() {
        if (solved) return
        val hit = hitIndex()
        hover = -1
        if (hit >= 0 && options[hit].emoji == target.emoji) {
            solved = true
            feedback = "🎉 ${praise(lang)}"
            sound.playSuccess()
            sound.speak(target.spoken, target.lang)
            onStar()
        } else {
            if (hit >= 0) {
                feedback = retry(lang)
                sound.playError()
            }
            springBack()
        }
    }

    LaunchedEffect(round) {
        delay(300)
        sound.speak(target.shortSpoken(), target.lang)
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(2200)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Associe !", "اربط!"), stars = stars, onBack = onBack)
            Text(
                t(lang, "Glisse la lettre sur la bonne image", "اسحب الحرف إلى الصورة الصحيحة"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Purple,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .offset { IntOffset(dx.roundToInt(), dy.roundToInt()) }
                    .size(130.dp)
                    .onGloballyPositioned { letterRect = it.boundsInRoot() }
                    .alpha(if (solved) 0f else 1f)
                    .background(Purple, RoundedCornerShape(32.dp))
                    .pointerInput(round) {
                        detectDragGestures(
                            onDragStart = { sound.playClick() },
                            onDrag = { change, drag ->
                                change.consume()
                                if (!solved) {
                                    dx += drag.x
                                    dy += drag.y
                                    hover = hitIndex()
                                }
                            },
                            onDragEnd = { drop() },
                            onDragCancel = {
                                hover = -1
                                springBack()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(target.symbol, fontSize = 80.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                feedback,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (solved) Green else Pink,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(40.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEachIndexed { i, o ->
                    val ok = solved && o.emoji == target.emoji
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .graphicsLayer {
                                val s = if (hover == i) 1.15f else 1f
                                scaleX = s
                                scaleY = s
                            }
                            .onGloballyPositioned { rects[i] = it.boundsInRoot() }
                            .background(
                                when {
                                    ok -> Green
                                    hover == i -> Yellow
                                    else -> Color.White
                                },
                                RoundedCornerShape(28.dp)
                            )
                            .border(3.dp, Purple.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(o.emoji, fontSize = 52.sp)
                            if (ok) {
                                Text(target.symbol, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
//  3. HISTOIRES INTERACTIVES
// =====================================================================

@Composable
fun StoryScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf<Story?>(null) }
    val list = remember(lang) {
        when (lang) {
            Lang.FR -> Stories.fr
            Lang.AR -> Stories.ar
            Lang.MIX -> Stories.fr + Stories.ar
        }
    }
    val story = selected
    if (story == null) {
        GameBackground {
            Column(Modifier.fillMaxSize()) {
                TopBar(title = t(lang, "Histoires", "قصص"), stars = stars, onBack = onBack)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    list.forEachIndexed { i, s ->
                        BouncyBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            color = tileColors[i % tileColors.size],
                            onClick = {
                                sound.playClick()
                                selected = s
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(s.emoji, fontSize = 52.sp)
                                Spacer(Modifier.size(12.dp))
                                Text(
                                    s.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        StoryPlayer(
            story = story,
            lang = lang,
            sound = sound,
            stars = stars,
            onStar = onStar,
            onList = { selected = null },
            onHome = onBack
        )
    }
}

@Composable
private fun StoryPlayer(
    story: Story,
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    onStar: () -> Unit,
    onList: () -> Unit,
    onHome: () -> Unit
) {
    var step by remember(story) { mutableIntStateOf(0) }
    var solved by remember(story, step) { mutableStateOf(false) }
    var wrong by remember(story, step) { mutableStateOf<String?>(null) }
    val current = story.steps.getOrNull(step)
    val praiseText = remember(story, step) { praiseFor(story.lang) }
    val retryText = if (story.lang == Lang.AR) Content.retryAr[0] else Content.retryFr[0]

    LaunchedEffect(story, step) {
        delay(400)
        sound.speak(current?.text ?: story.ending, story.lang)
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(1600)
            step += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = story.title, stars = stars, onBack = onList)
            if (current == null) {
                Spacer(Modifier.weight(1f))
                Mascot("🎉", 130)
                Text(
                    story.ending,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Purple,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    BouncyBox(
                        modifier = Modifier.size(90.dp),
                        color = Green,
                        shape = CircleShape,
                        onClick = { step = 0 }
                    ) { Text("🔁", fontSize = 40.sp) }
                    BouncyBox(
                        modifier = Modifier.size(90.dp),
                        color = Blue,
                        shape = CircleShape,
                        onClick = onList
                    ) { Text("📚", fontSize = 40.sp) }
                    BouncyBox(
                        modifier = Modifier.size(90.dp),
                        color = Purple,
                        shape = CircleShape,
                        onClick = onHome
                    ) { Text("🏠", fontSize = 40.sp) }
                }
                Spacer(Modifier.weight(1f))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    story.steps.indices.forEach { i ->
                        Text(if (i < step) "⭐" else if (i == step) "🔸" else "⚪", fontSize = 22.sp)
                    }
                }
                Mascot(current.hero, 100)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(28.dp))
                        .padding(16.dp)
                        .pointerInput(step) {
                            detectTapGestures { sound.speak(current.text, story.lang) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔊  ${current.text}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B2A8C),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    when {
                        solved -> "🎉 $praiseText"
                        wrong != null -> retryText
                        else -> ""
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (solved) Green else Pink,
                    modifier = Modifier.height(40.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    current.options.chunked(2).forEach { rowOpts ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowOpts.forEach { opt ->
                                BouncyBox(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                    color = when {
                                        solved && opt == current.answer -> Green
                                        wrong == opt -> Color.Gray
                                        else -> tileColors[(opt.hashCode() and 0xFF) % tileColors.size]
                                    },
                                    onClick = {
                                        if (solved) return@BouncyBox
                                        if (opt == current.answer) {
                                            solved = true
                                            sound.playSuccess()
                                            onStar()
                                        } else {
                                            wrong = opt
                                            sound.playError()
                                        }
                                    }
                                ) {
                                    Text(opt, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// =====================================================================
//  4. CHASSE AU TRÉSOR : 3 lettres à retrouver, sans pénalité
// =====================================================================

private enum class Phase { PLAY, WIN, TIMEOUT }

@Composable
fun TreasureScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    val pool = remember(lang) { Content.letters(lang) }
    var round by remember { mutableIntStateOf(0) }
    var phase by remember(round) { mutableStateOf(Phase.PLAY) }
    val setup = remember(round) {
        val t3 = pool.shuffled().take(3)
        val others = pool.filter { it !in t3 }.shuffled().take(6)
        t3 to (t3 + others).shuffled()
    }
    val targets = setup.first
    val tiles = setup.second
    val found = remember(round) { mutableStateListOf<String>() }
    val progress = remember { Animatable(0f) }

    // La fusée décolle doucement pendant 30 secondes. Rien ne se perd si elle part.
    LaunchedEffect(round, phase) {
        if (phase == Phase.PLAY) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(30_000, easing = LinearEasing))
            phase = Phase.TIMEOUT
        }
    }
    LaunchedEffect(round) {
        delay(300)
        val names = targets.joinToString(", ") { it.shortSpoken() }
        when (lang) {
            Lang.FR -> sound.speak("Trouve : $names", Lang.FR)
            Lang.AR -> sound.speak("جد: $names", Lang.AR)
            Lang.MIX -> sound.speak("Trouve les trois lettres !", Lang.FR)
        }
    }

    fun tap(tile: Item) {
        if (phase != Phase.PLAY) return
        if (tile in targets && tile.symbol !in found) {
            found.add(tile.symbol)
            onStar()
            if (found.size == targets.size) {
                phase = Phase.WIN
                onStar()
                onStar()
                sound.playSuccess()
                sound.speak(praiseFor(tile.lang), tile.lang)
            } else {
                sound.playStar()
                sound.speak(tile.shortSpoken(), tile.lang)
            }
        } else {
            sound.playClick()
            sound.speak(tile.shortSpoken(), tile.lang)
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Chasse au trésor", "صيد الكنز"), stars = stars, onBack = onBack)

            // Compte à rebours discret : une fusée qui voyage de la Terre à la Lune
            BoxWithConstraints(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1B1464), RoundedCornerShape(28.dp))
            ) {
                Text("🌍", fontSize = 26.sp, modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp))
                Text("🌙", fontSize = 26.sp, modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp))
                Text(
                    "🚀",
                    fontSize = 30.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(((maxWidth - 56.dp) * progress.value).roundToPx(), 0) }
                )
            }
            Spacer(Modifier.height(10.dp))

            // Les 3 lettres à retrouver
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                targets.forEach { tg ->
                    val isFound = tg.symbol in found
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(if (isFound) Green else Color.White, RoundedCornerShape(20.dp))
                            .border(3.dp, Purple.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isFound) "✓" else tg.symbol,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isFound) Color.White else Purple
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            if (phase == Phase.PLAY) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tiles.chunked(3).forEach { rowTiles ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowTiles.forEach { tile ->
                                val isFound = tile in targets && tile.symbol in found
                                BouncyBox(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                    color = if (isFound) Green else tileColors[(tile.symbol.hashCode() and 0xFF) % tileColors.size],
                                    onClick = { tap(tile) }
                                ) {
                                    Text(tile.symbol, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
                Text(
                    if (phase == Phase.WIN) "🎉 ${t(lang, "Bravo ! Tout trouvé !", "برافو! وجدت كل شيء!")}"
                    else "🚀 ${t(lang, "La fusée est partie ! On réessaie ?", "انطلقت الصاروخ! نحاول مرة أخرى؟")}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Purple,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
                BouncyBox(
                    modifier = Modifier.size(width = 200.dp, height = 90.dp),
                    color = Green,
                    onClick = { round += 1 }
                ) { Text("🔁", fontSize = 44.sp) }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

// =====================================================================
//  5. BULLES MAGIQUES : exploration libre, sans enjeu
// =====================================================================

private class Bubble(
    val id: Int,
    val item: Item,
    val x: Float,
    val size: Dp,
    val speed: Float,
    val color: Color,
    y0: Float
) {
    var y by mutableFloatStateOf(y0)
    var popAt by mutableLongStateOf(0L)
}

@Composable
fun BubblesScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    onBack: () -> Unit
) {
    val pool = remember(lang) { Content.letters(lang) + Content.numbers(lang) }
    val bubbles = remember { mutableStateListOf<Bubble>() }

    LaunchedEffect(pool) {
        var nextId = 0
        fun spawn(startY: Float) {
            bubbles.add(
                Bubble(
                    id = nextId++,
                    item = pool.random(),
                    x = Random.nextFloat(),
                    size = (90 + Random.nextInt(60)).dp,
                    speed = 0.10f + Random.nextFloat() * 0.12f,
                    color = tileColors.random(),
                    y0 = startY
                )
            )
        }
        repeat(5) { spawn(0.15f + Random.nextFloat() * 0.8f) }
        var last = 0L
        var timer = 0f
        while (true) {
            androidx.compose.runtime.withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                timer -= dt
                if (timer <= 0f && bubbles.size < 10) {
                    spawn(1.1f)
                    timer = 0.8f
                }
                var i = bubbles.size - 1
                while (i >= 0) {
                    val b = bubbles[i]
                    if (b.popAt == 0L) {
                        b.y -= b.speed * dt
                        if (b.y < -0.3f) bubbles.removeAt(i)
                    } else if (System.nanoTime() - b.popAt > 450_000_000L) {
                        bubbles.removeAt(i)
                    }
                    i--
                }
            }
        }
    }

    fun pop(b: Bubble) {
        if (b.popAt != 0L) return
        b.popAt = System.nanoTime()
        sound.playPop()
        sound.speak(b.item.shortSpoken(), b.item.lang)
    }

    GameBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar(title = t(lang, "Bulles magiques", "فقاعات سحرية"), stars = stars, onBack = onBack)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFBDE9FF), Color(0xFFE8F8FF))))
            ) {
                val w = maxWidth
                val h = maxHeight
                for (b in bubbles) {
                    key(b.id) {
                        val popped = b.popAt != 0L
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(((w - b.size) * b.x).roundToPx(), ((h - b.size) * b.y).roundToPx())
                                }
                                .size(b.size)
                                .pointerInput(Unit) {
                                    detectTapGestures(onPress = { pop(b) })
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!popped) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.9f),
                                                    b.color.copy(alpha = 0.6f),
                                                    b.color.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                        .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        b.item.symbol,
                                        fontSize = (b.size.value * 0.5f).sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Text("✨", fontSize = (b.size.value * 0.6f).sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
//  6. ALBUM D'AUTOCOLLANTS : les étoiles servent de monnaie
// =====================================================================

@Composable
fun StickerScreen(
    lang: Lang,
    sound: SoundManager,
    balance: Int,
    owned: Set<String>,
    onBuy: (Sticker) -> Unit,
    onBack: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    GameBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar(title = t(lang, "Mes autocollants", "ملصقاتي"), stars = balance, onBack = onBack)
            Text(
                if (message.isEmpty()) {
                    t(lang, "Gagne des ⭐ pour débloquer des autocollants !", "اجمع ⭐ لتفتح الملصقات!")
                } else message,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Purple,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(Stickers.all.size) { i ->
                    val s = Stickers.all[i]
                    val isOwned = s.id in owned
                    StickerCell(s, owned = isOwned, canBuy = balance >= s.price) {
                        if (isOwned) {
                            sound.playStar()
                        } else if (balance >= s.price) {
                            onBuy(s)
                            sound.playSuccess()
                            message = "🎉 ${praise(lang)}"
                        } else {
                            sound.playError()
                            message = t(
                                lang,
                                "Il te faut encore ${s.price - balance} ⭐",
                                "تحتاج ${s.price - balance} ⭐ أخرى"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StickerCell(s: Sticker, owned: Boolean, canBuy: Boolean, onClick: () -> Unit) {
    val pop = remember { Animatable(1f) }
    LaunchedEffect(owned) {
        if (owned) {
            pop.snapTo(0.3f)
            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    val transition = rememberInfiniteTransition(label = "wiggle")
    val rot by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rot"
    )
    BouncyBox(
        modifier = Modifier.aspectRatio(1f),
        color = when {
            owned -> Color.White
            canBuy -> Yellow.copy(alpha = 0.55f)
            else -> Color(0xFFE0DCE8)
        },
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        if (owned) {
            Text(
                s.emoji,
                fontSize = 54.sp,
                modifier = Modifier
                    .rotate(rot)
                    .graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                    }
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(s.emoji, fontSize = 40.sp, modifier = Modifier.alpha(0.25f))
                Text(
                    if (canBuy) "⭐ ${s.price}" else "🔒 ${s.price}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )
            }
        }
    }
}
