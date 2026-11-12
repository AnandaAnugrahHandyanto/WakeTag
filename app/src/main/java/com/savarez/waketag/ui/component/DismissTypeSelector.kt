package com.savarez.waketag.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.util.rememberHapticFeedback

/**
 * Pemilih metode mematikan alarm dalam bentuk dua kartu ringkas.
 * Teks dibuat singkat agar formulir tidak terasa seperti tutorial.
 */
@Composable
fun DismissTypeSelector(
    selected: DismissType,
    onSelect: (DismissType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DismissTypeOption(
            title = "Normal",
            caption = "Satu tombol",
            selected = selected == DismissType.NORMAL,
            onClick = { onSelect(DismissType.NORMAL) },
            modifier = Modifier.weight(1f)
        )
        DismissTypeOption(
            title = "Tag NFC",
            caption = "Kunci fisik",
            selected = selected == DismissType.NFC,
            onClick = { onSelect(DismissType.NFC) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DismissTypeOption(
    title: String,
    caption: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val shape = RoundedCornerShape(20.dp)

    Surface(
        onClick = { haptic(); onClick() },
        modifier = modifier,
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                SelectionDot(selected = selected)
            }
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    val accent = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .size(18.dp)
            .background(
                color = if (selected) accent else Color.Transparent,
                shape = CircleShape
            )
            .border(width = 1.dp, color = accent, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = MaterialTheme.colorScheme.onPrimary, shape = CircleShape)
            )
        }
    }
}
