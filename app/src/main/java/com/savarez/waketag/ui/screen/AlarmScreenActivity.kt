package com.savarez.waketag.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.data.repository.AlarmRepositoryProvider
import com.savarez.waketag.service.AlarmPlaybackService
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.displayLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val hour = intent.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, 0)
        val dismissType = intent
            .getStringExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE)
            ?.let { name -> DismissType.entries.firstOrNull { it.name == name } }
            ?: DismissType.NORMAL
        val alarmId = intent.getLongExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, -1L)

        setContent {
            WakeTagTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    color = Color.Black
                ) {
                    AlarmScreenContent(
                        hour = hour,
                        minute = minute,
                        dismissType = dismissType,
                        onDismissClick = {
                            if (alarmId >= 0L) {
                                val appContext = applicationContext
                                CoroutineScope(Dispatchers.IO).launch {
                                    AlarmRepositoryProvider.get(appContext).setAlarmEnabled(alarmId, false)
                                    WakeTagAlarmManager(appContext).cancelAlarm(alarmId)
                                }
                            }
                            AlarmPlaybackService.dismiss(this)
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun createIntent(
            context: Context,
            alarmId: Long,
            hour: Int,
            minute: Int,
            dismissType: String
        ): Intent {
            return Intent(context, AlarmScreenActivity::class.java)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, alarmId)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, hour)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, minute)
                .putExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE, dismissType)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
        }
    }
}

@Composable
fun AlarmScreenContent(
    hour: Int,
    minute: Int,
    dismissType: DismissType,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Alarm",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%02d:%02d".format(hour, minute),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dismissType.displayLabel,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
        Button(
            onClick = onDismissClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Dismiss")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenContentPreview() {
    WakeTagTheme {
        AlarmScreenContent(
            hour = 7,
            minute = 30,
            dismissType = DismissType.NORMAL,
            onDismissClick = {}
        )
    }
}
