package com.savarez.waketag

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.ui.screen.CreateAlarmScreen
import com.savarez.waketag.ui.screen.HomeScreen
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.InMemoryAlarmStore

private enum class WakeTagScreen {
    HOME,
    CREATE_ALARM
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
    val context = LocalContext.current
    val alarmStore = remember { InMemoryAlarmStore() }
    val alarmManager = remember { WakeTagAlarmManager(context.applicationContext) }
    var currentScreen by rememberSaveable { mutableStateOf(WakeTagScreen.HOME.name) }
    val resolvedScreen = WakeTagScreen.entries.firstOrNull { it.name == currentScreen } ?: WakeTagScreen.HOME

    when (resolvedScreen) {
        WakeTagScreen.HOME -> {
            HomeScreen(
                alarms = alarmStore.alarms,
                onCreateAlarmClick = { currentScreen = WakeTagScreen.CREATE_ALARM.name },
                onAlarmEnabledChange = { alarmId, enabled ->
                    val updatedAlarm = alarmStore.setEnabled(alarmId, enabled)
                    if (enabled) {
                        updatedAlarm?.let { alarm ->
                            val isScheduled = alarmManager.scheduleAlarm(alarm)
                            if (!isScheduled) {
                                alarmStore.setEnabled(alarmId, false)
                                Log.e(
                                    WAKE_TAG_APP_LOG_TAG,
                                    "Failed to enable alarm id=$alarmId; reverting to disabled state"
                                )
                            }
                        }
                    } else {
                        alarmManager.cancelAlarm(alarmId)
                    }
                },
                onDeleteAlarmClick = { alarmId ->
                    alarmManager.cancelAlarm(alarmId)
                    alarmStore.deleteAlarm(alarmId)
                }
            )
        }

        WakeTagScreen.CREATE_ALARM -> {
            CreateAlarmScreen(
                onBackClick = { currentScreen = WakeTagScreen.HOME.name },
                onSaveAlarm = { hour, minute, dismissType ->
                    if (!alarmManager.isValidTime(hour, minute)) {
                        Log.e(
                            WAKE_TAG_APP_LOG_TAG,
                            "Skipping save due to invalid time input: %02d:%02d".format(hour, minute)
                        )
                        return@CreateAlarmScreen
                    }

                    val alarm = alarmStore.addAlarm(hour, minute, dismissType)
                    val isScheduled = alarmManager.scheduleAlarm(alarm)
                    if (!isScheduled) {
                        alarmStore.setEnabled(alarm.id, false)
                        Log.e(
                            WAKE_TAG_APP_LOG_TAG,
                            "Alarm saved but scheduling failed for id=${alarm.id}; marked disabled"
                        )
                    }
                    currentScreen = WakeTagScreen.HOME.name
                }
            )
        }
    }
}
