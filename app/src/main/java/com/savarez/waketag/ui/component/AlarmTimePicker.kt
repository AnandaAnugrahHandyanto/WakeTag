package com.savarez.waketag.ui.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.savarez.waketag.ui.theme.WakeTagTheme

private const val ALARM_TIME_PICKER_LOG_TAG = "AlarmTimePicker"

@Composable
fun AlarmTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activePicker by rememberSaveable { mutableStateOf<PickerType?>(null) }

    fun openPicker(pickerType: PickerType) {
        val wasExpanded = activePicker != null
        if (!wasExpanded) {
            Log.d(ALARM_TIME_PICKER_LOG_TAG, "expanded changed: true")
        }
        Log.d(ALARM_TIME_PICKER_LOG_TAG, "${pickerType.label} picker opened")
        activePicker = pickerType
    }

    fun closePicker() {
        if (activePicker != null) {
            Log.d(ALARM_TIME_PICKER_LOG_TAG, "expanded changed: false")
        }
        activePicker = null
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {

        val compact = maxWidth < 360.dp

        if (compact) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberPickerField(
                    label = "Hour",
                    value = hour,
                    onClick = { openPicker(PickerType.HOUR) },
                    modifier = Modifier.fillMaxWidth()
                )
                NumberPickerField(
                    label = "Minute",
                    value = minute,
                    onClick = { openPicker(PickerType.MINUTE) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberPickerField(
                    label = "Hour",
                    value = hour,
                    onClick = { openPicker(PickerType.HOUR) },
                    modifier = Modifier.weight(1f)
                )
                NumberPickerField(
                    label = "Minute",
                    value = minute,
                    onClick = { openPicker(PickerType.MINUTE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    activePicker?.let { pickerType ->
        NumberSelectionDialog(
            title = "Select ${pickerType.label}",
            selectedValue = if (pickerType == PickerType.HOUR) hour else minute,
            range = if (pickerType == PickerType.HOUR) 0..23 else 0..59,
            onDismiss = { closePicker() },
            onValueSelected = { selected ->
                Log.d(ALARM_TIME_PICKER_LOG_TAG, "${pickerType.label} selected: $selected")
                if (pickerType == PickerType.HOUR) {
                    onHourChange(selected)
                } else {
                    onMinuteChange(selected)
                }
                closePicker()
            }
        )
    }
}

@Composable
private fun NumberPickerField(
    label: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "%02d".format(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { Text(text = "▼") },
            modifier = Modifier
                .fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clickable { onClick() }
        )
    }
}

@Composable
private fun NumberSelectionDialog(
    title: String,
    selectedValue: Int,
    range: IntRange,
    onDismiss: () -> Unit,
    onValueSelected: (Int) -> Unit
) {
    val initialIndex = (selectedValue - range.first).coerceIn(0, range.last - range.first)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (initialIndex - 3).coerceAtLeast(0))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(range.toList()) { option ->
                    val isSelected = option == selectedValue
                    TextButton(
                        onClick = { onValueSelected(option) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "%02d".format(option),
                            style = if (isSelected) {
                                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

private enum class PickerType(val label: String) {
    HOUR("Hour"),
    MINUTE("Minute")
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
