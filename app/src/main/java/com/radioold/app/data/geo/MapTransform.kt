package com.radioold.app.data.geo

/**
 * Screen <-> world transform for the map: a base equirectangular fit (normalized
 * [0,1] world -> screen) combined with user zoom (about the screen centre) and
 * pan. Pure math, kept out of Compose so drawing and hit-testing provably stay
 * in sync (unit-tested) and so zoom-about-centroid is testable.
 */
class MapTransform(
    val baseScale: Float,
    val ox: Float,
    val oy: Float,
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val cx: Float = 0f,
    val cy: Float = 0f,
) {
    fun toScreenX(nx: Float): Float {
        val bx = ox + nx * baseScale
        return cx + (bx - cx) * zoom + panX
    }

    fun toScreenY(ny: Float): Float {
        val by = oy + ny * (baseScale / 2f) // world aspect is 2:1
        return cy + (by - cy) * zoom + panY
    }

    fun toNormX(px: Float): Float {
        val bx = (px - panX - cx) / zoom + cx
        return (bx - ox) / baseScale
    }

    fun toNormY(py: Float): Float {
        val by = (py - panY - cy) / zoom + cy
        return (by - oy) / (baseScale / 2f)
    }

    fun withZoomPan(zoom: Float, panX: Float, panY: Float): MapTransform =
        MapTransform(baseScale, ox, oy, zoom, panX, panY, cx, cy)

    /**
     * Apply a pinch/drag: scale by [zoomChange] keeping the world point under
     * ([centroidX],[centroidY]) fixed on screen, then translate by (dragX,dragY).
     * Zoom is clamped to [minZoom]..[maxZoom].
     */
    fun zoomPan(
        zoomChange: Float,
        dragX: Float,
        dragY: Float,
        centroidX: Float,
        centroidY: Float,
        minZoom: Float = MIN_ZOOM,
        maxZoom: Float = MAX_ZOOM,
    ): MapTransform {
        val newZoom = (zoom * zoomChange).coerceIn(minZoom, maxZoom)
        val k = newZoom / zoom
        val newPanX = k * panX + (1 - k) * (centroidX - cx) + dragX
        val newPanY = k * panY + (1 - k) * (centroidY - cy) + dragY
        return withZoomPan(newZoom, newPanX, newPanY)
    }

    companion object {
        const val MIN_ZOOM = 0.8f
        const val MAX_ZOOM = 12f

        /** Fit the whole 2:1 world inside a screen of (width,height), centred. */
        fun fit(width: Float, height: Float, cx: Float, cy: Float): MapTransform {
            val s = minOf(width, height * 2f)
            val ox = (width - s) / 2f
            val oy = (height - s / 2f) / 2f
            return MapTransform(s, ox, oy, 1f, 0f, 0f, cx, cy)
        }
    }
}
