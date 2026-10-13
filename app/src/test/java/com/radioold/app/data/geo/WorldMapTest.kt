package com.radioold.app.data.geo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldMapTest {

    private val square = floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f)

    @Test
    fun `pointInRing detects inside and outside`() {
        assertTrue(WorldMap.pointInRing(square, 0.5f, 0.5f))
        assertFalse(WorldMap.pointInRing(square, 1.5f, 0.5f))
        assertFalse(WorldMap.pointInRing(square, 0.5f, -0.5f))
    }

    @Test
    fun `hitTest returns the country under a point`() {
        val shape = CountryShape("FRA", "France", listOf(square))
        val map = WorldMap(listOf(shape))
        assertEquals("FRA", map.hitTest(0.5f, 0.5f)?.id)
        assertNull(map.hitTest(2f, 2f))
    }

    @Test
    fun `hitTest prefers the later drawn shape on overlap`() {
        val lower = CountryShape("AAA", "Lower", listOf(square))
        val upper = CountryShape("BBB", "Upper", listOf(square))
        val map = WorldMap(listOf(lower, upper))
        assertEquals("BBB", map.hitTest(0.5f, 0.5f)?.id)
    }

    @Test
    fun `lonLatToNorm maps projection correctly`() {
        val (x0, y0) = WorldMap.lonLatToNorm(-180f, 90f)   // top-left
        val (x1, y1) = WorldMap.lonLatToNorm(180f, -90f)   // bottom-right
        assertEquals(0.0, x0.toDouble(), 1e-4)
        assertEquals(0.0, y0.toDouble(), 1e-4)
        assertEquals(1.0, x1.toDouble(), 1e-4)
        assertEquals(1.0, y1.toDouble(), 1e-4)
    }
}
