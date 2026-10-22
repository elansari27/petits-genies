package com.kidslearn.app

import java.util.Locale

enum class Lang(val locale: Locale) {
    FR(Locale.FRENCH),
    AR(Locale("ar")),
    MIX(Locale.FRENCH)
}

/** Un élément à apprendre : un symbole, ce qu'on dit à voix haute, et un mot-exemple. */
data class Item(
    val symbol: String,
    val spoken: String,
    val word: String,
    val emoji: String,
    val lang: Lang
)

object Content {

    private val frLetters = listOf(
        Item("A", "A comme avion", "Avion", "✈️", Lang.FR),
        Item("B", "B comme ballon", "Ballon", "🎈", Lang.FR),
        Item("C", "C comme chat", "Chat", "🐱", Lang.FR),
        Item("D", "D comme dauphin", "Dauphin", "🐬", Lang.FR),
        Item("E", "E comme éléphant", "Éléphant", "🐘", Lang.FR),
        Item("F", "F comme fraise", "Fraise", "🍓", Lang.FR),
        Item("G", "G comme gâteau", "Gâteau", "🍰", Lang.FR),
        Item("H", "H comme hibou", "Hibou", "🦉", Lang.FR),
        Item("I", "I comme île", "Île", "🏝️", Lang.FR),
        Item("J", "J comme jus", "Jus", "🧃", Lang.FR),
        Item("K", "K comme kiwi", "Kiwi", "🥝", Lang.FR),
        Item("L", "L comme lion", "Lion", "🦁", Lang.FR),
        Item("M", "M comme maison", "Maison", "🏠", Lang.FR),
        Item("N", "N comme nuage", "Nuage", "☁️", Lang.FR),
        Item("O", "O comme orange", "Orange", "🍊", Lang.FR),
        Item("P", "P comme poisson", "Poisson", "🐟", Lang.FR),
        Item("Q", "Q comme quatre", "Quatre", "4️⃣", Lang.FR),
        Item("R", "R comme robot", "Robot", "🤖", Lang.FR),
        Item("S", "S comme soleil", "Soleil", "☀️", Lang.FR),
        Item("T", "T comme tortue", "Tortue", "🐢", Lang.FR),
        Item("U", "U comme ours", "Ours", "🐻", Lang.FR),
        Item("V", "V comme voiture", "Voiture", "🚗", Lang.FR),
        Item("W", "W comme wagon", "Wagon", "🚃", Lang.FR),
        Item("X", "X comme xylophone", "Xylophone", "🎼", Lang.FR),
        Item("Y", "Y comme yo-yo", "Yo-yo", "🪀", Lang.FR),
        Item("Z", "Z comme zèbre", "Zèbre", "🦓", Lang.FR)
    )

    private val arLetters = listOf(
        Item("أ", "أَلِف. أرنب", "أرنب", "🐰", Lang.AR),
        Item("ب", "باء. بطة", "بطة", "🦆", Lang.AR),
        Item("ت", "تاء. تفاحة", "تفاحة", "🍎", Lang.AR),
        Item("ث", "ثاء. ثعلب", "ثعلب", "🦊", Lang.AR),
        Item("ج", "جيم. جمل", "جمل", "🐪", Lang.AR),
        Item("ح", "حاء. حصان", "حصان", "🐴", Lang.AR),
        Item("خ", "خاء. خروف", "خروف", "🐑", Lang.AR),
        Item("د", "دال. دجاجة", "دجاجة", "🐔", Lang.AR),
        Item("ذ", "ذال. ذرة", "ذرة", "🌽", Lang.AR),
        Item("ر", "راء. رمان", "رمان", "🍎", Lang.AR),
        Item("ز", "زاي. زرافة", "زرافة", "🦒", Lang.AR),
        Item("س", "سين. سمكة", "سمكة", "🐟", Lang.AR),
        Item("ش", "شين. شمس", "شمس", "☀️", Lang.AR),
        Item("ص", "صاد. صاروخ", "صاروخ", "🚀", Lang.AR),
        Item("ض", "ضاد. ضفدع", "ضفدع", "🐸", Lang.AR),
        Item("ط", "طاء. طائرة", "طائرة", "✈️", Lang.AR),
        Item("ظ", "ظاء. ظرف", "ظرف", "✉️", Lang.AR),
        Item("ع", "عين. عصفور", "عصفور", "🐦", Lang.AR),
        Item("غ", "غين. غيمة", "غيمة", "☁️", Lang.AR),
        Item("ف", "فاء. فيل", "فيل", "🐘", Lang.AR),
        Item("ق", "قاف. قمر", "قمر", "🌙", Lang.AR),
        Item("ك", "كاف. كتاب", "كتاب", "📖", Lang.AR),
        Item("ل", "لام. ليمون", "ليمون", "🍋", Lang.AR),
        Item("م", "ميم. موز", "موز", "🍌", Lang.AR),
        Item("ن", "نون. نحلة", "نحلة", "🐝", Lang.AR),
        Item("هـ", "هاء. هرم", "هرم", "🔺", Lang.AR),
        Item("و", "واو. وردة", "وردة", "🌹", Lang.AR),
        Item("ي", "ياء. يد", "يد", "✋", Lang.AR)
    )

    private val frNumbers = listOf(
        "zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix"
    )

    private val arNumbers = listOf(
        "صفر", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة", "عشرة"
    )

    private val arDigits = listOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "١٠")

    val frDigits: List<Item> = (0..10).map {
        Item(it.toString(), frNumbers[it], frNumbers[it], "🔢", Lang.FR)
    }

    val arDigitItems: List<Item> = (0..10).map {
        Item(arDigits[it], arNumbers[it], arNumbers[it], "🔢", Lang.AR)
    }

    fun letters(lang: Lang): List<Item> = when (lang) {
        Lang.FR -> frLetters
        Lang.AR -> arLetters
        Lang.MIX -> frLetters.zip(arLetters).flatMap { listOf(it.first, it.second) } +
                arLetters.drop(frLetters.size)
    }

    fun numbers(lang: Lang): List<Item> = when (lang) {
        Lang.FR -> frDigits
        Lang.AR -> arDigitItems
        Lang.MIX -> frDigits.zip(arDigitItems).flatMap { listOf(it.first, it.second) }
    }

    /** Emojis utilisés pour le jeu de comptage. */
    val countEmojis = listOf("🍎", "⭐", "🐟", "🎈", "🐥", "🚗", "🍓", "🦋")

    /** Mots d'encouragement. */
    val praiseFr = listOf("Bravo !", "Super !", "Génial !", "Tu es un champion !", "Bien joué !")
    val praiseAr = listOf("أحسنت!", "رائع!", "ممتاز!", "برافو!", "عمل جميل!")
    val retryFr = listOf("Essaie encore !", "Presque ! Réessaie !")
    val retryAr = listOf("حاول مرة أخرى!", "قريب! جرّب ثانية!")
}

// ---------- Aides ----------

/** Ce qu'on dit quand l'enfant touche une lettre ou un chiffre (court, sans le mot-exemple). */
fun Item.shortSpoken(): String = when {
    emoji == "🔢" -> spoken
    lang == Lang.AR -> spoken.substringBefore('.')
    else -> symbol
}

fun isArabicLetter(symbol: String): Boolean =
    symbol.isNotEmpty() && symbol[0] in 'ء'..'ي'

/** Formes d'une lettre arabe : isolée, finale, médiane, initiale (le ZWJ force la forme de liaison). */
fun arabicForms(symbol: String): List<String> {
    val base = symbol.replace("ـ", "")
    val zwj = "‍"
    return listOf(base, zwj + base, zwj + base + zwj, base + zwj)
}

fun praiseFor(lang: Lang): String =
    if (lang == Lang.AR) Content.praiseAr.random() else Content.praiseFr.random()

// ---------- Autocollants ----------

data class Sticker(val id: String, val emoji: String, val price: Int)

object Stickers {
    val all = listOf(
        Sticker("dog", "🐶", 2), Sticker("cat", "🐱", 2), Sticker("panda", "🐼", 3),
        Sticker("lion", "🦁", 3), Sticker("unicorn", "🦄", 4), Sticker("butterfly", "🦋", 4),
        Sticker("elephant", "🐘", 5), Sticker("dolphin", "🐬", 5), Sticker("rocket", "🚀", 6),
        Sticker("rainbow", "🌈", 6), Sticker("robot", "🤖", 8), Sticker("dino", "🦖", 8),
        Sticker("octopus", "🐙", 10), Sticker("crown", "👑", 10), Sticker("trophy", "🏆", 12),
        Sticker("cake", "🎂", 15)
    )
}

// ---------- Histoires interactives ----------

data class StoryStep(val text: String, val hero: String, val options: List<String>, val answer: String)

data class Story(
    val title: String,
    val emoji: String,
    val lang: Lang,
    val ending: String,
    val steps: List<StoryStep>
)

object Stories {
    val fr = listOf(
        Story(
            "Le chat et le chiffre 3", "🐱", Lang.FR,
            "Bravo ! L'histoire est terminée. Le chat est très content !",
            listOf(
                StoryStep("Le petit chat a perdu son chiffre porte-bonheur. Aide-le à trouver le chiffre trois !", "🐱", listOf("1", "3", "5", "7"), "3"),
                StoryStep("Super ! Maintenant, le chat a faim. Trouve la lettre C, comme chat !", "😺", listOf("M", "C", "P", "T"), "C"),
                StoryStep("Le chat veut cinq poissons. Trouve le chiffre cinq !", "🐟", listOf("2", "8", "5", "9"), "5")
            )
        ),
        Story(
            "La fusée de Léo", "🚀", Lang.FR,
            "Bravo ! La fusée a décollé jusqu'à la lune. Tu es un champion !",
            listOf(
                StoryStep("Léo prépare sa fusée. Trouve la lettre F, comme fusée !", "🚀", listOf("R", "S", "F", "T"), "F"),
                StoryStep("Super ! La fusée a besoin de quatre étoiles. Trouve le chiffre quatre !", "⭐", listOf("4", "3", "7", "1"), "4"),
                StoryStep("Dernière étape ! Trouve la lettre L, comme lune !", "🌙", listOf("J", "I", "N", "L"), "L")
            )
        )
    )

    val ar = listOf(
        Story(
            "الأرنب والحرف أ", "🐰", Lang.AR,
            "برافو! انتهت القصة. الأرنب سعيد جدا!",
            listOf(
                StoryStep("ضاع من الأرنب الصغير حرفه المفضل. ساعده ليجد حرف الألف!", "🐰", listOf("ب", "أ", "ت", "ث"), "أ"),
                StoryStep("أحسنت! الأرنب جائع. جد معه الرقم ثلاثة!", "🥕", listOf("١", "٥", "٣", "٧"), "٣"),
                StoryStep("الآن جد حرف الجيم، مثل جزرة!", "🥕", listOf("ج", "ح", "خ", "د"), "ج")
            )
        ),
        Story(
            "السمكة الصغيرة", "🐟", Lang.AR,
            "برافو! السمكة سعيدة. أنت بطل!",
            listOf(
                StoryStep("السمكة الصغيرة تسبح في البحر. ساعدها لتجد الرقم اثنين!", "🐟", listOf("٢", "٤", "٦", "٩"), "٢"),
                StoryStep("رائع! الآن جد حرف السين، مثل سمكة!", "🐠", listOf("ش", "ص", "س", "ض"), "س"),
                StoryStep("آخر خطوة! جد الرقم خمسة!", "🐚", listOf("٧", "٥", "٨", "١"), "٥")
            )
        )
    )
}
