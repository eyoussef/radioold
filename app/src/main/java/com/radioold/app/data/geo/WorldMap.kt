package com.radioold.app.data.geo

/**
 * World-map geometry for the Compose canvas: country outlines in normalized
 * equirectangular space (x = (lon+180)/360, y = (90-lat)/180), both in [0,1].
 * Stored pure-Kotlin (FloatArray rings) so hit-testing is unit-testable.
 */
data class CountryShape(
    val id: String,
    val name: String,
    val rings: List<FloatArray>, // each ring: [x0,y0,x1,y1,...] normalized 0..1
)

class WorldMap(val countries: List<CountryShape>) {

    /** Point-in-polygon hit test in normalized coordinates. Returns the country hit, if any. */
    fun hitTest(x: Float, y: Float): CountryShape? {
        // reverse order so smaller/later shapes (drawn on top) win
        for (i in countries.indices.reversed()) {
            val c = countries[i]
            for (ring in c.rings) {
                if (pointInRing(ring, x, y)) return c
            }
        }
        return null
    }

    fun byId(id: String): CountryShape? = countries.firstOrNull { it.id == id }

    companion object {
        fun pointInRing(ring: FloatArray, x: Float, y: Float): Boolean {
            var inside = false
            var j = ring.size - 2
            var i = 0
            while (i < ring.size) {
                val xi = ring[i]; val yi = ring[i + 1]
                val xj = ring[j]; val yj = ring[j + 1]
                val intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi + 0f) + xi)
                if (intersect) inside = !inside
                j = i
                i += 2
            }
            return inside
        }

        fun lonLatToNorm(lon: Float, lat: Float): Pair<Float, Float> =
            ((lon + 180f) / 360f) to ((90f - lat) / 180f)
    }
}
