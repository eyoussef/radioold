package com.radioold.app.data.geo

import org.junit.Assert.assertEquals
import org.junit.Test

class MapTransformTest {

    private val width = 1080f
    private val height = 1920f
    private val fit = MapTransform.fit(width, height, width / 2f, height / 2f)

    @Test
    fun `screen to world roundtrip is identity`() {
        for ((nx, ny) in listOf(0f to 0f, 0.5f to 0.5f, 1f to 1f, 0.23f to 0.77f)) {
            val sx = fit.toScreenX(nx)
            val sy = fit.toScreenY(ny)
            assertEquals(nx.toDouble(), fit.toNormX(sx).toDouble(), 1e-3)
            assertEquals(ny.toDouble(), fit.toNormY(sy).toDouble(), 1e-3)
        }
    }

    @Test
    fun `roundtrip stays identity under zoom and pan`() {
        val t = fit.zoomPan(2.5f, 120f, -80f, width / 2f, height / 2f)
        for ((nx, ny) in listOf(0.1f to 0.2f, 0.5f to 0.5f, 0.9f to 0.1f)) {
            val sx = t.toScreenX(nx)
            val sy = t.toScreenY(ny)
            assertEquals(nx.toDouble(), t.toNormX(sx).toDouble(), 1e-3)
            assertEquals(ny.toDouble(), t.toNormY(sy).toDouble(), 1e-3)
        }
    }

    @Test
    fun `zoom keeps the point under the centroid fixed`() {
        val cgX = 300f
        val cgY = 400f
        val nx = fit.toNormX(cgX)
        val ny = fit.toNormY(cgY)
        val t = fit.zoomPan(2f, 0f, 0f, cgX, cgY)
        // Same world point should still land at the centroid after zooming.
        assertEquals(cgX.toDouble(), t.toScreenX(nx).toDouble(), 1e-2)
        assertEquals(cgY.toDouble(), t.toScreenY(ny).toDouble(), 1e-2)
    }

    @Test
    fun `zoom is clamped to bounds`() {
        val t = fit.zoomPan(1000f, 0f, 0f, width / 2f, height / 2f)
        assertEquals(MapTransform.MAX_ZOOM.toDouble(), t.zoom.toDouble(), 1e-4)
        val s = fit.zoomPan(0.0001f, 0f, 0f, width / 2f, height / 2f)
        assertEquals(MapTransform.MIN_ZOOM.toDouble(), s.zoom.toDouble(), 1e-4)
    }
}
