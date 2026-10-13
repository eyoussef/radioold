package com.radioold.app.data.catalog

import com.radioold.app.data.geo.CountryShape
import com.radioold.app.data.geo.WorldMap
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure JSON -> model mapping for the bundled world geometry (tracks come live
 * from YouTube Music + iTunes + Wikidata, so only the map is parsed from
 * assets).
 */
object CatalogParser {

    fun parseWorld(json: String): WorldMap {
        val arr = JSONObject(json).optJSONArray("countries") ?: JSONArray()
        val countries = ArrayList<CountryShape>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val ringsArr = o.optJSONArray("rings") ?: JSONArray()
            val rings = ArrayList<FloatArray>(ringsArr.length())
            for (r in 0 until ringsArr.length()) {
                val pts = ringsArr.getJSONArray(r)
                val fa = FloatArray(pts.length() * 2)
                for (p in 0 until pts.length()) {
                    val pt = pts.getJSONArray(p)
                    fa[p * 2] = pt.getDouble(0).toFloat()
                    fa[p * 2 + 1] = pt.getDouble(1).toFloat()
                }
                if (fa.size >= 6) rings.add(fa)
            }
            if (rings.isNotEmpty()) {
                countries.add(CountryShape(o.getString("id"), o.optString("name", o.getString("id")), rings))
            }
        }
        return WorldMap(countries)
    }
}
