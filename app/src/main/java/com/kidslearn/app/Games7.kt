package com.kidslearn.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// =====================================================================
//  Jeux v6 : glisser-déposer qui suit le doigt, cache-cache, panier, moitiés...
// =====================================================================

private val noRect = Rect(0f, 0f, 0f, 0f)

// ---------- Tri : glisse l'objet dans le bon panier ----------

@Composable
fun SortScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val pair = remember(round / 4) { Things.families.shuffled().take(2) }
    val item = remember(round) {
        val f = pair.random()
        Pair(f, f.things.random())
    }
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val zones = remember { arrayOf(noRect, noRect) }
    val ask = t(lang, "Glisse-le dans le bon panier !", "اِسْحَبْهُ إِلَى السَّلَّةِ الصَّحِيحَة!", "Drag it to the right basket!")
    val sl = speakLang(lang)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p + " " + item.second.name(sl), sl); onStar()
            delay(2300); round += 1
        }
    }

    fun answer(idx: Int): Boolean {
        if (found) return true
        return if (pair[idx] === item.first) {
            found = true; message = "🎉 " + praise(lang); true
        } else {
            wrongs += 1; sound.playError()
            message = retry(lang); sound.speak(message, sl); false
        }
    }

    GameShell(
        t(lang, "Le tri", "الفَرْز", "Sorting"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            key(round) {
                Draggable(
                    onTap = { sound.speak(item.second.name(sl), sl) },
                    onDrop = { p, _ ->
                        val idx = zones.indexOfFirst { rectHas(it, p) }
                        if (idx < 0) false else answer(idx)
                    }
                ) { Text(item.second.emoji, fontSize = 120.sp, modifier = Modifier.padding(16.dp)) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pair.forEachIndexed { i, fam ->
                BouncyBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .onGloballyPositioned { zones[i] = it.boundsInRoot() },
                    color = if (wrongs >= 2 && !found && fam === item.first) Yellow else if (i == 0) Blue else Orange,
                    shape = RoundedCornerShape(32.dp),
                    onClick = { answer(i) }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧺", fontSize = 48.sp)
                        Text(fam.icon, fontSize = 30.sp)
                        Text(fam.name(lang), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ---------- Moitié-moitié : complète l'image ----------

@Composable
private fun Half(emoji: String, left: Boolean, size: Dp) {
    Box(Modifier.size(width = size / 2f, height = size).clipToBounds()) {
        Box(
            Modifier.requiredSize(size).offset(x = if (left) 0.dp else -(size / 2f)),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = (size.value * 0.78f).sp) }
    }
}

@Composable
fun HalvesScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val setup = remember(round) {
        val all = Things.all.shuffled()
        Pair(all[0], (listOf(all[0]) + all.drop(1).take(2)).shuffled())
    }
    val (target, choices) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    var slot by remember { mutableStateOf(noRect) }
    val ask = t(lang, "Trouve l'autre moitié !", "اِبْحَثْ عَنِ النِّصْفِ الآخَر!", "Find the other half!")
    val sl = speakLang(lang)
    val appear = remember { Animatable(0f) }

    LaunchedEffect(round) { appear.snapTo(0f); appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p + " " + target.name(sl), sl); onStar()
            delay(2600); round += 1
        }
    }

    fun check(th: Thing): Boolean {
        if (found) return true
        return if (th === target) {
            found = true; message = "🎉 " + praise(lang); true
        } else {
            wrongs += 1; sound.playError()
            message = retry(lang); sound.speak(message, sl); false
        }
    }

    GameShell(
        t(lang, "Moitié-moitié", "نِصْف وَنِصْف", "Half and half"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        Box(
            Modifier.weight(1f).fillMaxWidth().graphicsLayer { scaleX = appear.value; scaleY = appear.value },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(28.dp)).padding(8.dp)
            ) {
                Half(target.emoji, left = true, size = 220.dp)
                Box(
                    Modifier
                        .size(width = 110.dp, height = 220.dp)
                        .onGloballyPositioned { slot = it.boundsInRoot() }
                        .background(if (found) Color.Transparent else Color(0x22000000), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (found) Half(target.emoji, left = false, size = 220.dp)
                    else Text("❓", fontSize = 48.sp)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            choices.forEachIndexed { i, th ->
                key(round, i) {
                    Draggable(
                        onTap = { check(th) },
                        onDrop = { p, _ -> if (rectHas(slot, p)) check(th) else false }
                    ) {
                        Box(
                            Modifier
                                .alpha(if (found && th === target) 0f else 1f)
                                .background(tileColors[i % tileColors.size], RoundedCornerShape(24.dp))
                                .padding(12.dp)
                        ) { Half(th.emoji, left = false, size = 120.dp) }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ---------- Cache-cache : déplace les objets pour retrouver ce qui se cache ----------

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
    val covers = remember { listOf("🌳", "🏠", "🚗", "⛺", "🌲", "🪨", "🌴", "🌵", "📦", "🚪") }
    val density = LocalDensity.current.density
    var round by remember { mutableIntStateOf(0) }
    val hidden = remember(round) { pool.shuffled().take(6) }
    val coverSet = remember(round) { covers.shuffled().take(6) }
    val target = remember(round) { hidden.random() }
    val revealed = remember(round) { mutableStateListOf<Int>() }
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    var draggingIdx by remember { mutableIntStateOf(-1) }
    val sl = speakLang(lang)
    val what = target.shortSpoken()
    val ask = t(lang, "Cherche : $what ! Déplace les objets.", "اِبْحَثْ عَنْ: $what! حَرِّكِ الأَشْيَاء.", "Find: $what! Move the objects.")

    LaunchedEffect(round) { delay(500); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2600); round += 1
        }
    }

    fun reveal(i: Int) {
        if (i in revealed || found) return
        revealed.add(i)
        sound.playPop()
        val it = hidden[i]
        if (it.symbol == target.symbol) {
            found = true; message = "🎉 " + praise(lang)
        } else {
            wrongs += 1; sound.playError()
            val said = it.shortSpoken()
            message = t(lang, "Ce n'est pas $what, c'est $said. Cherche encore !",
                "لَيْسَ $what، إِنَّهُ $said. اِبْحَثْ مِنْ جَدِيد!", "It is not $what, it is $said. Keep looking!")
            sound.speak(message, sl)
        }
    }

    GameShell(
        t(lang, "Cache-cache", "الغُمَّيْضَة", "Hide and seek"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (r in 0 until 3) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .zIndex(if (draggingIdx in (r * 2)..(r * 2 + 1)) 10f else 0f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (c in 0 until 2) {
                        val i = r * 2 + c
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .zIndex(if (draggingIdx == i) 10f else 0f)
                                .background(Color(0x33FFFFFF), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // ce qui se cache (visible seulement une fois l'objet déplacé)
                            if (i in revealed) {
                                val isTarget = hidden[i].symbol == target.symbol
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        hidden[i].symbol,
                                        fontSize = if (hidden[i].symbol == hidden[i].emoji) 64.sp else 54.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isTarget) Green else Purple
                                    )
                                    if (hidden[i].symbol != hidden[i].emoji && hidden[i].emoji != NUMBER_EMOJI) {
                                        Text(hidden[i].emoji, fontSize = 28.sp)
                                    }
                                }
                            }
                            key(round, i) {
                                val glow = wrongs >= 2 && !found && hidden[i].symbol == target.symbol
                                Draggable(
                                    onDragState = { d -> draggingIdx = if (d) i else if (draggingIdx == i) -1 else draggingIdx },
                                    onDrop = { _, dist ->
                                        if (dist >= 56f * density) { reveal(i); true } else false
                                    }
                                ) {
                                    Text(
                                        coverSet[i], fontSize = 84.sp,
                                        modifier = Modifier
                                            .then(if (glow) Modifier.background(Yellow.copy(alpha = 0.6f), CircleShape) else Modifier)
                                            .padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Le panier : choisis ce qui passe sur le tapis ----------

@Composable
fun BasketScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    val goal = 3
    var round by remember { mutableIntStateOf(0) }
    val fam = remember(round) { Things.families.random() }
    val belt = remember(round) {
        val others = Things.families.filter { it !== fam }.flatMap { it.things }
        (fam.things.shuffled().take(goal) + others.shuffled().take(4)).shuffled()
    }
    val collected = remember(round) { mutableStateListOf<Int>() }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val sl = speakLang(lang)
    val ask = t(
        lang, "Mets ${fam.fr} dans le panier !", "ضَعِ ${fam.ar} فِي السَّلَّة!", "Put the ${fam.en} in the basket!"
    )
    val bounce = remember { Animatable(1f) }
    val tr = rememberInfiniteTransition(label = "belt")
    val phase by tr.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(collected.size) {
        if (collected.size > 0) {
            bounce.animateTo(1.35f, tween(120))
            bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }
    LaunchedEffect(collected.size) {
        if (collected.size >= goal && !found) {
            found = true
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2600); round += 1
        }
    }

    GameShell(
        t(lang, "Le panier", "السَّلَّة", "The basket"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val d = LocalDensity.current.density
            val itemW = 104.dp
            val itemWpx = 104f * d
            val totalPx = constraints.maxWidth + itemWpx
            val beltYpx = (constraints.maxHeight * 0.18f).roundToInt()
            // le tapis roulant
            Box(
                Modifier
                    .offset { IntOffset(0, beltYpx + (itemWpx - 18f * d).roundToInt()) }
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(Color(0xFF5A5A7A), RoundedCornerShape(11.dp))
            )
            belt.forEachIndexed { i, th ->
                if (i !in collected) {
                    AnswerTile(
                        modifier = Modifier
                            .offset {
                                // la position est lue ici (phase de mise en page) : pas de recomposition à chaque image
                                val frac = 1f - ((i.toFloat() / belt.size + phase) % 1f)
                                IntOffset((totalPx * frac - itemWpx).roundToInt(), beltYpx)
                            }
                            .size(itemW),
                        color = tileColors[i % tileColors.size],
                        correct = th in fam.things,
                        enabled = !found,
                        onCorrect = {
                            collected.add(i)
                            sound.playPop()
                            sound.speak(th.name(sl), sl)
                            message = "👍"
                        },
                        onWrong = {
                            sound.playError()
                            message = t(lang, "Ce n'est pas ${fam.fr}", "لَيْسَ مِنْ ${fam.ar}", "Not ${fam.en}")
                            sound.speak(retry(lang), sl)
                        }
                    ) { Text(th.emoji, fontSize = 56.sp) }
                }
            }
            // le panier et ce qu'il contient
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    collected.joinToString(" ") { belt[it].emoji }.ifEmpty { " " },
                    fontSize = 30.sp
                )
                Text(
                    "🧺",
                    fontSize = 96.sp,
                    modifier = Modifier.graphicsLayer { scaleX = bounce.value; scaleY = bounce.value }
                )
                Text(
                    "${collected.size} / $goal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Purple
                )
            }
        }
    }
}

// ---------- Petit, moyen ou grand ----------

@Composable
fun BigSmallScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val thing = remember(round) { Things.all.random() }
    val order = remember(round) { listOf(0, 1, 2).shuffled() } // 0 petit, 1 moyen, 2 grand
    val want = remember(round) { (0..2).random() }
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val sl = speakLang(lang)
    val ask = when (want) {
        0 -> t(lang, "Touche le PLUS PETIT !", "المَسِ الأَصْغَر!", "Touch the SMALLEST one!")
        1 -> t(lang, "Touche celui du MILIEU !", "المَسِ المُتَوَسِّط!", "Touch the MIDDLE one!")
        else -> t(lang, "Touche le PLUS GRAND !", "المَسِ الأَكْبَر!", "Touch the BIGGEST one!")
    }
    val heights = listOf(110.dp, 170.dp, 240.dp)
    val fonts = listOf(44.sp, 74.sp, 112.sp)

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2300); round += 1
        }
    }

    GameShell(
        t(lang, "Petit, moyen, grand", "صَغِير، مُتَوَسِّط، كَبِير", "Small, medium, big"),
        ask, message, found, found, stars, onBack, onRepeat = { sound.speak(ask, sl) }
    ) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            order.forEachIndexed { pos, sizeIdx ->
                AnswerTile(
                    modifier = Modifier.weight(1f).height(heights[sizeIdx]),
                    color = tileColors[pos % tileColors.size],
                    correct = sizeIdx == want,
                    glow = wrongs >= 2 && !found && sizeIdx == want,
                    enabled = !found,
                    onCorrect = { found = true; message = "🎉 " + praise(lang) },
                    onWrong = {
                        wrongs += 1; sound.playError()
                        message = retry(lang); sound.speak(message, sl)
                    }
                ) { Text(thing.emoji, fontSize = fonts[sizeIdx]) }
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

// ---------- Relier : le mot (lu à voix haute) à son image ----------

@Composable
fun ConnectScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val fam = remember(round) { Things.families.random() }
    val pairs = remember(round) { fam.things.shuffled().take(3) }
    val rightOrder = remember(round) { pairs.shuffled() }
    var matched by remember(round) { mutableStateOf(setOf<Int>()) }
    var dragFrom by remember(round) { mutableStateOf<Int?>(null) }
    var dragPos by remember(round) { mutableStateOf<Offset?>(null) }
    var message by remember(round) { mutableStateOf("") }
    var board by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current.density
    val sl = speakLang(lang)
    val ask = t(lang, "Relie chaque mot à son image !", "اِرْبِطْ كُلَّ كَلِمَةٍ بِصُورَتِهَا!", "Connect each word to its picture!")

    fun left(i: Int) = Offset(board.width * 0.24f, board.height * (0.17f + 0.33f * i))
    fun right(j: Int) = Offset(board.width * 0.80f, board.height * (0.17f + 0.33f * j))

    LaunchedEffect(round) { delay(400); sound.speak(ask, sl) }
    LaunchedEffect(matched.size) {
        if (matched.size == 3) {
            val p = praiseNamed(lang, childName)
            message = "🏆 $p"
            sound.playSuccess(); sound.speak(p, sl); onStar()
            delay(2800); round += 1
        }
    }

    GameShell(
        t(lang, "Relier", "الرَّبْط", "Connect"), ask, message, matched.size == 3, matched.size == 3, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(32.dp))
                .onSizeChanged { board = it }
                .pointerInput(round) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val li = (0..2).firstOrNull { i ->
                            i !in matched && (left(i) - down.position).getDistance() < 80f * density
                        }
                        if (li != null) {
                            dragFrom = li
                            dragPos = down.position
                            sound.playClick()
                            sound.speak(pairs[li].name(sl), sl) // dit le mot : « le vélo »
                            do {
                                val ev = awaitPointerEvent()
                                ev.changes.forEach { it.consume() }
                                dragPos = ev.changes.first().position
                            } while (ev.changes.any { it.pressed })
                            val end = dragPos
                            if (end != null) {
                                val rj = (0..2).firstOrNull { j -> (right(j) - end).getDistance() < 80f * density }
                                if (rj != null && rightOrder[rj] === pairs[li]) {
                                    matched = matched + li
                                    message = praise(lang)
                                    sound.playSuccess()
                                } else if (rj != null) {
                                    message = retry(lang)
                                    sound.playError()
                                    sound.speak(message, sl)
                                }
                            }
                            dragFrom = null
                            dragPos = null
                        }
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                matched.forEach { i ->
                    val j = rightOrder.indexOfFirst { it === pairs[i] }
                    if (j >= 0) drawLine(Green, left(i), right(j), strokeWidth = 12f * density, cap = StrokeCap.Round)
                }
                val df = dragFrom
                val dp0 = dragPos
                if (df != null && dp0 != null) {
                    drawLine(Orange, left(df), dp0, strokeWidth = 12f * density, cap = StrokeCap.Round)
                    drawCircle(Orange, radius = 16f * density, center = dp0)
                }
            }
            // mots à gauche
            pairs.forEachIndexed { i, th ->
                val a = left(i)
                val w = 150f * density
                val h = 74f * density
                Box(
                    modifier = Modifier
                        .offset { IntOffset((a.x - w / 2f).roundToInt(), (a.y - h / 2f).roundToInt()) }
                        .size(width = 150.dp, height = 74.dp)
                        .background(if (i in matched) Green else Purple, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔊 " + th.label(lang),
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // images à droite
            rightOrder.forEachIndexed { j, th ->
                val a = right(j)
                val s = 92f * density
                val done = pairs.indexOfFirst { it === th } in matched
                val sc by animateFloatAsState(
                    targetValue = if (done) 1.25f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "pic"
                )
                Box(
                    modifier = Modifier
                        .offset { IntOffset((a.x - s / 2f).roundToInt(), (a.y - s / 2f).roundToInt()) }
                        .size(92.dp)
                        .graphicsLayer { scaleX = sc; scaleY = sc }
                        .background(if (done) Color(0xFFD7F7D0) else Color(0xFFFFF1C9), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(th.emoji, fontSize = 54.sp) }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ---------- Associer : glisse la lettre sur son image ----------

@Composable
fun MatchScreen(
    lang: Lang, sound: SoundManager, stars: Int, childName: String = "",
    onStar: () -> Unit, onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val setup = remember(round) {
        val all = Content.letters(lang).shuffled()
        val target = all[0]
        val others = all.drop(1).filter { it.emoji != target.emoji }.distinctBy { it.emoji }.take(2)
        Pair(target, (listOf(target) + others).shuffled())
    }
    val (target, choices) = setup
    var wrongs by remember(round) { mutableIntStateOf(0) }
    var found by remember(round) { mutableStateOf(false) }
    var message by remember(round) { mutableStateOf("") }
    val zones = remember { arrayOf(noRect, noRect, noRect) }
    val sl = speakLang(lang)
    val ask = t(lang, "Glisse la lettre sur son image !", "اِسْحَبِ الحَرْفَ إِلَى صُورَتِهِ!", "Drag the letter to its picture!")

    LaunchedEffect(round) { delay(400); sound.speak(ask, if (target.lang == Lang.AR) Lang.AR else sl) }
    LaunchedEffect(found) {
        if (found) {
            val p = praiseNamed(lang, childName)
            sound.playSuccess(); sound.speak(p, sl); sound.speak(target.spoken, target.lang); onStar()
            delay(2800); round += 1
        }
    }

    fun answer(idx: Int): Boolean {
        if (found) return true
        return if (choices[idx].emoji == target.emoji) {
            found = true; message = "🎉 " + praise(lang); true
        } else {
            wrongs += 1; sound.playError()
            message = retry(lang); sound.speak(message, sl); false
        }
    }

    GameShell(
        t(lang, "Associer", "الرَّبْط", "Match"), ask, message, found, found, stars, onBack,
        onRepeat = { sound.speak(ask, sl) }
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            key(round) {
                Draggable(
                    onTap = { sound.speak(target.shortSpoken(), target.lang) },
                    onDrop = { p, _ ->
                        val idx = zones.indexOfFirst { rectHas(it, p) }
                        if (idx < 0) false else answer(idx)
                    }
                ) {
                    Box(
                        Modifier
                            .size(150.dp)
                            .background(Purple, RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text(target.symbol, fontSize = 96.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            choices.forEachIndexed { i, c ->
                BouncyBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .onGloballyPositioned { zones[i] = it.boundsInRoot() },
                    color = if (wrongs >= 2 && !found && c.emoji == target.emoji) Yellow else tileColors[i % tileColors.size],
                    shape = RoundedCornerShape(28.dp),
                    onClick = { answer(i) }
                ) { Text(c.emoji, fontSize = 64.sp) }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
