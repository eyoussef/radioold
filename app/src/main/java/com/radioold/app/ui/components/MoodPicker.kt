package com.radioold.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radioold.app.data.model.Mood
import com.radioold.app.ui.theme.Ink
import com.radioold.app.ui.theme.ParchmentDeep
import com.radioold.app.ui.theme.SealRed
import com.radioold.app.ui.theme.Sepia
import com.radioold.app.ui.theme.TextOnAccent

/** Mood selector: Any / Slow / Fast / Weird — paper chips with a seal-red mark. */
@Composable
fun MoodPicker(
    selected: Mood?,
    onSelect: (Mood?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MoodChip(selected == null, "Any") { onSelect(null) }
        Mood.ALL.forEach { mood ->
            MoodChip(selected == mood, mood.label) { onSelect(mood) }
        }
    }
}

@Composable
private fun MoodChip(isSelected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SealRed,
            selectedLabelColor = TextOnAccent,
            containerColor = ParchmentDeep,
            labelColor = Ink,
        ),
        border = BorderStroke(1.dp, if (isSelected) SealRed else Sepia.copy(alpha = 0.55f)),
    )
}
