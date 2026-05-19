package com.savarez.waketag.ui.screen

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.ui.component.AlarmTimePicker
import com.savarez.waketag.ui.component.DismissTypeChip
import com.savarez.waketag.ui.theme.WakeTagTheme

private const val CREATE_ALARM_LOG_TAG = "CreateAlarmScreen"

@Composable
fun CreateAlarmScreen(
    modifier: Modifier = Modifier,
    title: String = "Create Alarm",
    saveButtonText: String = "Save",
    initialHour: Int = 7,
    initialMinute: Int = 0,
    initialDismissType: DismissType = DismissType.NORMAL,
    initialEnabled: Boolean = true,
    showEnabledToggle: Boolean = false,
    onBackClick: () -> Unit = {},
    onSaveAlarm: (hour: Int, minute: Int, dismissType: DismissType, enabled: Boolean) -> Unit = { hour, minute, dismissType, enabled ->
        Log.d(
            CREATE_ALARM_LOG_TAG,
            "Save alarm tapped: %02d:%02d, dismissType=%s, enabled=%s"
                .format(hour, minute, dismissType.name, enabled)
        )
    }
) {
    var selectedHour by rememberSaveable(initialHour) { mutableIntStateOf(initialHour) }
    var selectedMinute by rememberSaveable(initialMinute) { mutableIntStateOf(initialMinute) }
    var selectedDismissType by rememberSaveable(initialDismissType) { mutableStateOf(initialDismissType) }
    var selectedEnabled by rememberSaveable(initialEnabled) { mutableStateOf(initialEnabled) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }

    CreateAlarmContent(
        title = title,
        saveButtonText = saveButtonText,
        hour = selectedHour,
        minute = selectedMinute,
        dismissType = selectedDismissType,
        enabled = selectedEnabled,
        showEnabledToggle = showEnabledToggle,
        onHourChange = { selectedHour = it },
        onMinuteChange = { selectedMinute = it },
        onDismissTypeChange = { selectedDismissType = it },
        onEnabledChange = { selectedEnabled = it },
        onBackClick = onBackClick,
        onSaveClick = {
            saveError = null
            if (selectedHour !in 0..23 || selectedMinute !in 0..59) {
                saveError = "Please select a valid alarm time."
                Log.e(
                    CREATE_ALARM_LOG_TAG,
                    "Invalid alarm time selected: %02d:%02d".format(selectedHour, selectedMinute)
                )
            } else {
                runCatching { onSaveAlarm(selectedHour, selectedMinute, selectedDismissType, selectedEnabled) }
                    .onFailure { throwable ->
                        saveError = "Failed to save alarm. Please try again."
                        Log.e(CREATE_ALARM_LOG_TAG, "Alarm save failed", throwable)
                    }
            }
        },
        errorMessage = saveError,
        modifier = modifier
    )
}

@Composable
fun CreateAlarmContent(
    title: String,
    saveButtonText: String,
    hour: Int,
    minute: Int,
    dismissType: DismissType,
    enabled: Boolean,
    showEnabledToggle: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onDismissTypeChange: (DismissType) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val horizontalPadding = when {
                maxWidth < 360.dp -> 12.dp
                maxWidth < 600.dp -> 20.dp
                maxWidth < 840.dp -> 24.dp
                else -> 32.dp
            }
            val verticalPadding: Dp = if (maxHeight < 700.dp) 16.dp else 24.dp
            val contentSpacing: Dp = if (maxWidth < 360.dp) 16.dp else 20.dp
            val maxContentWidth: Dp = if (maxWidth >= 840.dp) 640.dp else 560.dp
            val compactActions = maxWidth < 420.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .widthIn(max = maxContentWidth),
                verticalArrangement = Arrangement.spacedBy(contentSpacing)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )

                AlarmTimePicker(
                    hour = hour,
                    minute = minute,
                    onHourChange = onHourChange,
                    onMinuteChange = onMinuteChange
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Dismiss Method",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        DismissType.entries.forEach { type ->
                            DismissTypeChip(
                                dismissType = type,
                                selected = dismissType == type,
                                onClick = { onDismissTypeChange(type) }
                            )
                        }
                    }
                }

                if (showEnabledToggle) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enabled",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = onEnabledChange
                        )
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (compactActions) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Cancel")
                        }
                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = saveButtonText)
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancel")
                        }
                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = saveButtonText)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateAlarmContentPreview() {
    WakeTagTheme {
        CreateAlarmContent(
            hour = 6,
            minute = 45,
            dismissType = DismissType.QR,
            enabled = true,
            showEnabledToggle = true,
            onHourChange = {},
            onMinuteChange = {},
            onDismissTypeChange = {},
            onEnabledChange = {},
            onBackClick = {},
            onSaveClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateAlarmScreenPreview() {
    WakeTagTheme {
        CreateAlarmScreen()
    }
}
