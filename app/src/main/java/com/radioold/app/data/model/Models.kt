package com.radioold.app.data.model

/**
 * Core domain types for Radioold — a clean-room "musical time machine".
 * These are pure Kotlin (no Android imports) so they are unit-testable on the JVM.
 */

enum class Mood(val label: String) {
    SLOW("Slow"),
    FAST("Fast"),
    WEIRD("Weird");

    companion object {
        val ALL = entries.toList()
    }
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int,
    val decade: Int,
    val country: String,       // ISO-3166-1 alpha-3, e.g. "MAR"
    val countryName: String,
    val mood: Mood,
    val stream: String? = null,   // direct stream URL
    val cover: String = "",
    val album: String = "",
    val label: String = "",
    val songwriter: String = "",
    val durationSec: Int = 0,
    val likes: Int = 0,
    val source: String = "radiooooo",
    val license: String = "radiooooo-curated",
) {
    val decadeLabel: String
        get() = if (decade >= NOW_DECADE) "Now" else "${decade}s"

    val durationLabel: String
        get() {
            val m = durationSec / 60
            val s = durationSec % 60
            return "%d:%02d".format(m, s)
        }

    companion object {
        const val NOW_DECADE = 2070

        fun decadeOf(year: Int): Int {
            if (year >= 2020) return NOW_DECADE
            return (year / 10) * 10
        }
    }
}

/** One selectable place+time (+optional mood) on the map. */
data class Stop(
    val country: String,
    val countryName: String,
    val decade: Int,
    val mood: Mood? = null,
)

/** Immutable selection currently driving playback. [decade] null = all decades. */
data class Selection(
    val country: String? = null,
    val countryName: String? = null,
    val decade: Int? = null,
    val mood: Mood? = null,
)
