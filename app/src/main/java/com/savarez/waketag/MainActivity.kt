package com.savarez.waketag

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.data.repository.AlarmRepositoryProvider
import com.savarez.waketag.ui.screen.CreateAlarmScreen
import com.savarez.waketag.ui.screen.HomeScreen
import com.savarez.waketag.ui.theme.WakeTagTheme
import kotlinx.coroutines.launch

private enum class WakeTagScreen {
    HOME,
    EDIT_ALARM
}

private const val WAKE_TAG_APP_LOG_TAG = "WakeTagApp"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WakeTagTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WakeTagApp()
                }
            }
        }
    }
}

@Composable
private fun WakeTagApp() {
    val context = LocalContext.current.applicationContext
    val alarmRepository = remember { AlarmRepositoryProvider.get(context) }
    val alarmManager = remember { WakeTagAlarmManager(context) }
    val alarms by alarmRepository.alarms.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(WakeTagScreen.HOME.name) }
    var editingAlarmId by rememberSaveable { mutableStateOf<Long?>(null) }
    var exactAlarmPromptShown by rememberSaveable { mutableStateOf(false) }
    val resolvedScreen = WakeTagScreen.entries.firstOrNull { it.name == currentScreen } ?: WakeTagScreen.HOME

    suspend fun syncAlarmSchedule(alarmId: Long, enabled: Boolean) {
        if (enabled) {
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                val isScheduled = alarmManager.scheduleAlarm(alarm)
                maybePromptForExactAlarmAccess(alarmManager, context, exactAlarmPromptShown) { shown ->
                    exactAlarmPromptShown = shown
                }
                if (!isScheduled) {
                    alarmRepository.setAlarmEnabled(alarmId, false)
                    Log.e(
                        WAKE_TAG_APP_LOG_TAG,
                        "Failed to schedule alarm id=$alarmId; persisted state reverted to disabled"
                    )
                }
            }
        } else {
            alarmManager.cancelAlarm(alarmId)
        }
    }

    LaunchedEffect(Unit) {
        alarmRepository.getEnabledAlarms().forEach { alarm ->
            val isScheduled = alarmManager.scheduleAlarm(alarm)
            if (!isScheduled) {
                Log.e(WAKE_TAG_APP_LOG_TAG, "Failed to restore scheduled alarm id=${alarm.id}")
            }
        }
        maybePromptForExactAlarmAccess(alarmManager, context, exactAlarmPromptShown) { shown ->
            exactAlarmPromptShown = shown
        }
    }

    fun maybePromptForExactAlarmAccess() {
        maybePromptForExactAlarmAccess(alarmManager, context, exactAlarmPromptShown) { shown ->
            exactAlarmPromptShown = shown
        }
    }

    when (resolvedScreen) {
        WakeTagScreen.HOME -> {
            HomeScreen(
                alarms = alarms,
                onCreateAlarmClick = {
                    editingAlarmId = null
                    currentScreen = WakeTagScreen.EDIT_ALARM.name
                },
                onAlarmEnabledChange = { alarmId, enabled ->
                    coroutineScope.launch {
                        val updatedAlarm = alarmRepository.setAlarmEnabled(alarmId, enabled)
                        if (updatedAlarm != null) {
                            syncAlarmSchedule(alarmId = alarmId, enabled = enabled)
                        }
                    }
                },
                onEditAlarmClick = { alarmId ->
                    editingAlarmId = alarmId
                    currentScreen = WakeTagScreen.EDIT_ALARM.name
                },
                onDeleteAlarmClick = { alarmId ->
                    coroutineScope.launch {
                        alarmManager.cancelAlarm(alarmId)
                        alarmRepository.deleteAlarm(alarmId)
                    }
                }
            )
        }

        WakeTagScreen.EDIT_ALARM -> {
            val editingAlarm = alarms.firstOrNull { it.id == editingAlarmId }
            CreateAlarmScreen(
                title = if (editingAlarm == null) "Create Alarm" else "Edit Alarm",
                saveButtonText = if (editingAlarm == null) "Save" else "Update",
                initialHour = editingAlarm?.hour ?: 7,
                initialMinute = editingAlarm?.minute ?: 0,
                initialDismissType = editingAlarm?.dismissType ?: com.savarez.waketag.data.model.DismissType.NORMAL,
                initialEnabled = editingAlarm?.enabled ?: true,
                showEnabledToggle = editingAlarm != null,
                onBackClick = {
                    editingAlarmId = null
                    currentScreen = WakeTagScreen.HOME.name
                },
                onSaveAlarm = { hour, minute, dismissType, enabled ->
                    if (!alarmManager.isValidTime(hour, minute)) {
                        Log.e(
                            WAKE_TAG_APP_LOG_TAG,
                            "Skipping save due to invalid time input: %02d:%02d".format(hour, minute)
                        )
                        return@CreateAlarmScreen
                    }
                    coroutineScope.launch {
                        val alarmId = if (editingAlarmId == null) {
                            val createdAlarm = alarmRepository.addAlarm(hour, minute, dismissType)
                            createdAlarm.id
                        } else {
                            val existingId = editingAlarmId ?: return@launch
                            alarmManager.cancelAlarm(existingId)
                            val updatedAlarm = alarmRepository.updateAlarm(
                                alarmId = existingId,
                                hour = hour,
                                minute = minute,
                                dismissType = dismissType,
                                enabled = enabled
                            )
                            updatedAlarm?.id ?: existingId
                        }

                        syncAlarmSchedule(alarmId = alarmId, enabled = enabled)
                        maybePromptForExactAlarmAccess()
                        editingAlarmId = null
                        currentScreen = WakeTagScreen.HOME.name
                    }
                }
            )
        }
    }
}

private fun maybePromptForExactAlarmAccess(
    alarmManager: WakeTagAlarmManager,
    context: android.content.Context,
    exactAlarmPromptShown: Boolean,
    onPromptShown: (Boolean) -> Unit
) {
    if (exactAlarmPromptShown || alarmManager.canScheduleExactAlarms()) {
        return
    }
    onPromptShown(true)
    Toast.makeText(
        context,
        "Enable exact alarms for reliable WakeTag triggers.",
        Toast.LENGTH_LONG
    ).show()
    alarmManager.createExactAlarmSettingsIntent()?.let { settingsIntent ->
        Log.w(
            WAKE_TAG_APP_LOG_TAG,
            "Exact alarm access unavailable; opening system settings for reliable alarm timing"
        )
        context.startActivity(settingsIntent)
    }
}
