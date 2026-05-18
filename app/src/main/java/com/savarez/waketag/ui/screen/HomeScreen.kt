package com.savarez.waketag.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.ui.component.AlarmCard
import com.savarez.waketag.ui.theme.WakeTagTheme

@Composable
fun HomeScreen(
    alarms: List<Alarm>,
    onCreateAlarmClick: () -> Unit,
    onAlarmEnabledChange: (alarmId: Long, enabled: Boolean) -> Unit,
    onDeleteAlarmClick: (alarmId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateAlarmClick) {
                Text(text = "+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "WakeTag",
                style = MaterialTheme.typography.headlineMedium
            )

            if (alarms.isEmpty()) {
                Text(
                    text = "No alarms yet. Tap + to create your first alarm.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onEnabledChange = { enabled ->
                                onAlarmEnabledChange(alarm.id, enabled)
                            },
                            onDeleteClick = {
                                onDeleteAlarmClick(alarm.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    WakeTagTheme {
        HomeScreen(
            alarms = emptyList(),
            onCreateAlarmClick = {},
            onAlarmEnabledChange = { _, _ -> },
            onDeleteAlarmClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenListPreview() {
    WakeTagTheme {
        HomeScreen(
            alarms = listOf(
                Alarm(id = 1, hour = 6, minute = 30, enabled = true, dismissType = DismissType.NORMAL),
                Alarm(id = 2, hour = 7, minute = 0, enabled = false, dismissType = DismissType.NFC)
            ),
            onCreateAlarmClick = {},
            onAlarmEnabledChange = { _, _ -> },
            onDeleteAlarmClick = {}
        )
    }
}
