package com.savarez.waketag

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.savarez.waketag.ui.screen.CreateAlarmScreen
import com.savarez.waketag.ui.screen.HomeScreen
import com.savarez.waketag.ui.theme.WakeTagTheme

private const val MAIN_ACTIVITY_LOG_TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WakeTagTheme {
                var showCreateAlarmScreen by rememberSaveable { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showCreateAlarmScreen) {
                        CreateAlarmScreen(
                            modifier = Modifier.padding(innerPadding),
                            onSaveAlarm = { hour, minute, dismissType ->
                                Log.d(
                                    MAIN_ACTIVITY_LOG_TAG,
                                    "Alarm selected: %02d:%02d (%s)".format(hour, minute, dismissType.name)
                                )
                                showCreateAlarmScreen = false
                            }
                        )
                    } else {
                        HomeScreen(
                            onCreateAlarmClick = { showCreateAlarmScreen = true },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
