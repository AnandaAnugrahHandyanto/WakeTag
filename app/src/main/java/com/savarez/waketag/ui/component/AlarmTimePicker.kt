package com.savarez.waketag.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.rememberHapticFeedback

/** Premium compact time display with a dual hour/minute picker dialog. */
@Composable
fun AlarmTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val shape = MaterialTheme.shapes.extraLarge

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                    shape
                )
                .clickable { haptic(); pickerOpen = true }
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "WAKTU ALARM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.8.sp
                    )
                    Text(
                        text = "%02d:%02d".format(hour, minute),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Ubah",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    if (pickerOpen) {
        Dialog(onDismissRequest = { pickerOpen = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp
            ) {
                DualTimePicker(
                    hour = hour,
                    minute = minute,
                    onHourChange = onHourChange,
                    onMinuteChange = onMinuteChange,
                    onDone = { haptic(); pickerOpen = false }
                )
            }
        }
    }
}

@Composable
private fun DualTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Atur waktu", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Geser angka atau ketuk pilihan yang kamu mau",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactWheel(
                label = "JAM",
                value = hour,
                range = 0..23,
                onValueChange = onHourChange,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            CompactWheel(
                label = "MENIT",
                value = minute,
                range = 0..59,
                onValueChange = onMinuteChange,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDone) { Text("Selesai") }
        }
    }
}

@Composable
private fun CompactWheel(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val dragState = rememberDraggableState { delta ->
        dragDistance += delta
        while (dragDistance <= -34f && value < range.last) {
            dragDistance += 34f
            onValueChange(value + 1)
            haptic()
        }
        while (dragDistance >= 34f && value > range.first) {
            dragDistance -= 34f
            onValueChange(value - 1)
            haptic()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(178.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
                .draggable(dragState, Orientation.Vertical)
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ((value - 2).coerceAtLeast(range.first)..(value + 2).coerceAtMost(range.last)).forEach { option ->
                val selected = option == value
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                        )
                        .clickable { haptic(); onValueChange(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d".format(option),
                        style = if (selected) MaterialTheme.typography.titleMedium
                        else MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmTimePickerPreview() {
    WakeTagTheme {
        AlarmTimePicker(hour = 7, minute = 30, onHourChange = {}, onMinuteChange = {})
    }
}
