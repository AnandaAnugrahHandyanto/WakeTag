package com.savarez.waketag.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.displayLabel
import com.savarez.waketag.util.rememberHapticFeedback

@Composable
fun AlarmCard(
    alarm: Alarm,
    onEnabledChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val timeColor = if (alarm.enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val accent = if (alarm.label.isNotBlank()) {
        alarmLabelAccent(alarm.label)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val edgeColor = if (alarm.enabled) accent else MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (alarm.enabled) {
                accent.copy(alpha = 0.32f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Pantulan warna label di sudut kanan atas.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 34.dp, y = (-42).dp)
                    .size(136.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                edgeColor.copy(alpha = if (alarm.enabled) 0.16f else 0.06f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(50.dp)
                            .clip(CircleShape)
                            .background(edgeColor.copy(alpha = if (alarm.enabled) 0.85f else 0.35f))
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (alarm.label.isNotBlank()) {
                            AlarmLabelPill(label = alarm.label)
                        }

                        Text(
                            text = "%02d:%02d".format(alarm.hour, alarm.minute),
                            style = MaterialTheme.typography.displaySmall,
                            color = timeColor
                        )

                        MethodPill(dismissType = alarm.dismissType)
                        Text(
                            text = activeDaysLabel(alarm.daysOfWeek),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = alarm.enabled,
                        onCheckedChange = { enabled -> haptic(); onEnabledChange(enabled) }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { haptic(); onEditClick() }) {
                        Text(text = "Ubah")
                    }
                    TextButton(onClick = { haptic(); onDeleteClick() }) {
                        Text(
                            text = "Hapus",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private val CARD_DAY_LABELS = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
private fun activeDaysLabel(days: Int): String = when (days) {
    127 -> "Setiap hari"
    0 -> "Belum dipilih"
    else -> CARD_DAY_LABELS.filterIndexed { index, _ -> days and (1 shl index) != 0 }.joinToString(" · ")
}

@Composable
private fun MethodPill(dismissType: DismissType) {
    val isNfc = dismissType == DismissType.NFC
    Surface(
        shape = CircleShape,
        color = if (isNfc) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = dismissType.displayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isNfc) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmCardPreview() {
    WakeTagTheme {
        AlarmCard(
            alarm = Alarm(
                id = 1,
                hour = 7,
                minute = 15,
                enabled = true,
                dismissType = DismissType.NFC,
                label = "Kuliah"
            ),
            onEnabledChange = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
