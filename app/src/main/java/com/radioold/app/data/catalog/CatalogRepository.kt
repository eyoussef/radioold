package com.radioold.app.data.catalog

import android.content.Context
import com.radioold.app.data.geo.WorldMap
import com.radioold.app.data.model.Mood
import com.radioold.app.data.model.Track
import com.radioold.app.data.remote.RadioooooApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Music data source: radiooooo.com's curated country x decade x mood stations —
 * real local music of the selected year, with direct MP3 audio and full
 * metadata. One API call = one random track of a station, so playback is fed
 * just in time (the signed stream links are valid for ~15 minutes). Tracks
 * carry their metadata at fetch time; the world map geometry is bundled.
 */
class CatalogRepository(private val context: Context) {

    private var worldCache: WorldMap? = null

    suspend fun loadWorld(): WorldMap = withContext(Dispatchers.IO) {
        CatalogParser.parseWorld(
            context.assets.open("world.json").bufferedReader().use { it.readText() }
        ).also { worldCache = it }
    }

    /** First-open feed: random tracks from the whole time machine. */
    suspend fun baseTracks(count: Int = STATION_QUEUE): List<Track> =
        stationQueue(null, null, null, null, count)

    /**
     * A station's queue: [count] random tracks of [countryIso]/[decade]/[mood]
     * (null = anywhere/anything). Empty when the area/decade has no music yet —
     * exactly what radiooooo itself reports for unpopulated stations.
     */
    suspend fun stationQueue(
        countryIso: String?,
        countryName: String?,
        decade: Int?,
        mood: Mood?,
        count: Int = STATION_QUEUE,
    ): List<Track> {
        val moods = if (mood != null) listOf(mood.name) else Mood.entries.map { it.name }
        return (1..count).mapNotNull {
            RadioooooApi.next(countryIso, decade, moods)?.toTrack(countryIso, countryName)
        }
    }

    private fun RadioooooApi.Raw.toTrack(stationIso: String?, stationName: String?): Track = Track(
        id = id,
        title = title,
        artist = artist,
        year = year,
        decade = decade,
        country = stationIso ?: country,
        countryName = stationName ?: nameOf(country),
        mood = runCatching { Mood.valueOf(mood) }.getOrDefault(Mood.FAST),
        stream = stream,
        cover = cover ?: "",
        album = album,
        label = label,
        songwriter = songwriter,
        durationSec = durationSec,
        likes = likes,
        source = "radiooooo",
        license = "radiooooo-curated",
    )

    private fun nameOf(iso: String): String =
        worldCache?.countries?.firstOrNull { it.id == iso }?.name ?: iso

    companion object {
        /** Small on purpose: the signed stream links expire in ~15 minutes. */
        const val STATION_QUEUE = 3
    }
}
