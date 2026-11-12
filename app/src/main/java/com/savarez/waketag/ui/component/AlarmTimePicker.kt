package com.savarez.waketag.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.rememberHapticFeedback

/**
 * Pemilih jam dan menit.
 *
 * Angka jam dan menit ditampilkan besar seperti jam digital, lalu
 * bagian yang dipilih ditampilkan sebagai daftar angka yang bisa
 * di-scroll untuk dilihat dan dipilih.
 */
@Composable
fun AlarmTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activePicker by rememberSaveable { mutableStateOf<PickerType?>(null) }
    val haptic = rememberHapticFeedback()
    val shape = MaterialTheme.shapes.extraLarge

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                    shape = shape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeSection(
                    label = "JAM",
                    value = hour,
                    onClick = { haptic(); activePicker = PickerType.HOUR },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                TimeSection(
                    label = "MENIT",
                    value = minute,
                    onClick = { haptic(); activePicker = PickerType.MINUTE },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        activePicker?.let { pickerType ->
            ScrollingNumberPicker(
                title = pickerType.label,
                value = if (pickerType == PickerType.HOUR) hour else minute,
                range = pickerType.range,
                onValueChange = if (pickerType == PickerType.HOUR) onHourChange else onMinuteChange,
                onDismiss = { activePicker = null }
            )
        }
    }
}

@Composable
private fun TimeSection(
    label: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.6.sp
        )
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(22.dp)
                .height(2.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun ScrollingNumberPicker(
    title: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val scrollState = rememberScrollState()
    val itemHeightPx = with(LocalDensity.current) { 64.dp.toPx() }
    val itemStridePx = with(LocalDensity.current) { 72.dp.toPx() }

    LaunchedEffect(value) {
        val selectedIndex = (value - range.first).coerceIn(0, range.count() - 1)
        val targetOffset = (selectedIndex * itemStridePx).coerceIn(0f, scrollState.maxValue.toFloat())
        scrollState.animateScrollTo(
            targetOffset.toInt(),
            tween(durationMillis = 350)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { haptic(); onDismiss() }) {
                Text(text = "Batal")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            range.toList().forEach { option ->
                NumberOption(
                    value = option,
                    isSelected = option == value,
                    onClick = { haptic(); onValueChange(option) }
                )
            }
        }
    }
}

@Composable
private fun NumberOption(
    value: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { haptic(); onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = if (isSelected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private enum class PickerType(
    val label: String,
    val range: IntRange
) {
    HOUR("Jam", 0..23),
    MINUTE("Menit", 0..59)
}

@Preview(showBackground = true)
@Composable
private fun AlarmTimePickerPreview() {
    WakeTagTheme {
        AlarmTimePicker(
            hour = 7,
            minute = 30,
            onHourChange = {},
            onMinuteChange = {}
        )
    }
}
