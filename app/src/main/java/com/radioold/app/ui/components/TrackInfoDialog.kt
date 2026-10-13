package com.radioold.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.radioold.app.data.model.Track
import com.radioold.app.ui.theme.Ink
import com.radioold.app.ui.theme.InkSoft
import com.radioold.app.ui.theme.ParchmentDeep
import com.radioold.app.ui.theme.Sepia

/**
 * The record card: tapping the cover opens every scrap of metadata the source
 * carries — like reading the liner notes.
 */
@Composable
fun TrackInfoDialog(track: Track, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ParchmentDeep,
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(6.dp),
        title = { Text(track.title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (track.cover.isNotBlank()) {
                    AsyncImage(
                        model = track.cover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(3.dp)),
                    )
                }
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaRow("PERFORMER", track.artist)
                    MetaRow("ALBUM", track.album)
                    MetaRow("LABEL", track.label)
                    MetaRow("SONGWRITER", track.songwriter)
                    MetaRow("YEAR", track.year.toString())
                    MetaRow("TEMPERAMENT", track.mood.label)
                    MetaRow("LAND", track.countryName)
                    MetaRow("DURATION", track.durationLabel)
                    MetaRow("FAVOURITES", track.likes.toString())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = Sepia, style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

@Composable
private fun MetaRow(name: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = InkSoft,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
    }
}
