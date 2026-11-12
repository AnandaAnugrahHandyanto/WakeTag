package com.savarez.waketag.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.ui.component.AlarmCard
import com.savarez.waketag.ui.component.NfcTagCard
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.displayLabel
import com.savarez.waketag.util.rememberHapticFeedback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    alarms: List<Alarm>,
    onCreateAlarmClick: () -> Unit,
    onAlarmEnabledChange: (alarmId: Long, enabled: Boolean) -> Unit,
    onEditAlarmClick: (alarmId: Long) -> Unit,
    onDeleteAlarmClick: (alarmId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val nextAlarm = remember(alarms) { findNextAlarm(alarms) }
    val todayLabel = remember {
        SimpleDateFormat("EEE, d MMM", Locale("id", "ID")).format(Calendar.getInstance().time)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { haptic(); onCreateAlarmClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = "Alarm baru",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val horizontalPadding = when {
                maxWidth < 360.dp -> 16.dp
                maxWidth < 600.dp -> 20.dp
                else -> 28.dp
            }
            val verticalPadding = if (maxHeight < 700.dp) 16.dp else 24.dp
            val contentWidth = if (maxWidth > 680.dp) 680.dp else maxWidth

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(contentWidth)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = verticalPadding,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HomeHeader(todayLabel = todayLabel)
                }

                if (nextAlarm != null) {
                    item {
                        NextAlarmPanel(nextAlarm = nextAlarm)
                    }
                }

                item {
                    NfcTagCard()
                }

                item {
                    SectionHeader(alarms = alarms)
                }

                if (alarms.isEmpty()) {
                    item {
                        EmptyAlarmState(onCreateAlarmClick = onCreateAlarmClick)
                    }
                } else {
                    items(items = alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onEnabledChange = { enabled ->
                                onAlarmEnabledChange(alarm.id, enabled)
                            },
                            onEditClick = { onEditAlarmClick(alarm.id) },
                            onDeleteClick = { onDeleteAlarmClick(alarm.id) }
                        )
                    }
                }

                item {
                    Text(
                        text = "Alarm berbunyi setiap hari selama statusnya aktif.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(todayLabel: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "WakeTag",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = todayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
        Text(
            text = "Alarm yang hanya bisa dimatikan dengan tag fisik.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NextAlarmPanel(nextAlarm: NextAlarmInfo) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ALARM BERIKUTNYA",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp
                )
                Text(
                    text = nextAlarm.alarm.dismissType.displayLabel,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "%02d:%02d".format(nextAlarm.alarm.hour, nextAlarm.alarm.minute),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = nextAlarm.dayLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Text(
                text = "Berbunyi " + nextAlarm.dayLabel.lowercase() + ".",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SectionHeader(alarms: List<Alarm>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Alarm",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (alarms.isNotEmpty()) {
            val activeCount = alarms.count { it.enabled }
            Text(
                text = "$activeCount dari ${alarms.size} aktif",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyAlarmState(onCreateAlarmClick: () -> Unit) {
    val haptic = rememberHapticFeedback()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Belum ada alarm",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Buat alarm pertama, lalu pilih cara mematikannya.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = { haptic(); onCreateAlarmClick() }) {
                Text(text = "Buat alarm")
            }
        }
    }
}

private data class NextAlarmInfo(
    val alarm: Alarm,
    val dayLabel: String
)

private fun findNextAlarm(alarms: List<Alarm>): NextAlarmInfo? {
    val enabledAlarms = alarms.filter { it.enabled }
    if (enabledAlarms.isEmpty()) return null

    val now = Calendar.getInstance()
    val next = enabledAlarms
        .map { alarm -> alarm to nextTrigger(alarm, now) }
        .minByOrNull { (_, trigger) -> trigger.timeInMillis }
        ?: return null

    return NextAlarmInfo(
        alarm = next.first,
        dayLabel = dayLabel(next.second, now)
    )
}

private fun nextTrigger(alarm: Alarm, now: Calendar): Calendar {
    return (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}

private fun dayLabel(trigger: Calendar, now: Calendar): String {
    val startOfToday = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfTrigger = (trigger.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val days = ((startOfTrigger.timeInMillis - startOfToday.timeInMillis) / DAY_MILLIS).toInt()

    return when (days) {
        0 -> "Hari ini"
        1 -> "Besok"
        else -> SimpleDateFormat("EEEE", Locale("id", "ID")).format(trigger.time)
    }
}

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

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
                Alarm(id = 1, hour = 6, minute = 30, enabled = true, dismissType = DismissType.NFC),
                Alarm(id = 2, hour = 7, minute = 0, enabled = false, dismissType = DismissType.NORMAL)
            ),
            onCreateAlarmClick = {},
            onAlarmEnabledChange = { _, _ -> },
            onEditAlarmClick = {},
            onDeleteAlarmClick = {}
        )
    }
}
