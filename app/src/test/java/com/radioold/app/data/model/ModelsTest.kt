package com.radioold.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {

    @Test
    fun `decadeOf buckets years into decades`() {
        assertEquals(1900, Track.decadeOf(1903))
        assertEquals(1950, Track.decadeOf(1954))
        assertEquals(1950, Track.decadeOf(1959))
        assertEquals(2010, Track.decadeOf(2015))
    }

    @Test
    fun `decadeOf maps modern years to Now`() {
        assertEquals(Track.NOW_DECADE, Track.decadeOf(2020))
        assertEquals(Track.NOW_DECADE, Track.decadeOf(2026))
    }

    @Test
    fun `decadeLabel renders Now and s decades`() {
        val now = Track("i", "t", "a", 2024, Track.NOW_DECADE, "FRA", "France",
            Mood.SLOW, stream = "s", cover = "c")
        val old = Track("i", "t", "a", 1954, 1950, "FRA", "France",
            Mood.SLOW, stream = "s", cover = "c")
        assertEquals("Now", now.decadeLabel)
        assertEquals("1950s", old.decadeLabel)
    }

    @Test
    fun `durationLabel renders minutes and seconds`() {
        val t = Track("i", "t", "a", 1984, 1980, "MAR", "Morocco",
            Mood.SLOW, durationSec = 224)
        assertEquals("3:44", t.durationLabel)
    }
}
