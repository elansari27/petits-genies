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
