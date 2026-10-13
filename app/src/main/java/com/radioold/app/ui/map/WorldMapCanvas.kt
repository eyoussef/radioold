package com.radioold.app.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.radioold.app.data.geo.CountryShape
import com.radioold.app.data.geo.MapTransform
import com.radioold.app.data.geo.WorldMap
import com.radioold.app.ui.theme.Brass
import com.radioold.app.ui.theme.Ink
import com.radioold.app.ui.theme.SealRed
import com.radioold.app.ui.theme.Sepia

/**
 * Pannable / zoomable equirectangular world map. Countries are coloured by how
 * much music they have (muted = none, warm gold = lots), the selection glows,
 * and a graticule + gradient sea give it a finished map look. Tap selects a
 * country; drag pans and pinch zooms (about the finger). Drawing and hit-testing
 * both use [MapTransform] so taps always match what is on screen.
 */
@Composable
fun WorldMapCanvas(
    world: WorldMap,
    selected: String?,
    coverage: Map<String, Int>,
    onCountryTap: (CountryShape) -> Unit,
    modifier: Modifier = Modifier,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableStateOf(1.15f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .pointerInput(world) {
                detectTapGestures { pos ->
                    val t = currentTransform(canvasSize, zoom, pan) ?: return@detectTapGestures
                    world.hitTest(t.toNormX(pos.x), t.toNormY(pos.y))?.let { onCountryTap(it) }
                }
            }
            .pointerInput(world) {
                detectTransformGestures { centroid, panChange, zoomChange, _ ->
                    val fit = baseFit(canvasSize) ?: return@detectTransformGestures
                    val cur = fit.withZoomPan(zoom, pan.x, pan.y)
                    val next = cur.zoomPan(zoomChange, panChange.x, panChange.y, centroid.x, centroid.y)
                    zoom = next.zoom
                    pan = Offset(next.panX, next.panY)
                }
            }
    ) {
        val t = currentTransform(canvasSize, zoom, pan) ?: return@Canvas

        // Sea: vertical gradient for depth. (DrawScope's own `size` is used here.)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(SeaDeep, SeaMid, SeaLight),
                startY = 0f,
                endY = size.height,
            )
        )
        drawGraticule(t)

        val maxCoverage = (coverage.values.maxOrNull() ?: 1).coerceAtLeast(1)
        for (country in world.countries) {
            val isSel = country.id == selected
            val cov = coverage[country.id] ?: 0
            val path = buildPath(country, t)

            if (isSel) {
                // Warm brass glow beneath the chosen land.
                drawPath(path, Brass.copy(alpha = 0.5f), style = Stroke(width = 9.dp.toPx()))
            }
            drawPath(path, if (isSel) SealRed else coverageColor(cov, maxCoverage), style = Fill)
            drawPath(
                path = path,
                color = if (isSel) Ink else BorderColor,
                style = Stroke(width = if (isSel) 2.dp.toPx() else 0.8.dp.toPx()),
            )
        }

        // Aged edges and a chartmaker's double rule, like an old map plate.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0x2B2B2019)),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = maxOf(size.width, size.height) * 0.72f,
            )
        )
        drawRect(color = Sepia, style = Stroke(width = 2.dp.toPx()))
        drawRect(
            color = Sepia.copy(alpha = 0.5f),
            style = Stroke(width = 1.dp.toPx()),
            topLeft = Offset(5f, 5f),
            size = Size(size.width - 10f, size.height - 10f),
        )
    }
}

private fun baseFit(canvasSize: IntSize): MapTransform? {
    if (canvasSize == IntSize.Zero) return null
    val w = canvasSize.width.toFloat()
    val h = canvasSize.height.toFloat()
    return MapTransform.fit(w, h, w / 2f, h / 2f)
}

private fun currentTransform(canvasSize: IntSize, zoom: Float, pan: Offset): MapTransform? =
    baseFit(canvasSize)?.withZoomPan(zoom, pan.x, pan.y)

private fun coverageColor(cov: Int, maxCoverage: Int): Color {
    if (cov <= 0) return LandEmpty
    val t = (cov.toFloat() / maxCoverage).coerceIn(0f, 1f)
    return lerp(LandLow, LandHigh, t)
}

private fun DrawScope.drawGraticule(t: MapTransform) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
    for (lon in -180..180 step 30) {
        val x = t.toScreenX((lon + 180f) / 360f)
        drawLine(Graticule, Offset(x, t.toScreenY(0f)), Offset(x, t.toScreenY(1f)), strokeWidth = 1f, pathEffect = dash)
    }
    for (lat in -60..60 step 30) {
        val y = t.toScreenY((90f - lat) / 180f)
        drawLine(Graticule, Offset(t.toScreenX(0f), y), Offset(t.toScreenX(1f), y), strokeWidth = 1f, pathEffect = dash)
    }
}

private fun buildPath(shape: CountryShape, t: MapTransform): Path {
    val path = Path()
    for (ring in shape.rings) {
        var i = 0
        while (i < ring.size) {
            val x = t.toScreenX(ring[i])
            val y = t.toScreenY(ring[i + 1])
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            i += 2
        }
        path.close()
    }
    return path
}

// Map palette (old portolan chart: bare paper sea, faded washes, brown ink).
private val SeaDeep = Color(0xFFB7C0AE)
private val SeaMid = Color(0xFFCBD1BC)
private val SeaLight = Color(0xFFDDE0CB)
private val Graticule = Color(0x406B5844)
private val BorderColor = Color(0xFF4A3826)
private val LandEmpty = Color(0xFFD3C19B)
private val LandLow = Color(0xFFC79E63)
private val LandHigh = Color(0xFF9C5E2F)
