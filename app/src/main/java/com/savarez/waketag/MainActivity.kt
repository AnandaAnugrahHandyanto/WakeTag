package com.savarez.waketag

import android.os.Bundle
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

    when (WakeTagScreen.valueOf(currentScreen)) {
        WakeTagScreen.HOME -> {
            HomeScreen(
                alarms = alarmStore.alarms,
                onCreateAlarmClick = { currentScreen = WakeTagScreen.CREATE_ALARM.name },
                onAlarmEnabledChange = { alarmId, enabled ->
                    val updatedAlarm = alarmStore.setEnabled(alarmId, enabled)
                    if (enabled) {
                        updatedAlarm?.let(alarmManager::scheduleAlarm)
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
                    val alarm = alarmStore.addAlarm(hour, minute, dismissType)
                    alarmManager.scheduleAlarm(alarm)
                    currentScreen = WakeTagScreen.HOME.name
                }
            )
        }
    }
}
