package com.kidslearn.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection


// ---------- Couleurs & thème ----------

val Purple = Color(0xFF6C4BFF)
val Pink = Color(0xFFFF5FA2)
val Orange = Color(0xFFFF9F43)
val Green = Color(0xFF2ED573)
val Blue = Color(0xFF1E90FF)
val Yellow = Color(0xFFFFC933)
val Cream = Color(0xFFFFF4E0)

val tileColors = listOf(Pink, Orange, Green, Blue, Purple, Yellow)

@Composable
fun KidsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Purple, background = Cream),
        content = content
    )
}

// ---------- Briques réutilisables ----------

/** Bouton/tuile qui rebondit quand on appuie dessus. */
@Composable
fun BouncyBox(
    modifier: Modifier = Modifier,
    color: Color,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(28.dp),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .background(color, shape)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun GameBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF4E0), Color(0xFFFFE0F0))))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { content() }
}

@Composable
fun TopBar(title: String, stars: Int?, onBack: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            BouncyBox(
                modifier = Modifier.size(64.dp),
                color = Purple,
                shape = CircleShape,
                onClick = onBack
            ) { Text("🏠", fontSize = 30.sp) }
        } else {
            Spacer(Modifier.size(64.dp))
        }
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3B2A8C)
        )
        if (stars != null) {
            Box(
                modifier = Modifier
                    .background(Yellow, RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) { Text("⭐ $stars", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        } else {
            Spacer(Modifier.size(64.dp))
        }
    }
}

/** Petit personnage qui rebondit en continu. */
@Composable
fun Mascot(emoji: String, size: Int = 90) {
    val tr = rememberInfiniteTransition(label = "mascot")
    val dy by tr.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dy"
    )
    Text(emoji, fontSize = size.sp, modifier = Modifier.graphicsLayer { translationY = dy })
}

// ---------- Accueil ----------

@Composable
fun HomeScreen(
    stars: Int,
    streak: Int,
    childName: String,
    avatar: String,
    sound: SoundManager,
    musicOn: Boolean,
    onToggleMusic: () -> Unit,
    onPickLang: (Lang) -> Unit
) {
    LaunchedEffect(Unit) {
        sound.speak("Salut $childName ! Bienvenue chez Petits Génies !", Lang.FR)
    }
    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .background(Yellow, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) { Text("⭐ $stars", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                BouncyBox(
                    modifier = Modifier.size(56.dp),
                    color = if (musicOn) Green else Color.LightGray,
                    shape = CircleShape,
                    onClick = onToggleMusic
                ) { Text(if (musicOn) "🎵" else "🔇", fontSize = 28.sp) }
            }
            Spacer(Modifier.height(8.dp))
            if (streak > 1) {
                Box(
                    modifier = Modifier
                        .background(Orange, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "🔥 $streak jours de suite !",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            Zizou(170.dp)
            Text(
                "Salut $childName ! 👋",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Pink
            )
            Text(
                "Petits Génies",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple
            )
            Text(
                "عباقرة صغار",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Pink
            )
            Spacer(Modifier.height(28.dp))

            val options = listOf(
                Triple(Lang.FR, "🇫🇷  Français", Blue),
                Triple(Lang.AR, "العربية  🌙", Green),
                Triple(Lang.EN, "🇬🇧  English", Pink),
                Triple(Lang.MIX, "Français + العربية", Orange)
            )
            options.forEach { (lang, label, color) ->
                BouncyBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .padding(vertical = 5.dp),
                    color = color,
                    onClick = { onPickLang(lang) }
                ) {
                    Text(label, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}

// ---------- Menu ----------

private data class MenuEntry(val screen: Screen, val emoji: String, val label: String)

fun openVoiceSettings(ctx: Context) {
    try {
        ctx.startActivity(
            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        try {
            ctx.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
        }
    }
}

@Composable
fun MenuScreen(
    lang: Lang,
    stars: Int,
    sound: SoundManager,
    onBack: () -> Unit,
    onPick: (Screen) -> Unit
) {
    val entries = listOf(
        MenuEntry(Screen.LEARN_LETTERS, "🔤", t(lang, "Les lettres", "الحروف")),
        MenuEntry(Screen.LEARN_NUMBERS, "🔢", t(lang, "Les chiffres", "الأرقام")),
        MenuEntry(Screen.COLORS, "🎨", t(lang, "Les couleurs", "الألوان")),
        MenuEntry(Screen.SHAPES, "🔷", t(lang, "Les formes", "الأشكال")),
        MenuEntry(Screen.COUNT, "🍎", t(lang, "Compter", "العدّ")),
        MenuEntry(Screen.FIRST_LETTER, "🖼️", t(lang, "Première lettre", "أول حرف")),
        MenuEntry(Screen.BIG_SMALL, "🐘", t(lang, "Petit ou grand", "صغير أم كبير؟")),
        MenuEntry(Screen.CONNECT, "🔗", t(lang, "Relier", "اربط")),
        MenuEntry(Screen.BASKET, "🧺", t(lang, "Le panier", "السلة")),
        MenuEntry(Screen.ODD, "🔍", t(lang, "Trouver l'intrus", "جد الغريب")),
        MenuEntry(Screen.SORT, "🧺", t(lang, "Tri", "الفرز")),
        MenuEntry(Screen.LOGIC, "🧠", t(lang, "Logique", "المنطق")),
        MenuEntry(Screen.SHADOW, "🌑", t(lang, "Puzzles : ombres", "ألغاز الظلال")),
        MenuEntry(Screen.HIDE_LETTERS, "🙈", t(lang, "Cache-cache lettres", "غميضة الحروف")),
        MenuEntry(Screen.HIDE_NUMBERS, "🙉", t(lang, "Cache-cache chiffres", "غميضة الأرقام")),
        MenuEntry(Screen.QUIZ_LETTERS, "🎯", t(lang, "Écoute les lettres", "اسمع الحروف")),
        MenuEntry(Screen.QUIZ_NUMBERS, "🎲", t(lang, "Écoute les chiffres", "اسمع الأرقام")),
        MenuEntry(Screen.MEMORY, "🃏", t(lang, "Memory", "الذاكرة")),
        MenuEntry(Screen.TRACE, "✏️", t(lang, "Tracer", "ارسم")),
        MenuEntry(Screen.MATCH, "🧩", t(lang, "Associer", "اربط")),
        MenuEntry(Screen.STORY, "📖", t(lang, "Histoires", "قصص")),
        MenuEntry(Screen.BUBBLES, "🧼", t(lang, "Bulles magiques", "فقاعات سحرية")),
        MenuEntry(Screen.TREASURE, "🚀", t(lang, "Chasse au trésor", "صيد الكنز")),
        MenuEntry(Screen.STICKERS, "🏅", t(lang, "Mes autocollants", "ملصقاتي")),
        MenuEntry(Screen.PARENTS, "👨‍👩‍👧", t(lang, "Zone parents", "منطقة الآباء"))
    )
    val ctx = LocalContext.current
    GameBackground {
        Column(Modifier.fillMaxSize()) {
            TopBar(title = t(lang, "On joue !", "هيا نلعب!"), stars = stars, onBack = onBack)
            if ((lang == Lang.AR || lang == Lang.MIX) && !sound.arabicOk) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(Orange, RoundedCornerShape(20.dp))
                        .clickable { openVoiceSettings(ctx) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Parents : la voix arabe n'est pas installée. Touchez ici pour l'installer.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Menu en nid d'abeille : rangées de 2 hexagones, une rangée sur deux décalée.
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val gap = 6.dp
                val tileW = (maxWidth - 16.dp - gap * 2) / 2.5f
                val tileH = tileW * 1.1547f
                val rowShift = (tileW + gap) / 2f
                val rows = entries.chunked(2)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    rows.forEachIndexed { r, row ->
                        Row(
                            modifier = Modifier
                                .offset(x = if (r % 2 == 0) -rowShift / 2f else rowShift / 2f)
                                .height(tileH * 0.75f),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            row.forEachIndexed { c, e ->
                                HexTile(
                                    emoji = e.emoji,
                                    label = e.label,
                                    color = tileColors[(r * 2 + c) % tileColors.size],
                                    width = tileW,
                                    onClick = { onPick(e.screen) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(tileH * 0.25f + 16.dp))
                }
            }
        }
    }
}

// ---------- Apprendre (cartes) ----------

/** Les 4 formes d'une lettre arabe : isolée, finale, médiane, initiale (lues de droite à gauche). */
@Composable
fun ArabicForms(symbol: String) {
    val forms = arabicForms(symbol)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            forms.forEach { f ->
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text(f, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LearnScreen(
    items: List<Item>,
    lang: Lang,
    sound: SoundManager,
    onBack: () -> Unit,
    onTest: (() -> Unit)? = null
) {
    var index by remember { mutableIntStateOf(0) }
    val item = items[index]
    val pop = remember { Animatable(1f) }
    val rtl = lang == Lang.AR

    LaunchedEffect(index) {
        sound.speak(item.spoken, item.lang)
        pop.snapTo(0.6f)
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = "${index + 1} / ${items.size}", stars = null, onBack = onBack)
            Spacer(Modifier.height(8.dp))
            BouncyBox(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer { scaleX = pop.value; scaleY = pop.value },
                color = tileColors[index % tileColors.size],
                shape = RoundedCornerShape(40.dp),
                onClick = {
                    sound.playClick()
                    sound.speak(item.spoken, item.lang)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.symbol, fontSize = 120.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    if (isArabicLetter(item.symbol)) {
                        ArabicForms(item.symbol)
                    }
                    Mascot(item.emoji, 72)
                    Text(
                        item.word,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text("🔊", fontSize = 32.sp)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BouncyBox(
                    modifier = Modifier.size(88.dp),
                    color = Purple,
                    shape = CircleShape,
                    onClick = {
                        sound.playClick()
                        index = (index - 1 + items.size) % items.size
                    }
                ) { Text(if (rtl) "➡️" else "⬅️", fontSize = 40.sp) }
                if (onTest != null) {
                    // Après l'apprentissage : le test ! (met en valeur le bouton à la dernière carte)
                    BouncyBox(
                        modifier = Modifier
                            .height(88.dp)
                            .defaultMinSize(minWidth = 88.dp)
                            .padding(horizontal = 4.dp),
                        color = if (index == items.size - 1) Green else Orange,
                        shape = RoundedCornerShape(44.dp),
                        onClick = {
                            sound.playClick()
                            onTest()
                        }
                    ) {
                        Text(
                            "🎯 " + t(lang, "Test !", "اختبار!", "Test!"),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                BouncyBox(
                    modifier = Modifier.size(88.dp),
                    color = Purple,
                    shape = CircleShape,
                    onClick = {
                        sound.playClick()
                        index = (index + 1) % items.size
                    }
                ) { Text(if (rtl) "⬅️" else "➡️", fontSize = 40.sp) }
            }
        }
    }
}

// ---------- Jeu : trouve la lettre / le chiffre ----------

fun praise(lang: Lang): String = when (lang) {
    Lang.FR -> Content.praiseFr.random()
    Lang.AR -> Content.praiseAr.random()
    Lang.EN -> Content.praiseEn.random()
    Lang.MIX -> (Content.praiseFr + Content.praiseAr).random()
}

fun retry(lang: Lang): String = when (lang) {
    Lang.FR -> Content.retryFr.random()
    Lang.AR -> Content.retryAr.random()
    Lang.EN -> Content.retryEn.random()
    Lang.MIX -> (Content.retryFr + Content.retryAr).random()
}

@Composable
fun QuizScreen(
    pool: List<Item>,
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    distractors: Int = 2,
    childName: String = "",
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val target = remember(round) { pool.random() }
    val choices = remember(round) {
        (pool.filter { it.lang == target.lang && it.symbol != target.symbol }
            .shuffled().take(distractors) + target).shuffled()
    }
    var feedback by remember(round) { mutableStateOf("") }
    var solved by remember(round) { mutableStateOf(false) }
    var wrongSymbol by remember(round) { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(round) {
        delay(300)
        sound.speak(target.spoken, target.lang)
    }

    LaunchedEffect(wrongSymbol) {
        if (wrongSymbol != null) {
            for (i in 0 until 3) {
                shake.animateTo(16f, tween(50)); shake.animateTo(-16f, tween(50))
            }
            shake.animateTo(0f, tween(50))
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(
                title = t(lang, "Écoute et touche !", "اسمع واختر!"),
                stars = stars,
                onBack = onBack
            )
            BouncyBox(
                modifier = Modifier.size(width = 200.dp, height = 90.dp),
                color = Orange,
                onClick = { sound.speak(target.spoken, target.lang) }
            ) { Text("🔊  ?", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
            Spacer(Modifier.height(8.dp))
            Text(
                feedback,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (solved) Green else Pink,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(44.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                choices.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { c ->
                            val isWrong = wrongSymbol == c.symbol
                            BouncyBox(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .graphicsLayer { if (isWrong) translationX = shake.value },
                                color = when {
                                    solved && c.symbol == target.symbol -> Green
                                    isWrong -> Color.Gray
                                    else -> tileColors[(c.symbol.hashCode() and 0xFF) % tileColors.size]
                                },
                                onClick = {
                                    if (solved) return@BouncyBox
                                    if (c.symbol == target.symbol) {
                                        solved = true
                                        val p = praiseNamed(target.lang, childName)
                                        feedback = "🎉 $p"
                                        sound.playSuccess()
                                        sound.speak(p, target.lang)
                                        onStar()
                                    } else {
                                        wrongSymbol = c.symbol
                                        feedback = retry(lang)
                                        sound.playError()
                                    }
                                }
                            ) {
                                Text(
                                    c.symbol,
                                    fontSize = 90.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Passer à la question suivante après un succès
    LaunchedEffect(solved) {
        if (solved) {
            delay(1800)
            round += 1
        }
    }
}

// ---------- Jeu : compter ----------

@Composable
fun CountScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String = "",
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val count = remember(round) { (1..10).random() }
    val emoji = remember(round) { Content.countEmojis.random() }
    val roundLang = remember(round) {
        when (lang) {
            Lang.MIX -> listOf(Lang.FR, Lang.AR).random()
            else -> lang
        }
    }
    val numberItems = Content.digitsFor(roundLang)
    val choices = remember(round) {
        ((1..10).filter { it != count }.shuffled().take(2) + count).shuffled()
    }
    var solved by remember(round) { mutableStateOf(false) }
    var feedback by remember(round) { mutableStateOf("") }

    val question = when (roundLang) {
        Lang.AR -> "كم عددها؟"
        Lang.EN -> "How many are there?"
        else -> "Combien y en a-t-il ?"
    }

    LaunchedEffect(round) {
        delay(300)
        sound.speak(question, roundLang)
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(2200)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "On compte !", "هيا نعدّ!"), stars = stars, onBack = onBack)
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(32.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    (0 until count).toList().chunked(5).forEach { rowIdx ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowIdx.forEach { i ->
                                CountItem(emoji, i, onTap = {
                                    sound.playStar()
                                    val n = i + 1
                                    sound.speak(
                                        Content.digitsFor(roundLang)[n].spoken,
                                        roundLang
                                    )
                                })
                            }
                        }
                    }
                }
            }
            Text(
                if (feedback.isEmpty()) question else feedback,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (solved) Green else Purple,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                choices.forEach { n ->
                    BouncyBox(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        color = if (solved && n == count) Green else tileColors[n % tileColors.size],
                        onClick = {
                            if (solved) return@BouncyBox
                            if (n == count) {
                                solved = true
                                feedback = "🎉 ${praiseNamed(roundLang, childName)}"
                                sound.playSuccess()
                                onStar()
                            } else {
                                feedback = retry(roundLang)
                                sound.playError()
                            }
                        }
                    ) {
                        Text(
                            numberItems[n].symbol,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/** Un objet à compter : apparaît avec une petite animation, rebondit au toucher. */
@Composable
private fun CountItem(emoji: String, index: Int, onTap: () -> Unit) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 90L)
        appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Text(
        emoji,
        fontSize = 48.sp,
        modifier = Modifier
            .graphicsLayer {
                val s = appear.value * (if (pressed) 1.3f else 1f)
                scaleX = s; scaleY = s
            }
            .clickable(interactionSource = source, indication = null, onClick = onTap)
            .padding(4.dp)
    )
}
