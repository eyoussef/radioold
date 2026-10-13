package com.radioold.app.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * radiooooo.com — the original musical time machine: a curated
 * country x decade x mood catalog with direct MP3 links. One call to /play
 * returns one random track of the station; the stream links are signed and
 * valid for ~15 minutes, so tracks are fetched just in time and queues stay
 * small. This is Radioold's music and metadata source.
 */
object RadioooooApi {

    private const val TAG = "RadiooldApi"
    private const val PLAY = "https://radiooooo.com/play"
    private const val PLAY_RANDOM = "https://radiooooo.com/play/random"
    private const val ASSET = "https://asset.radiooooo.com/"

    /** Cover variants live in size directories: {path}{format}/{filename}. */
    private const val COVER_FORMAT = "large"

    data class Raw(
        val id: String,
        val title: String,
        val artist: String,
        val album: String,
        val label: String,
        val songwriter: String,
        val year: Int,
        val decade: Int,
        val country: String,
        val mood: String,
        val durationSec: Int,
        val likes: Int,
        val cover: String?,
        val stream: String,
    )

    /**
     * One random track of the station ([countryIso]/[decade] = anywhere/any time
     * when null); null when the area/decade is empty.
     */
    suspend fun next(countryIso: String?, decade: Int?, moods: List<String>): Raw? {
        val worldwide = countryIso == null && decade == null
        val body = requestBody(countryIso, decade, moods)
        return parseTrack(post(if (worldwide) PLAY_RANDOM else PLAY, body) ?: "")
    }

    /** Station request body (pure): explore for a place/time, random for anywhere. */
    internal fun requestBody(countryIso: String?, decade: Int?, moods: List<String>): JSONObject =
        if (countryIso == null && decade == null) {
            JSONObject()
                .put("mode", "random")
                .put("moods", JSONArray(moods))
        } else {
            JSONObject()
                .put("mode", "explore")
                .put("isocodes", JSONArray(listOfNotNull(countryIso)))
                .put("decades", JSONArray(listOfNotNull(decade)))
                .put("moods", JSONArray(moods))
        }

    /** Tolerant track mapping (pure): an empty/error response yields null. */
    internal fun parseTrack(text: String): Raw? {
        val t = runCatching { JSONObject(text) }.getOrNull() ?: return null
        val id = t.optString("_id")
        val stream = t.optJSONObject("links")?.optString("mpeg").orEmpty()
        if (id.isEmpty() || stream.isEmpty()) return null
        val decade = t.optInt("decade", 0)
        val yearRaw = t.optString("year")
        val img = t.optJSONObject("image") ?: t.optJSONObject("cover")
        return Raw(
            id = id,
            title = t.optString("title").ifBlank { "Untitled" },
            artist = t.optString("artist").ifBlank { "Unknown" },
            album = t.optString("album"),
            label = t.optString("label"),
            songwriter = t.optString("songwriter"),
            // year can be "1984" or just "1980s"; digits win, decade is the floor.
            year = yearRaw.filter { it.isDigit() }.take(4).toIntOrNull() ?: decade,
            decade = decade,
            country = t.optString("country"),
            mood = t.optString("mood").ifBlank { "FAST" },
            durationSec = t.optInt("length", 0),
            likes = t.optInt("likes", 0),
            cover = img?.let { ASSET + it.optString("path") + COVER_FORMAT + "/" + it.optString("filename") }
                ?.takeIf { it.endsWith(".jpg") || it.endsWith(".png") || it.endsWith(".jpeg") },
            stream = stream,
        )
    }

    private suspend fun post(url: String, body: JSONObject): String? = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 20000
                readTimeout = 20000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "RadiooldApp/1.0 (clean-room)")
            }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) {
                Log.w(TAG, "radiooooo $code for $url")
                conn.disconnect()
                return@withContext null
            }
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            text
        } catch (e: Exception) {
            Log.w(TAG, "radiooooo request failed: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }
}
