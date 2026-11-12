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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.nfc.NfcManager
import com.savarez.waketag.nfc.NfcTagStore
import com.savarez.waketag.receiver.AlarmDismissReceiver
import com.savarez.waketag.ui.theme.WakeTagTheme

class AlarmScreenActivity : ComponentActivity() {

    private lateinit var nfcManager: NfcManager
    private var alarmId: Long = -1L
    private var isNfcAlarm: Boolean = false

    private val nfcEnabled = mutableStateOf(false)
    private val nfcScanning = mutableStateOf(false)
    private val nfcMessage = mutableStateOf<String?>(null)

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

        nfcManager = NfcManager(this)

        val hour = intent.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, 0)
        val dismissType = intent
            .getStringExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE)
            ?.let { name -> DismissType.entries.firstOrNull { it.name == name } }
            ?: DismissType.NORMAL
        alarmId = intent.getLongExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, -1L)
        isNfcAlarm = dismissType == DismissType.NFC
        nfcEnabled.value = nfcManager.isEnabled

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
                        nfcScanning = nfcScanning.value,
                        nfcEnabled = nfcEnabled.value,
                        nfcMessage = nfcMessage.value,
                        onDismissClick = { dismissAlarm() },
                        onOpenNfcSettings = { nfcManager.openNfcSettings() },
                        onForceStopClick = { dismissAlarm() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isNfcAlarm) {
            startNfcScanning()
        }
    }

    override fun onPause() {
        if (isNfcAlarm) {
            nfcManager.disableReaderMode(this)
            nfcScanning.value = false
        }
        super.onPause()
    }

    private fun startNfcScanning() {
        nfcEnabled.value = nfcManager.isEnabled

        when {
            !nfcManager.isSupported -> {
                nfcMessage.value = "HP ini tidak mendukung NFC."
            }

            !nfcManager.isEnabled -> {
                nfcMessage.value = "NFC tidak aktif. Aktifkan NFC untuk mematikan alarm."
            }

            NfcTagStore.getTagId(this) == null -> {
                nfcMessage.value = "Belum ada tag NFC yang terdaftar."
            }

            else -> {
                nfcMessage.value = null
                nfcScanning.value = true
                nfcManager.enableReaderMode(this) { tagId ->
                    runOnUiThread { handleTagScanned(tagId) }
                }
            }
        }
    }

    private fun handleTagScanned(tagId: String) {
        val expectedTagId = NfcTagStore.getTagId(this)
        if (expectedTagId == null) {
            nfcMessage.value = "Belum ada tag NFC yang terdaftar."
            return
        }

        if (tagId.equals(expectedTagId, ignoreCase = true)) {
            nfcScanning.value = false
            nfcManager.disableReaderMode(this)
            dismissAlarm()
        } else {
            nfcMessage.value = "Tag tidak cocok. Gunakan tag yang sudah didaftarkan."
        }
    }

    private fun dismissAlarm() {
        AlarmDismissReceiver.dismissNow(
            context = applicationContext,
            alarmId = alarmId
        )
        finish()
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
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
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
    nfcScanning: Boolean,
    nfcEnabled: Boolean,
    nfcMessage: String?,
    onDismissClick: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onForceStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showForceStopDialog by remember { mutableStateOf(false) }

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

            if (dismissType == DismissType.NFC) {
                Text(
                    text = "Ketuk tag NFC untuk mematikan",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Text(
                    text = nfcMessage
                        ?: if (nfcScanning) "Menunggu tag NFC..." else "Siap memindai tag.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (nfcMessage == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.padding(top = 10.dp)
                )

                if (!nfcEnabled) {
                    OutlinedButton(
                        onClick = onOpenNfcSettings,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(text = "Aktifkan NFC")
                    }
                }
            } else {
                Text(
                    text = "Alarm biasa",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }

        if (dismissType == DismissType.NORMAL) {
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
                Text(text = "Matikan")
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { showForceStopDialog = true }) {
                    Text(text = "Matikan paksa")
                }
                Text(
                    text = "Gunakan hanya jika tag tidak bisa dipakai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showForceStopDialog) {
        AlertDialog(
            onDismissRequest = { showForceStopDialog = false },
            title = { Text(text = "Matikan paksa?") },
            text = {
                Text(
                    text = "Alarm NFC seharusnya dimatikan dengan tag. " +
                        "Matikan paksa akan menghentikan alarm tanpa bukti tag."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForceStopDialog = false
                        onForceStopClick()
                    }
                ) {
                    Text(text = "Ya, matikan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForceStopDialog = false }) {
                    Text(text = "Batal")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenContentPreview() {
    WakeTagTheme {
        AlarmScreenContent(
            hour = 7,
            minute = 30,
            dismissType = DismissType.NFC,
            nfcScanning = true,
            nfcEnabled = true,
            nfcMessage = null,
            onDismissClick = {},
            onOpenNfcSettings = {},
            onForceStopClick = {}
        )
    }
}
