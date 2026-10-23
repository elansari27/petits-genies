package com.kidslearn.app

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Profil de l'enfant : prénom, avatar, âge, code parents. */
data class ChildProfile(
    val name: String = "Ilyas",
    val avatar: String = "🦁",
    val age: Int = 5,
    val pin: String = "1234"
)

object ProfileStore {

    private const val PREF = "kids"

    fun load(ctx: Context): ChildProfile {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return ChildProfile(
            name = p.getString("child_name", null) ?: "Ilyas",
            avatar = p.getString("child_avatar", "🦁") ?: "🦁",
            age = p.getInt("child_age", 5),
            pin = p.getString("parent_pin", "1234") ?: "1234"
        )
    }

    fun save(ctx: Context, profile: ChildProfile) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString("child_name", profile.name)
            .putString("child_avatar", profile.avatar)
            .putInt("child_age", profile.age)
            .putString("parent_pin", profile.pin)
            .apply()
    }

    /** Met à jour la série de jours de jeu (1 ouverture par jour compte). Renvoie la série actuelle. */
    fun touchStreak(ctx: Context): Int {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val today = dayStamp()
        val last = p.getInt("last_play_day", 0)
        if (last == today) return p.getInt("streak", 1)
        val streak = if (last == today - 1) p.getInt("streak", 0) + 1 else 1
        p.edit().putInt("last_play_day", today).putInt("streak", streak).apply()
        return streak
    }

    fun resetProgress(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .remove("stars").remove("spent").remove("stickers")
            .remove("last_play_day").remove("streak")
            .apply()
    }

    private fun dayStamp(): Int =
        SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()).toInt()
}

/** Encouragement personnalisé avec le prénom de l'enfant. */
fun praiseNamed(lang: Lang, name: String): String {
    val n = name.trim()
    val fr = if (n.isEmpty()) Content.praiseFr.random()
    else listOf(
        "Bravo $n !", "Super $n !", "Génial $n !",
        "Tu es fort $n !", "Quel champion $n !"
    ).random()
    val ar = if (n.isEmpty()) Content.praiseAr.random()
    else listOf(
        "أحسنت يا $n!", "رائع يا $n!", "ممتاز يا $n!", "أنت بطل يا $n!"
    ).random()
    return when (lang) {
        Lang.FR -> fr
        Lang.AR -> ar
        Lang.MIX -> if (java.util.Random().nextBoolean()) fr else ar
    }
}
