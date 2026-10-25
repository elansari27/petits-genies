package com.kidslearn.app

/**
 * Traduction d'un texte d'interface.
 * - FR / AR / EN : le texte de la langue choisie (EN retombe sur le français si absent).
 * - MIX : français puis arabe, sur deux lignes.
 */
fun t(lang: Lang, fr: String, ar: String, en: String? = null): String = when (lang) {
    Lang.FR -> fr
    Lang.AR -> ar
    Lang.EN -> en ?: Tr.en[fr] ?: fr
    Lang.MIX -> "$fr\n$ar"
}

/** Dictionnaire français -> anglais pour les textes déjà écrits sans traduction anglaise explicite. */
object Tr {
    val en: Map<String, String> = mapOf(
        "Jouer" to "Play",
        "Retour" to "Back",
        "Parents" to "Parents",
        "Lettres" to "Letters",
        "Chiffres" to "Numbers",
        "Compter" to "Count",
        "Couleurs" to "Colors",
        "Formes" to "Shapes",
        "Tailles" to "Sizes",
        "Tri" to "Sorting",
        "Puzzles" to "Puzzles",
        "Logique" to "Logic",
        "Trouver l'intrus" to "Odd one out",
        "Cache-cache" to "Hide and seek",
        "Mémoire" to "Memory",
        "Histoires" to "Stories",
        "Trésor" to "Treasure",
        "Bulles" to "Bubbles",
        "Autocollants" to "Stickers",
        "Tracer" to "Trace",
        "Relier" to "Connect",
        "Apprendre" to "Learn",
        "Test" to "Test",
        "Suivant" to "Next",
        "Bravo !" to "Great job!",
        "Essaie encore !" to "Try again!",
        "Choisis ta langue" to "Choose your language",
        "Français" to "French",
        "Arabe" to "Arabic",
        "Anglais" to "English",
        "Mixte" to "Mixed",
        "Les lettres" to "Letters",
        "Les chiffres" to "Numbers",
        "Les couleurs" to "Colors",
        "Les formes" to "Shapes",
        "Première lettre" to "First letter",
        "Petit ou grand" to "Small or big",
        "Le panier" to "The basket",
        "Écoute les lettres" to "Listen: letters",
        "Écoute les chiffres" to "Listen: numbers",
        "Memory" to "Memory",
        "Associer" to "Match",
        "Bulles magiques" to "Magic bubbles",
        "Chasse au trésor" to "Treasure hunt",
        "Mes autocollants" to "My stickers",
        "Zone parents" to "Parents area",
        "On joue !" to "Let's play!",
        "Trace avec ton doigt" to "Trace with your finger",
        "Relie !" to "Connect!",
        "Test : lettres" to "Test: letters",
        "Test : chiffres" to "Test: numbers",
        "Cache-cache lettres" to "Hide & seek: letters",
        "Cache-cache chiffres" to "Hide & seek: numbers",
        "Puzzles : ombres" to "Puzzles: shadows"
    )
}
