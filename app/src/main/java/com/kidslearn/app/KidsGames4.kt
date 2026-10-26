package com.kidslearn.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
// Nouveaux jeux pour 4-5 ans (Petits Génies v2)
// Règles pédagogiques appliquées :
//  - 3 propositions maximum (charge mentale adaptée à 4-5 ans)
//  - tout est lu à voix haute (la lecture n'est pas encore acquise)
//  - grosses cibles tactiles, feedback immédiat, encouragement prénommé
// ============================================================

// ---------- Contenu : couleurs & formes ----------

data class ColorItem(val fr: String, val ar: String, val value: Color)

object KidColors {
    val all = listOf(
        ColorItem("Rouge", "أَحْمَر", Color(0xFFFF5252)),
        ColorItem("Bleu", "أَزْرَق", Color(0xFF1E90FF)),
        ColorItem("Jaune", "أَصْفَر", Color(0xFFFFC933)),
        ColorItem("Vert", "أَخْضَر", Color(0xFF2ED573)),
        ColorItem("Violet", "بَنَفْسَجِيّ", Color(0xFF6C4BFF)),
        ColorItem("Orange", "بُرْتُقَالِيّ", Color(0xFFFF9F43))
    )
}

enum class ShapeKind { CIRCLE, SQUARE, TRIANGLE, STAR }

data class ShapeItem(val fr: String, val ar: String, val kind: ShapeKind)

object KidShapes {
    val all = listOf(
        ShapeItem("le cercle", "الدَّائِرَة", ShapeKind.CIRCLE),
        ShapeItem("le carré", "المُرَبَّع", ShapeKind.SQUARE),
        ShapeItem("le triangle", "المُثَلَّث", ShapeKind.TRIANGLE),
        ShapeItem("l'étoile", "النَّجْمَة", ShapeKind.STAR)
    )
}

/** Dessine une forme géométrique pleine, bien visible pour un petit enfant. */
@Composable
fun ShapeIcon(kind: ShapeKind, color: Color, size: Int = 90) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val w = this.size.width
        val h = this.size.height
        when (kind) {
            ShapeKind.CIRCLE -> drawCircle(color, radius = w / 2f, center = center)
            ShapeKind.SQUARE -> drawRect(
                color,
                topLeft = Offset(w * 0.08f, h * 0.08f),
                size = Size(w * 0.84f, h * 0.84f)
            )
            ShapeKind.TRIANGLE -> {
                val p = Path().apply {
                    moveTo(w / 2f, h * 0.06f)
                    lineTo(w * 0.94f, h * 0.94f)
                    lineTo(w * 0.06f, h * 0.94f)
                    close()
                }
                drawPath(p, color)
            }
            ShapeKind.STAR -> drawPath(starPath(w, h), color)
        }
    }
}

fun starPath(w: Float, h: Float): Path {
    val cx = w / 2f
    val cy = h / 2f
    val rOut = w * 0.48f
    val rIn = w * 0.2f
    val p = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) rOut else rIn
        val a = Math.toRadians((-90 + i * 36).toDouble())
        val x = (cx + r * Math.cos(a)).toFloat()
        val y = (cy + r * Math.sin(a)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    return p
}

// ---------- Jeu : les couleurs ----------

@Composable
fun ColorsGameScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val target = remember(round) { KidColors.all.random() }
    val choices = remember(round) {
        (KidColors.all.filter { it.fr != target.fr }.shuffled().take(2) + target).shuffled()
    }
    var solved by remember(round) { mutableStateOf(false) }
    var feedback by remember(round) { mutableStateOf("") }

    val question = t(lang, "Touche la couleur ${target.fr} !", "المَسِ اللَّوْنَ ${target.ar}!")

    LaunchedEffect(round) {
        delay(300)
        sound.speak(question, lang)
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(1800)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Les couleurs", "الألوان"), stars = stars, onBack = onBack)
            Spacer(Modifier.height(8.dp))
            BouncyBox(
                modifier = Modifier.size(width = 200.dp, height = 84.dp),
                color = Orange,
                onClick = { sound.speak(question, lang) }
            ) { Text("🔊 🎨", fontSize = 36.sp) }
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
                choices.forEach { c ->
                    BouncyBox(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        color = c.value,
                        onClick = {
                            if (solved) return@BouncyBox
                            if (c.fr == target.fr) {
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
                        Text(
                            t(lang, c.fr, c.ar),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------- Jeu : les formes ----------

@Composable
fun ShapesScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val target = remember(round) { KidShapes.all.random() }
    val choices = remember(round) {
        (KidShapes.all.filter { it.kind != target.kind }.shuffled().take(2) + target).shuffled()
    }
    var solved by remember(round) { mutableStateOf(false) }
    var feedback by remember(round) { mutableStateOf("") }

    val question = t(lang, "Touche ${target.fr} !", "المَسِ ${target.ar}!")

    LaunchedEffect(round) {
        delay(300)
        sound.speak(question, lang)
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(1800)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Les formes", "الأشكال"), stars = stars, onBack = onBack)
            Spacer(Modifier.height(8.dp))
            BouncyBox(
                modifier = Modifier.size(width = 200.dp, height = 84.dp),
                color = Orange,
                onClick = { sound.speak(question, lang) }
            ) { Text("🔊 🔷", fontSize = 36.sp) }
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
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                choices.forEach { s ->
                    BouncyBox(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        color = if (solved && s.kind == target.kind) Green else Color.White,
                        shape = RoundedCornerShape(28.dp),
                        onClick = {
                            if (solved) return@BouncyBox
                            if (s.kind == target.kind) {
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
                        ShapeIcon(s.kind, if (solved && s.kind == target.kind) Color.White else Purple, 90)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------- Jeu : memory (paires) ----------

@Composable
fun MemoryScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val pairs = remember(round) { Content.countEmojis.shuffled().take(3) }
    val cards = remember(round) { (pairs + pairs).shuffled() }
    var faceUp by remember(round) { mutableStateOf(setOf<Int>()) }
    var matched by remember(round) { mutableStateOf(setOf<String>()) }
    var firstPick by remember(round) { mutableStateOf<Int?>(null) }
    var lock by remember(round) { mutableStateOf(false) }
    var feedback by remember(round) { mutableStateOf(t(lang, "Trouve les paires !", "اِبْحَثْ عَنِ الأَزْوَاج!")) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(round) {
        delay(300)
        sound.speak(feedback, lang)
    }

    LaunchedEffect(matched, round) {
        if (pairs.isNotEmpty() && matched.size == pairs.size) {
            val p = praiseNamed(lang, childName)
            feedback = "🏆 $p"
            sound.playSuccess()
            sound.speak(p, lang)
            onStar()
            delay(2500)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Memory", "الذاكرة"), stars = stars, onBack = onBack)
            Text(
                feedback,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (matched.size == pairs.size && pairs.isNotEmpty()) Green else Purple,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (rowStart in cards.indices step 3) {
                    val rowIdx = rowStart until minOf(rowStart + 3, cards.size)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in rowIdx) {
                            val emoji = cards[i]
                            val up = i in faceUp || emoji in matched
                            BouncyBox(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                color = when {
                                    emoji in matched -> Green
                                    up -> Color.White
                                    else -> tileColors[i % tileColors.size]
                                },
                                onClick = {
                                    if (lock || i in faceUp) return@BouncyBox
                                    sound.playClick()
                                    faceUp = faceUp + i
                                    val f = firstPick
                                    if (f == null) {
                                        firstPick = i
                                    } else if (cards[f] == emoji) {
                                        matched = matched + emoji
                                        firstPick = null
                                        feedback = praiseNamed(lang, childName)
                                        sound.playSuccess()
                                        onStar()
                                    } else {
                                        lock = true
                                        feedback = retry(lang)
                                        sound.playError()
                                        scope.launch {
                                            delay(800)
                                            faceUp = faceUp - f - i
                                            firstPick = null
                                            lock = false
                                        }
                                    }
                                }
                            ) {
                                Text(if (up) emoji else "❓", fontSize = 48.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Jeu : la première lettre ----------

@Composable
fun FirstLetterScreen(
    lang: Lang,
    sound: SoundManager,
    stars: Int,
    childName: String,
    onStar: () -> Unit,
    onBack: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    val pool = Content.letters(lang)
    val target = remember(round) { pool.random() }
    val choices = remember(round) {
        (pool.filter { it.symbol != target.symbol }.shuffled().take(2) + target).shuffled()
    }
    var solved by remember(round) { mutableStateOf(false) }
    var feedback by remember(round) { mutableStateOf("") }

    LaunchedEffect(round) {
        delay(300)
        sound.speak(target.word, target.lang)
        delay(900)
        sound.speak(
            t(lang, "Ça commence par quelle lettre ?", "بِأَيِّ حَرْفٍ يَبْدَأ؟"),
            target.lang
        )
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(1800)
            round += 1
        }
    }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(
                title = t(lang, "Première lettre", "أول حرف"),
                stars = stars,
                onBack = onBack
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(32.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(target.emoji, fontSize = 110.sp)
                    Text(
                        target.word,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Purple
                    )
                }
            }
            Text(
                feedback,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (solved) Green else Pink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(44.dp)
                    .padding(top = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                choices.forEach { c ->
                    BouncyBox(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        color = if (solved && c.symbol == target.symbol) Green
                        else tileColors[(c.symbol.hashCode() and 0xFF) % tileColors.size],
                        onClick = {
                            if (solved) return@BouncyBox
                            if (c.symbol == target.symbol) {
                                solved = true
                                val p = praiseNamed(lang, childName)
                                feedback = "🎉 $p"
                                sound.playSuccess()
                                sound.speak(p, target.lang)
                                onStar()
                            } else {
                                feedback = retry(lang)
                                sound.playError()
                                sound.vibrate(100)
                            }
                        }
                    ) {
                        Text(
                            c.symbol,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------- Zone parents (protégée par un code) ----------

@Composable
fun ParentScreen(
    lang: Lang,
    sound: SoundManager,
    profile: ChildProfile,
    balance: Int,
    stickersOwned: Int,
    streak: Int,
    onSaveProfile: (ChildProfile) -> Unit,
    onResetProgress: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onBack: () -> Unit
) {
    var unlocked by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    val gateA = remember { (3..9).random() }
    val gateB = remember { (3..9).random() }
    var confirmReset by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(profile.name) }
    var avatar by remember { mutableStateOf(profile.avatar) }
    var age by remember { mutableIntStateOf(profile.age) }

    GameBackground {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(title = t(lang, "Zone parents", "منطقة الآباء"), stars = null, onBack = onBack)

            if (!unlocked) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "🔒",
                    fontSize = 56.sp
                )
                Text(
                    t(lang, "Question pour les grands : $gateA + $gateB = ?", "سؤال للكبار: $gateA + $gateB = ؟", "Grown-ups only: $gateA + $gateB = ?"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Purple
                )
                Text(
                    "•".repeat(pin.length).ifEmpty { " " },
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Pink,
                    modifier = Modifier.height(52.dp)
                )
                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "✓")
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    keys.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { k ->
                                BouncyBox(
                                    modifier = Modifier.size(84.dp),
                                    color = when (k) {
                                        "✓" -> Green
                                        "C" -> Pink
                                        else -> Purple
                                    },
                                    shape = CircleShape,
                                    onClick = {
                                        sound.playClick()
                                        when (k) {
                                            "C" -> pin = ""
                                            "✓" -> if (pin == (gateA + gateB).toString()) {
                                                unlocked = true
                                                sound.playSuccess()
                                            } else {
                                                pin = ""
                                                sound.playError()
                                            }
                                            else -> if (pin.length < 2) pin += k
                                        }
                                    }
                                ) {
                                    Text(
                                        k,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    t(lang, "Code par défaut : 1234 (modifiable ci-dessous)", "الرمز الافتراضي: 1234"),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "⭐ $balance    🏅 $stickersOwned/${Stickers.all.size}    🔥 $streak    v$APP_VERSION",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(14) },
                    label = { Text(t(lang, "Prénom de l'enfant", "اسم الطفل")) },
                    singleLine = true,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("🦁", "🦄", "🚀", "🐼", "🤖", "🦋").forEach { a ->
                        BouncyBox(
                            modifier = Modifier.size(56.dp),
                            color = if (a == avatar) Green else Color.White,
                            shape = CircleShape,
                            onClick = {
                                sound.playClick()
                                avatar = a
                            }
                        ) { Text(a, fontSize = 28.sp) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        t(lang, "Âge :", "العمر :"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    listOf(3, 4, 5, 6).forEach { a ->
                        BouncyBox(
                            modifier = Modifier.size(56.dp),
                            color = if (a == age) Green else Purple,
                            shape = CircleShape,
                            onClick = {
                                sound.playClick()
                                age = a
                            }
                        ) {
                            Text(
                                "$a",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                BouncyBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(60.dp),
                    color = Green,
                    onClick = {
                        sound.playSuccess()
                        onSaveProfile(ChildProfile(name.trim().ifEmpty { "Ilyas" }, avatar, age))
                    }
                ) {
                    Text(
                        t(lang, "💾 Enregistrer", "💾 حفظ"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(10.dp))
                BouncyBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(60.dp),
                    color = Blue,
                    onClick = {
                        sound.playClick()
                        onOpenVoiceSettings()
                    }
                ) {
                    Text(
                        t(lang, "🔊 Installer la voix arabe", "🔊 تثبيت الصوت العربي"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(10.dp))
                BouncyBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(60.dp),
                    color = if (confirmReset) Pink else Color.LightGray,
                    onClick = {
                        if (!confirmReset) {
                            confirmReset = true
                            sound.playError()
                        } else {
                            sound.playClick()
                            onResetProgress()
                            confirmReset = false
                        }
                    }
                ) {
                    Text(
                        if (confirmReset) t(lang, "⚠️ Confirmer la remise à zéro ?", "⚠️ تأكيد المسح؟")
                        else t(lang, "🗑️ Remettre à zéro la progression", "🗑️ مسح التقدم"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
