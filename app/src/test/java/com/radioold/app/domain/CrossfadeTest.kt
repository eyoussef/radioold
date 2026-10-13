package com.radioold.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CrossfadeTest {

    @Test
    fun `fadeIn ramps from 0 to 1`() {
        assertEquals(0.0, Crossfade.fadeIn(0L, 1000L).toDouble(), 1e-4)
        assertEquals(0.5, Crossfade.fadeIn(500L, 1000L).toDouble(), 1e-4)
        assertEquals(1.0, Crossfade.fadeIn(1000L, 1000L).toDouble(), 1e-4)
        assertEquals(1.0, Crossfade.fadeIn(2000L, 1000L).toDouble(), 1e-4) // clamped
    }

    @Test
    fun `fadeOut ramps from 1 to 0 as remaining drops`() {
        assertEquals(1.0, Crossfade.fadeOut(1000L, 1000L).toDouble(), 1e-4)
        assertEquals(0.5, Crossfade.fadeOut(500L, 1000L).toDouble(), 1e-4)
        assertEquals(0.0, Crossfade.fadeOut(0L, 1000L).toDouble(), 1e-4)
    }

    @Test
    fun `zero fade disables ramping`() {
        assertEquals(1.0, Crossfade.fadeIn(0L, 0L).toDouble(), 1e-4)
        assertEquals(1.0, Crossfade.fadeOut(0L, 0L).toDouble(), 1e-4)
    }
}
