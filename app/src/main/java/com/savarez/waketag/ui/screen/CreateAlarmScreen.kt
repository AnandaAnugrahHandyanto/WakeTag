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
    onBackClick: () -> Unit = {},
    onSaveAlarm: (hour: Int, minute: Int, dismissType: DismissType) -> Unit = { hour, minute, dismissType ->
        Log.d(
            CREATE_ALARM_LOG_TAG,
            "Save alarm tapped: %02d:%02d, dismissType=%s".format(hour, minute, dismissType.name)
        )
    }
) {
    var selectedHour by rememberSaveable { mutableIntStateOf(7) }
    var selectedMinute by rememberSaveable { mutableIntStateOf(0) }
    var selectedDismissType by rememberSaveable { mutableStateOf(DismissType.NORMAL) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }

    CreateAlarmContent(
        hour = selectedHour,
        minute = selectedMinute,
        dismissType = selectedDismissType,
        onHourChange = { selectedHour = it },
        onMinuteChange = { selectedMinute = it },
        onDismissTypeChange = { selectedDismissType = it },
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
                runCatching { onSaveAlarm(selectedHour, selectedMinute, selectedDismissType) }
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
    hour: Int,
    minute: Int,
    dismissType: DismissType,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onDismissTypeChange: (DismissType) -> Unit,
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
                    text = "Create Alarm",
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
                            Text(text = "Save")
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
                            Text(text = "Save")
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
            onHourChange = {},
            onMinuteChange = {},
            onDismissTypeChange = {},
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
