package com.radioold.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.radioold.app.data.model.Track
import com.radioold.app.ui.theme.Ink
import com.radioold.app.ui.theme.InkSoft
import com.radioold.app.ui.theme.ParchmentDeep
import com.radioold.app.ui.theme.SealRed
import com.radioold.app.ui.theme.Sepia

/**
 * The vintage player card: a cover framed like an old photograph (tap it for
 * the record's full metadata), serif captions, a wax-seal transport and an
 * "Up next" rail — tap any upcoming track to jump straight to it.
 */
@Composable
fun PlayerBar(
    current: Track?,
    queue: List<Track>,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlayAt: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo && current != null) {
        TrackInfoDialog(track = current, onDismiss = { showInfo = false })
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs) else 0f
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { frac -> onSeek((frac * durationMs).toLong()) },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = SealRed,
                activeTrackColor = Sepia,
                inactiveTrackColor = ParchmentDeep,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FramedCover(current?.cover, 78.dp, onClick = { if (current != null) showInfo = true })
            Column(Modifier.weight(1f)) {
                Text(
                    text = current?.title ?: "Choose a land and a year on the map",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = current?.artist ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = InkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = current?.let { "${it.countryName} · ${it.decadeLabel}" } ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    color = Sepia,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onPrev) { Text("◀◀", color = Ink, style = MaterialTheme.typography.titleMedium) }
            PlayPauseButton(isPlaying = isPlaying, onToggle = onToggle)
            TextButton(onClick = onNext) { Text("▶▶", color = Ink, style = MaterialTheme.typography.titleMedium) }
        }

        val currentIndex = queue.indexOfFirst { it.id == current?.id }
        val upcoming = queue.drop((currentIndex + 1).coerceAtLeast(0)).take(UP_NEXT_COUNT)
        if (upcoming.size > 1) {
            Text(
                "UP NEXT",
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(upcoming.size) { i ->
                    val track = upcoming[i]
                    UpNextCard(track = track, onClick = { onPlayAt(currentIndex + 1 + i) })
                }
            }
        }
    }
}

@Composable
private fun UpNextCard(track: Track, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(196.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, Sepia.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
            .background(ParchmentDeep.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FramedCover(track.cover, 40.dp)
        Column {
            Text(
                track.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.labelSmall,
                color = InkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Cover art behind a double rule, like a photograph pasted into an album. */
@Composable
private fun FramedCover(cover: String?, size: Dp, onClick: (() -> Unit)? = null) {
    val framed = Modifier
        .size(size)
        .clip(RoundedCornerShape(3.dp))
        .background(ParchmentDeep)
        .border(2.dp, Ink, RoundedCornerShape(3.dp))
        .padding(3.dp)
        .border(1.dp, Sepia.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Box(framed, contentAlignment = Alignment.Center) {
        if (!cover.isNullOrBlank()) {
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(1.dp)),
            )
        } else {
            Text("♪", style = MaterialTheme.typography.titleMedium, color = InkSoft)
        }
    }
}

private const val UP_NEXT_COUNT = 8
