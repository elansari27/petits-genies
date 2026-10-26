package com.kidslearn.app

/** Un objet du monde réel : image + nom dans chaque langue (arabe voyellé pour une lecture juste). */
class Thing(val emoji: String, val fr: String, val ar: String, val en: String) {
    /** Nom à lire / afficher dans la langue demandée. En mode MIX : français puis arabe. */
    fun name(lang: Lang): String = when (lang) {
        Lang.FR -> fr
        Lang.AR -> ar
        Lang.EN -> en
        Lang.MIX -> "$fr. $ar"
    }

    /** Nom à afficher à l'écran (MIX sur deux lignes). */
    fun label(lang: Lang): String = if (lang == Lang.MIX) "$fr\n$ar" else name(lang)
}

/** Une famille d'objets (animaux, fruits...). */
class Family(val fr: String, val ar: String, val en: String, val icon: String, val things: List<Thing>) {
    fun name(lang: Lang) = t(lang, fr, ar, en)
}

object Things {
    val animals = listOf(
        Thing("🦁", "le lion", "الأَسَد", "the lion"),
        Thing("🐘", "l'éléphant", "الفِيل", "the elephant"),
        Thing("🐒", "le singe", "القِرْد", "the monkey"),
        Thing("🦒", "la girafe", "الزَّرَافَة", "the giraffe"),
        Thing("🐯", "le tigre", "النَّمِر", "the tiger"),
        Thing("🐻", "l'ours", "الدُّبّ", "the bear"),
        Thing("🐼", "le panda", "البَانْدَا", "the panda"),
        Thing("🐰", "le lapin", "الأَرْنَب", "the rabbit"),
        Thing("🐶", "le chien", "الكَلْب", "the dog"),
        Thing("🐱", "le chat", "القِطَّة", "the cat"),
        Thing("🐴", "le cheval", "الحِصَان", "the horse"),
        Thing("🐮", "la vache", "البَقَرَة", "the cow"),
        Thing("🐑", "le mouton", "الخَرُوف", "the sheep"),
        Thing("🐔", "la poule", "الدَّجَاجَة", "the chicken"),
        Thing("🦆", "le canard", "البَطَّة", "the duck"),
        Thing("🐟", "le poisson", "السَّمَكَة", "the fish"),
        Thing("🐢", "la tortue", "السُّلَحْفَاة", "the turtle"),
        Thing("🐸", "la grenouille", "الضِّفْدَع", "the frog"),
        Thing("🐪", "le chameau", "الجَمَل", "the camel"),
        Thing("🦓", "le zèbre", "الحِمَار الوَحْشِيّ", "the zebra")
    )
    val vehicles = listOf(
        Thing("🚗", "la voiture", "السَّيَّارَة", "the car"),
        Thing("🚌", "le bus", "الحَافِلَة", "the bus"),
        Thing("🚲", "le vélo", "الدَّرَّاجَة", "the bike"),
        Thing("✈️", "l'avion", "الطَّائِرَة", "the plane"),
        Thing("🚂", "le train", "القِطَار", "the train"),
        Thing("🚀", "la fusée", "الصَّارُوخ", "the rocket"),
        Thing("🚁", "l'hélicoptère", "المَرْوَحِيَّة", "the helicopter"),
        Thing("⛵", "le bateau", "القَارِب", "the boat")
    )
    val fruits = listOf(
        Thing("🍎", "la pomme", "التُّفَّاحَة", "the apple"),
        Thing("🍌", "la banane", "المَوْزَة", "the banana"),
        Thing("🍇", "le raisin", "العِنَب", "the grapes"),
        Thing("🍓", "la fraise", "الفَرَاوْلَة", "the strawberry"),
        Thing("🍊", "l'orange", "البُرْتُقَالَة", "the orange"),
        Thing("🍉", "la pastèque", "البِطِّيخ", "the watermelon"),
        Thing("🍐", "la poire", "الإِجَّاصَة", "the pear"),
        Thing("🍒", "les cerises", "الكَرَز", "the cherries")
    )
    val food = listOf(
        Thing("🍕", "la pizza", "البِيتْزَا", "the pizza"),
        Thing("🍔", "le burger", "الهَمْبُورْغَر", "the burger"),
        Thing("🍟", "les frites", "البَطَاطَا المَقْلِيَّة", "the fries"),
        Thing("🍦", "la glace", "البُوظَة", "the ice cream"),
        Thing("🍪", "le biscuit", "البِسْكُوِيت", "the cookie"),
        Thing("🥕", "la carotte", "الجَزَرَة", "the carrot"),
        Thing("🍞", "le pain", "الخُبْز", "the bread"),
        Thing("🧀", "le fromage", "الجُبْن", "the cheese")
    )

    val families = listOf(
        Family("les animaux", "الحَيَوَانَات", "animals", "🐾", animals),
        Family("les fruits", "الفَوَاكِه", "fruits", "🍎", fruits),
        Family("les transports", "المُوَاصَلَات", "vehicles", "🚗", vehicles),
        Family("la nourriture", "الطَّعَام", "food", "🍕", food)
    )

    val all: List<Thing> = families.flatMap { it.things }

    fun familyOf(thing: Thing): Family = families.first { f -> f.things.any { it === thing } }
}

/** Animaux sous forme d'éléments « apprendre » : l'image en grand + le nom dans la langue. */
fun animalItems(lang: Lang): List<Item> {
    fun mk(a: Thing, l: Lang): Item {
        val nm = a.name(l)
        return Item(a.emoji, nm, nm.removePrefix("le ").removePrefix("la ").removePrefix("l'")
            .removePrefix("the "), a.emoji, l)
    }
    return when (lang) {
        Lang.MIX -> Things.animals.flatMap { listOf(mk(it, Lang.FR), mk(it, Lang.AR)) }
        else -> Things.animals.map { mk(it, lang) }
    }
}
