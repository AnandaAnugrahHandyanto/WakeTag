package com.savarez.waketag.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.alarm.SnoozeStore
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.ui.component.AlarmAura
import com.savarez.waketag.ui.component.AlarmCard
import com.savarez.waketag.ui.component.AlarmLabelPill
import com.savarez.waketag.ui.component.NfcTagCard
import com.savarez.waketag.ui.component.SunriseDial
import com.savarez.waketag.ui.component.WakeTagBackdrop
import com.savarez.waketag.ui.component.alarmLabelAccent
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.displayLabel
import com.savarez.waketag.util.rememberHapticFeedback
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    alarms: List<Alarm>,
    snooze: SnoozeStore.SnoozeInfo? = null,
    onCreateAlarmClick: () -> Unit,
    onAlarmEnabledChange: (alarmId: Long, enabled: Boolean) -> Unit,
    onEditAlarmClick: (alarmId: Long) -> Unit,
    onDeleteAlarmClick: (alarmId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val todayLabel = remember {
        SimpleDateFormat("EEE, d MMM", Locale("id", "ID")).format(Calendar.getInstance().time)
    }
    val greeting = remember {
        greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    }
    val hasNextAlarmCandidate = remember(alarms, snooze) {
        alarms.any { it.enabled } || snooze != null
    }

    val listState = rememberLazyListState()
    val showCreateButton = alarms.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AnimatedVisibility(
                visible = showCreateButton && listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0,
                enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                exit = fadeOut(tween(220)) + scaleOut(tween(220))
            ) {
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

            WakeTagBackdrop()

            LazyColumn(
                state = listState,
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
                    HomeHeader(todayLabel = todayLabel, greeting = greeting)
                }

                if (hasNextAlarmCandidate) {
                    item {
                        NextAlarmSection(alarms = alarms, snooze = snooze)
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
            }
        }
    }
}

@Composable
private fun HomeHeader(todayLabel: String, greeting: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Text(
                    text = greeting.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            }
            Text(
                text = "WakeTag",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = todayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun NextAlarmSection(
    alarms: List<Alarm>,
    snooze: SnoozeStore.SnoozeInfo?
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val nextAlarm = remember(alarms, snooze, nowMillis) {
        findNextAlarm(alarms, snooze, nowMillis)
    }

    // Hanya bagian ini yang diperbarui tiap detik, bukan seluruh halaman.
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    if (nextAlarm != null) {
        NextAlarmPanel(nextAlarm = nextAlarm)
    }
}

@Composable
private fun NextAlarmPanel(nextAlarm: NextAlarmInfo) {
    val shape = MaterialTheme.shapes.extraLarge
    val accent = if (nextAlarm.label.isNotBlank()) {
        alarmLabelAccent(nextAlarm.label)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradient)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
                shape = shape
            )
    ) {
        AlarmAura(
            color = accent,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 74.dp, y = (-84).dp)
                .size(236.dp),
            ringCount = 2
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accent, CircleShape)
                    )
                    Text(
                        text = "ALARM BERIKUTNYA",
                        style = MaterialTheme.typography.labelSmall,
                        color = onContainer,
                        letterSpacing = 2.sp
                    )
                }
                Text(
                    text = if (nextAlarm.isSnooze) "TUNDA" else "HARIAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (nextAlarm.isSnooze) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        onContainer.copy(alpha = 0.65f)
                    },
                    letterSpacing = 1.4.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "%02d:%02d".format(nextAlarm.hour, nextAlarm.minute),
                        style = MaterialTheme.typography.displayMedium,
                        color = onContainer
                    )
                    if (nextAlarm.label.isNotBlank()) {
                        AlarmLabelPill(
                            label = nextAlarm.label,
                            prominent = true
                        )
                    }
                }

                SunriseDial(
                    progress = nextAlarm.progress,
                    modifier = Modifier.size(104.dp),
                    accent = accent,
                    contentColor = onContainer,
                    centerCaption = "Menuju",
                    centerLabel = nextAlarm.dayLabel
                )
            }

            HorizontalDivider(
                color = onContainer.copy(alpha = 0.12f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nextAlarm.relativeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = onContainer
                )
                Text(
                    text = nextAlarm.dismissType.displayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainer.copy(alpha = 0.65f)
                )
            }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SunriseDial(
                progress = 0.28f,
                modifier = Modifier.size(70.dp),
                accent = MaterialTheme.colorScheme.primary,
                showTicks = false,
                trackWidth = 4.dp
            )
            Text(
                text = "Belum ada alarm",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(onClick = { haptic(); onCreateAlarmClick() }) {
                Text(text = "Buat alarm")
            }
        }
    }
}

private data class NextAlarmInfo(
    val hour: Int,
    val minute: Int,
    val label: String,
    val dismissType: DismissType,
    val triggerAtMillis: Long,
    val isSnooze: Boolean,
    val dayLabel: String,
    val relativeLabel: String,
    val progress: Float
)

private fun findNextAlarm(
    alarms: List<Alarm>,
    snooze: SnoozeStore.SnoozeInfo?,
    nowMillis: Long
): NextAlarmInfo? {
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }

    val dailyCandidates = alarms
        .filter { it.enabled }
        .map { alarm ->
            val triggerAtMillis = nextTrigger(alarm, now).timeInMillis
            NextAlarmInfo(
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                dismissType = alarm.dismissType,
                triggerAtMillis = triggerAtMillis,
                isSnooze = false,
                dayLabel = dayLabel(triggerAtMillis, now),
                relativeLabel = relativeLabel(triggerAtMillis, nowMillis),
                progress = progressFor(triggerAtMillis, nowMillis)
            )
        }

    val snoozeCandidate = snooze
        ?.takeIf { it.triggerAtMillis > nowMillis }
        ?.let { info ->
            NextAlarmInfo(
                hour = info.hour,
                minute = info.minute,
                label = info.label,
                dismissType = info.dismissType,
                triggerAtMillis = info.triggerAtMillis,
                isSnooze = true,
                dayLabel = dayLabel(info.triggerAtMillis, now),
                relativeLabel = relativeLabel(info.triggerAtMillis, nowMillis),
                progress = progressFor(info.triggerAtMillis, nowMillis)
            )
        }

    return (dailyCandidates + listOfNotNull(snoozeCandidate))
        .minByOrNull { it.triggerAtMillis }
}

private fun nextTrigger(alarm: Alarm, now: Calendar): Calendar {
    return (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        while (timeInMillis <= now.timeInMillis || !isDayEnabled(get(Calendar.DAY_OF_WEEK), alarm.daysOfWeek)) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}

private fun isDayEnabled(calendarDay: Int, daysOfWeek: Int): Boolean {
    val mondayBasedIndex = (calendarDay + 5) % 7
    return daysOfWeek and (1 shl mondayBasedIndex) != 0
}

private fun dayLabel(triggerAtMillis: Long, now: Calendar): String {
    val trigger = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
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

internal fun progressFor(triggerAtMillis: Long, nowMillis: Long): Float {
    val deltaMillis = (triggerAtMillis - nowMillis).coerceAtLeast(0L)
    val remainingFraction = deltaMillis.toFloat() / DAY_MILLIS.toFloat()
    return (1f - remainingFraction).coerceIn(0.02f, 1f)
}

internal fun relativeLabel(triggerAtMillis: Long, nowMillis: Long): String {
    val deltaMillis = triggerAtMillis - nowMillis
    if (deltaMillis <= 0L) return "Sekarang"

    val totalSeconds = (deltaMillis + 999L) / 1_000L
    if (totalSeconds < 60L) {
        return "$totalSeconds detik lagi"
    }

    val totalMinutes = (deltaMillis + 59_999L) / 60_000L
    if (totalMinutes < 60L) {
        return "$totalMinutes menit lagi"
    }

    val totalHours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    if (totalHours < 24L) {
        return buildString {
            append("$totalHours jam")
            if (minutes > 0L) append(" $minutes menit")
            append(" lagi")
        }
    }

    val days = totalHours / 24L
    val hours = totalHours % 24L
    return buildString {
        append("$days hari")
        if (hours > 0L) append(" $hours jam")
        append(" lagi")
    }
}

private fun greetingForHour(hour: Int): String {
    return when (hour) {
        in 4..10 -> "Selamat pagi"
        in 11..14 -> "Selamat siang"
        in 15..17 -> "Selamat sore"
        else -> "Selamat malam"
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
                Alarm(
                    id = 1,
                    hour = 6,
                    minute = 30,
                    enabled = true,
                    dismissType = DismissType.NFC,
                    label = "Kuliah"
                ),
                Alarm(
                    id = 2,
                    hour = 7,
                    minute = 0,
                    enabled = false,
                    dismissType = DismissType.NORMAL,
                    label = "Kerja"
                )
            ),
            onCreateAlarmClick = {},
            onAlarmEnabledChange = { _, _ -> },
            onEditAlarmClick = {},
            onDeleteAlarmClick = {}
        )
    }
}
