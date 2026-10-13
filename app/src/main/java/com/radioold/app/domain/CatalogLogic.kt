package com.radioold.app.domain

import com.radioold.app.data.model.Mood
import com.radioold.app.data.model.Track
import kotlin.random.Random

/**
 * Pure catalog matching + queue sequencing logic. Kept free of Android/media
 * types so it is trivially unit-testable.
 */
object CatalogLogic {

    /** Does [track] belong to the given country / decade / mood filters (null = any)? */
    fun matches(
        track: Track,
        country: String?,
        decade: Int?,
        mood: Mood?,
    ): Boolean {
        if (country != null && track.country != country) return false
        if (decade != null && track.decade != decade) return false
        if (mood != null && track.mood != mood) return false
        return true
    }

    fun filter(
        tracks: List<Track>,
        country: String? = null,
        decade: Int? = null,
        mood: Mood? = null,
    ): List<Track> = tracks.filter { matches(it, country, decade, mood) }

    /**
     * A fresh random running order: a permutation of [tracks], reshuffled on every
     * call so the same country/decade selection never yields the same order twice.
     * [avoidFirstId] keeps the currently playing track from starting the queue
     * again, unless it is the only track.
     */
    fun shuffled(
        tracks: List<Track>,
        random: Random = Random.Default,
        avoidFirstId: String? = null,
    ): List<Track> {
        if (tracks.size <= 1) return tracks
        val out = tracks.shuffled(random).toMutableList()
        if (out[0].id == avoidFirstId) {
            val swapWith = out.indexOfLast { it.id != avoidFirstId }
            if (swapWith > 0) {
                val first = out[0]
                out[0] = out[swapWith]
                out[swapWith] = first
            }
        }
        return out
    }
}
