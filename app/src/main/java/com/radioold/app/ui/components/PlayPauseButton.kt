package com.radioold.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.radioold.app.ui.theme.Ink
import com.radioold.app.ui.theme.SealRed
import com.radioold.app.ui.theme.TextOnAccent

/**
 * The transport's heart: a wax-seal red disc with the play/pause mark drawn as
 * geometry (never a font glyph, so it renders identically everywhere).
 */
@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 58.dp,
) {
    Box(
        modifier = modifier
            .size(diameter)
            .clip(CircleShape)
            .background(SealRed)
            .border(2.dp, Ink, CircleShape)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(diameter * 0.36f)) {
            if (isPlaying) {
                // Pause: two bars.
                val w = size.width / 3.1f
                drawRoundRect(
                    color = TextOnAccent,
                    topLeft = Offset(0f, 0f),
                    size = Size(w, size.height),
                    cornerRadius = CornerRadius(w * 0.3f),
                )
                drawRoundRect(
                    color = TextOnAccent,
                    topLeft = Offset(size.width - w, 0f),
                    size = Size(w, size.height),
                    cornerRadius = CornerRadius(w * 0.3f),
                )
            } else {
                // Play: a triangle.
                val path = Path().apply {
                    moveTo(size.width * 0.1f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width * 0.1f, size.height)
                    close()
                }
                drawPath(path, TextOnAccent)
            }
        }
    }
}
