package com.radioold.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioooooApiTest {

    private val trackPayload = """
        {"_id":"67f675213e30adea537ecb75","title":"Douz Douz","artist":"Fatima",
         "album":"Qui A Aime Aimera / Douz, Douz","label":"Atoll Music",
         "songwriter":"Ali El Hadani, Hassan Megri","length":224,"likes":24,
         "country":"MAR","mood":"SLOW",
         "year":"1984","decade":1980,
         "image":{"path":"cover/MAR/1980/","filename":"x.jpg","color":"#886858"},
         "links":{"mpeg":"https://radiooooo-track.b-cdn.net/MAR/1980/x.mp3?token=t&expires=1"}}
    """.trimIndent()

    @Test
    fun `parseTrack reads metadata, cover and stream`() {
        val t = RadioooooApi.parseTrack(trackPayload)!!
        assertEquals("67f675213e30adea537ecb75", t.id)
        assertEquals("Douz Douz", t.title)
        assertEquals("Fatima", t.artist)
        assertEquals(1984, t.year)
        assertEquals(1980, t.decade)
        assertEquals("MAR", t.country)
        assertEquals("SLOW", t.mood)
        assertEquals("Atoll Music", t.label)
        assertEquals("Ali El Hadani, Hassan Megri", t.songwriter)
        assertEquals(224, t.durationSec)
        assertEquals(24, t.likes)
        assertEquals("https://asset.radiooooo.com/cover/MAR/1980/large/x.jpg", t.cover)
        assertTrue(t.stream.startsWith("https://radiooooo-track.b-cdn.net/"))
    }

    @Test
    fun `parseTrack falls back to the decade for era-string years`() {
        val t = RadioooooApi.parseTrack(trackPayload.replace("\"year\":\"1984\"", "\"year\":\"1980s\""))!!
        assertEquals(1980, t.year)
    }

    @Test
    fun `parseTrack rejects empty and error responses`() {
        assertNull(RadioooooApi.parseTrack(""))
        assertNull(RadioooooApi.parseTrack("not json"))
        assertNull(RadioooooApi.parseTrack("""{"error":"This Area/Decade is EMPTY for now"}"""))
        assertNull(RadioooooApi.parseTrack("""{"_id":"x","title":"t"}""")) // no links
    }

    @Test
    fun `requestBody builds an explore station query`() {
        val b = RadioooooApi.requestBody("MAR", 1980, listOf("SLOW", "FAST"))
        assertEquals("explore", b.getString("mode"))
        assertEquals("MAR", b.getJSONArray("isocodes").getString(0))
        assertEquals(1980, b.getJSONArray("decades").getInt(0))
        assertEquals(2, b.getJSONArray("moods").length())
    }

    @Test
    fun `requestBody builds a random query for the worldwide station`() {
        val b = RadioooooApi.requestBody(null, null, listOf("WEIRD"))
        assertEquals("random", b.getString("mode"))
        assertEquals("WEIRD", b.getJSONArray("moods").getString(0))
    }
}
