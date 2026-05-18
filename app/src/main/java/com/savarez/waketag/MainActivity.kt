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
import androidx.compose.ui.Modifier
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
    val alarmStore = remember { InMemoryAlarmStore() }
    var currentScreen by rememberSaveable { mutableStateOf(WakeTagScreen.HOME.name) }

    when (WakeTagScreen.valueOf(currentScreen)) {
        WakeTagScreen.HOME -> {
            HomeScreen(
                alarms = alarmStore.alarms,
                onCreateAlarmClick = { currentScreen = WakeTagScreen.CREATE_ALARM.name },
                onAlarmEnabledChange = alarmStore::setEnabled,
                onDeleteAlarmClick = alarmStore::deleteAlarm
            )
        }

        WakeTagScreen.CREATE_ALARM -> {
            CreateAlarmScreen(
                onBackClick = { currentScreen = WakeTagScreen.HOME.name },
                onSaveAlarm = { hour, minute, dismissType ->
                    alarmStore.addAlarm(hour, minute, dismissType)
                    currentScreen = WakeTagScreen.HOME.name
                }
            )
        }
    }
}
