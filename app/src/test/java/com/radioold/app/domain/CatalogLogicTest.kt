package com.radioold.app.domain

import com.radioold.app.data.model.Mood
import com.radioold.app.data.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CatalogLogicTest {

    private fun track(
        id: String,
        country: String = "MAR",
        decade: Int = 1980,
        year: Int = 1984,
        mood: Mood = Mood.SLOW,
    ) = Track(
        id = id, title = id, artist = "Artist", year = year, decade = decade,
        country = country, countryName = "Morocco", mood = mood,
        stream = "stream", cover = "cover",
    )

    @Test
    fun `matches honours country, decade and mood with null meaning any`() {
        val t = track("t", country = "MAR", decade = 1980, mood = Mood.FAST)
        assertTrue(CatalogLogic.matches(t, "MAR", 1980, Mood.FAST))
        assertFalse(CatalogLogic.matches(t, "USA", 1980, Mood.FAST))
        assertFalse(CatalogLogic.matches(t, "MAR", 1990, Mood.FAST))
        assertFalse(CatalogLogic.matches(t, "MAR", 1980, Mood.SLOW))
        assertTrue(CatalogLogic.matches(t, null, null, null))
    }

    @Test
    fun `filter returns only matching tracks`() {
        val tracks = listOf(
            track("a", country = "MAR", decade = 1980),
            track("b", country = "USA", decade = 1980),
            track("c", country = "MAR", decade = 1990),
        )
        assertEquals(listOf("a"), CatalogLogic.filter(tracks, "MAR", 1980).map { it.id })
        assertEquals(setOf("a", "b"), CatalogLogic.filter(tracks, decade = 1980).map { it.id }.toSet())
        assertEquals(setOf("a", "c"), CatalogLogic.filter(tracks, country = "MAR").map { it.id }.toSet())
    }

    @Test
    fun `shuffled returns a permutation of the input`() {
        val tracks = (0 until 12).map { track("t$it") }
        val q = CatalogLogic.shuffled(tracks, Random(7))
        assertEquals(tracks.size, q.size)
        assertEquals(tracks.map { it.id }.toSet(), q.map { it.id }.toSet())
    }

    @Test
    fun `shuffled is reproducible per seed and differs across seeds`() {
        val tracks = (0 until 12).map { track("t$it") }
        assertEquals(
            CatalogLogic.shuffled(tracks, Random(42)).map { it.id },
            CatalogLogic.shuffled(tracks, Random(42)).map { it.id },
        )
        assertNotEquals(
            CatalogLogic.shuffled(tracks, Random(1)).map { it.id },
            CatalogLogic.shuffled(tracks, Random(2)).map { it.id },
        )
    }

    @Test
    fun `shuffled never starts with the playing track when alternatives exist`() {
        val tracks = (0 until 12).map { track("t$it") }
        for (seed in 0L until 25L) {
            val q = CatalogLogic.shuffled(tracks, Random(seed), avoidFirstId = "t5")
            assertEquals(12, q.size)
            assertNotEquals("t5", q.first().id)
            assertTrue(q.any { it.id == "t5" })
        }
    }

    @Test
    fun `shuffled keeps a lone track even when it is the avoided one`() {
        val q = CatalogLogic.shuffled(listOf(track("only")), Random(3), avoidFirstId = "only")
        assertEquals(listOf("only"), q.map { it.id })
    }
}
