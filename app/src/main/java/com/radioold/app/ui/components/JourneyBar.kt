package com.radioold.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radioold.app.data.model.Stop
import com.radioold.app.data.model.Track

/**
 * Taxi-mode journey builder: an ordered list of (country, decade, mood) stops
 * that can be added from the current selection and played as one trip.
 */
@Composable
fun JourneyBar(
    journey: List<Stop>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onPlay: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Taxi journey", style = MaterialTheme.typography.labelLarge)
            Row {
                TextButton(onClick = onAdd) { Text("+ Stop") }
                TextButton(onClick = onPlay, enabled = journey.isNotEmpty()) { Text("▶ Ride") }
                TextButton(onClick = onClear, enabled = journey.isNotEmpty()) { Text("Clear") }
            }
        }

        if (journey.isEmpty()) {
            Text(
                "Add stops to build a musical trip across the world.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                journey.forEachIndexed { index, stop ->
                    val decadeLabel = if (stop.decade >= Track.NOW_DECADE) "Now" else "${stop.decade}s"
                    val moodLabel = stop.mood?.let { " · ${it.label}" } ?: ""
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${index + 1}. ${stop.countryName} · $decadeLabel$moodLabel",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = { onRemove(index) }) { Text("✕") }
                    }
                }
            }
        }
    }
}
