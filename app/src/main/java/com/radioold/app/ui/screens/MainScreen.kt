package com.radioold.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.radioold.app.data.model.Mood
import com.radioold.app.data.model.Track
import com.radioold.app.ui.components.DecadeStrip
import com.radioold.app.ui.components.JourneyBar
import com.radioold.app.ui.components.MoodPicker
import com.radioold.app.ui.components.PlayerBar
import com.radioold.app.ui.components.PlayPauseButton
import com.radioold.app.ui.map.WorldMapCanvas
import com.radioold.app.ui.theme.Ink
import com.radioold.app.ui.theme.InkSoft
import com.radioold.app.ui.theme.Parchment
import com.radioold.app.ui.theme.ParchmentDeep
import com.radioold.app.ui.theme.Sepia
import com.radioold.app.viewmodel.RadiooldViewModel

@Composable
fun MainScreen(viewModel: RadiooldViewModel) {
    val loading by viewModel.loading.collectAsState()
    val world by viewModel.world.collectAsState()
    val allTracks by viewModel.tracks.collectAsState()
    val filtered by viewModel.filtered.collectAsState()
    val selection by viewModel.selection.collectAsState()
    val current by viewModel.current.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.positionMs.collectAsState()
    val duration by viewModel.durationMs.collectAsState()
    val journey by viewModel.journey.collectAsState()

    val coverage = remember(allTracks) { allTracks.groupingBy { it.country }.eachCount() }
    var panelOpen by rememberSaveable { mutableStateOf(true) }

    Box(Modifier.fillMaxSize().background(Parchment)) {
        WorldMapCanvas(
            world = world,
            selected = selection.country,
            coverage = coverage,
            onCountryTap = { viewModel.selectCountry(it.id, it.name) },
            modifier = Modifier.fillMaxSize(),
        )

        SelectionBanner(
            countryName = selection.countryName,
            decade = selection.decade,
            mood = selection.mood,
            trackCount = filtered.size,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
        )

        Surface(
            color = ParchmentDeep,
            contentColor = Ink,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        ) {
            Column(Modifier.padding(bottom = 8.dp)) {
                HorizontalDivider(Modifier.fillMaxWidth(), thickness = 2.dp, color = Ink)
                HorizontalDivider(Modifier.fillMaxWidth().padding(top = 2.dp), thickness = 1.dp, color = Sepia)

                // Panel head: closed = a slim mini player, open = the masthead.
                // Either way the map keeps the rest of the screen.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 2.dp),
                ) {
                    if (panelOpen) {
                        Text(
                            "RADIOOLD · THE MUSICAL TIME MACHINE",
                            style = MaterialTheme.typography.labelLarge,
                            color = InkSoft,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Column(Modifier.weight(1f)) {
                            Text(
                                current?.let { "${it.artist} — ${it.title}" } ?: "RADIOOLD",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                current?.let { "${it.countryName} · ${it.decadeLabel}" } ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = InkSoft,
                                maxLines = 1,
                            )
                        }
                        PlayPauseButton(
                            isPlaying = isPlaying,
                            onToggle = viewModel::togglePlay,
                            diameter = 40.dp,
                        )
                    }
                    TextButton(onClick = { panelOpen = !panelOpen }) {
                        Text(
                            if (panelOpen) "▾ CLOSE" else "▴ OPEN",
                            color = Sepia,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                if (panelOpen) {
                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 2.dp))
                    DecadeStrip(
                        selected = selection.decade,
                        onSelect = { viewModel.selectDecade(it) },
                    )
                    MoodPicker(
                        selected = selection.mood,
                        onSelect = { viewModel.selectMood(it) },
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    PlayerBar(
                        current = current,
                        queue = queue,
                        isPlaying = isPlaying,
                        positionMs = position,
                        durationMs = duration,
                        onToggle = viewModel::togglePlay,
                        onNext = viewModel::next,
                        onPrev = viewModel::prev,
                        onSeek = viewModel::seekTo,
                        onPlayAt = viewModel::playAt,
                    )
                    JourneyBar(
                        journey = journey,
                        onAdd = viewModel::addStopToJourney,
                        onRemove = viewModel::removeStop,
                        onPlay = viewModel::playJourney,
                        onClear = viewModel::clearJourney,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionBanner(
    countryName: String?,
    decade: Int?,
    mood: Mood?,
    trackCount: Int,
    modifier: Modifier = Modifier,
) {
    val decadeLabel = when (decade) {
        null -> "All years"
        Track.NOW_DECADE -> "Now"
        else -> "${decade}s"
    }
    val moodLabel = mood?.label ?: "Any mood"
    val text = if (countryName == null) {
        "Worldwide · $decadeLabel · $moodLabel · $trackCount tracks"
    } else {
        "$countryName · $decadeLabel · $moodLabel · $trackCount tracks"
    }
    Surface(
        color = ParchmentDeep,
        contentColor = Ink,
        border = BorderStroke(1.5.dp, Sepia),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
