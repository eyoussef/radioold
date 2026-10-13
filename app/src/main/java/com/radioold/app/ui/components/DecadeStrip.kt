package com.radioold.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radioold.app.data.model.Track
import com.radioold.app.ui.theme.Ink
import com.radioold.app.ui.theme.ParchmentDeep
import com.radioold.app.ui.theme.SealRed
import com.radioold.app.ui.theme.Sepia
import com.radioold.app.ui.theme.TextOnAccent

/**
 * Horizontal decade selector. [selected] null means "All years"; otherwise a
 * decade (with "Now" for the present). Tapping a chip filters the catalog.
 */
@Composable
fun DecadeStrip(
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val decades: List<Int?> = listOf(
        null, 1900, 1910, 1920, 1930, 1940, 1950,
        1960, 1970, 1980, 1990, 2000, 2010, Track.NOW_DECADE,
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(decades) { decade ->
            FilterChip(
                selected = decade == selected,
                onClick = { onSelect(decade) },
                label = {
                    Text(
                        when (decade) {
                            null -> "All"
                            Track.NOW_DECADE -> "Now"
                            else -> "${decade}s"
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SealRed,
                    selectedLabelColor = TextOnAccent,
                    containerColor = ParchmentDeep,
                    labelColor = Ink,
                ),
                border = BorderStroke(1.dp, if (decade == selected) SealRed else Sepia.copy(alpha = 0.55f)),
            )
        }
    }
}
