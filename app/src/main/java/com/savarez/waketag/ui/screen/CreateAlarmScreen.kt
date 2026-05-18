package com.savarez.waketag.ui.screen

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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

    CreateAlarmContent(
        hour = selectedHour,
        minute = selectedMinute,
        dismissType = selectedDismissType,
        onHourChange = { selectedHour = it },
        onMinuteChange = { selectedMinute = it },
        onDismissTypeChange = { selectedDismissType = it },
        onBackClick = onBackClick,
        onSaveClick = { onSaveAlarm(selectedHour, selectedMinute, selectedDismissType) },
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
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
