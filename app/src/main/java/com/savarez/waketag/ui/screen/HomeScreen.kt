package com.savarez.waketag.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
    onEditAlarmClick: (alarmId: Long) -> Unit,
    onDeleteAlarmClick: (alarmId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val fabContent: @Composable () -> Unit = {
        BoxWithConstraints {
            if (maxWidth >= 600.dp) {
                LargeFloatingActionButton(onClick = onCreateAlarmClick) {
                    Text(text = "New Alarm")
                }
            } else {
                FloatingActionButton(onClick = onCreateAlarmClick) {
                    Text(text = "+")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = fabContent
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val horizontalPadding = when {
                maxWidth < 360.dp -> 12.dp
                maxWidth < 600.dp -> 16.dp
                maxWidth < 840.dp -> 24.dp
                else -> 32.dp
            }
            val verticalPadding: Dp = if (maxHeight < 700.dp) 16.dp else 24.dp
            val maxContentWidth: Dp = if (maxWidth >= 840.dp) 760.dp else 640.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .widthIn(max = maxContentWidth),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = alarms, key = { it.id }) { alarm ->
                            AlarmCard(
                                alarm = alarm,
                                onEnabledChange = { enabled ->
                                    onAlarmEnabledChange(alarm.id, enabled)
                                },
                                onEditClick = {
                                    onEditAlarmClick(alarm.id)
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
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    WakeTagTheme {
        HomeScreen(
            alarms = emptyList(),
            onCreateAlarmClick = {},
            onAlarmEnabledChange = { _, _ -> },
            onEditAlarmClick = {},
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
            onEditAlarmClick = {},
            onDeleteAlarmClick = {}
        )
    }
}
