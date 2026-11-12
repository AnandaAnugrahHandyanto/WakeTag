package com.savarez.waketag.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.nfc.NfcManager
import com.savarez.waketag.nfc.NfcTagStore
import com.savarez.waketag.receiver.AlarmDismissReceiver
import com.savarez.waketag.ui.component.AlarmLabelPill
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.rememberHapticFeedback

class AlarmScreenActivity : ComponentActivity() {

    private lateinit var nfcManager: NfcManager
    private var alarmId: Long = -1L
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0
    private var alarmLabel: String = ""
    private var dismissType: DismissType = DismissType.NORMAL
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

        alarmHour = intent.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, 0)
        alarmMinute = intent.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, 0)
        dismissType = intent
            .getStringExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE)
            ?.let { name -> DismissType.entries.firstOrNull { it.name == name } }
            ?: DismissType.NORMAL
        alarmId = intent.getLongExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, -1L)
        alarmLabel = intent.getStringExtra(WakeTagAlarmManager.EXTRA_ALARM_LABEL).orEmpty()
        isNfcAlarm = dismissType == DismissType.NFC
        nfcEnabled.value = nfcManager.isEnabled

        setContent {
            // Layar alarm selalu gelap, mengikuti palet "midnight".
            WakeTagTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BackHandler {
                        Toast.makeText(
                            this@AlarmScreenActivity,
                            "Matikan alarm terlebih dahulu",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    AlarmScreenContent(
                        hour = alarmHour,
                        minute = alarmMinute,
                        label = alarmLabel,
                        dismissType = dismissType,
                        nfcScanning = nfcScanning.value,
                        nfcEnabled = nfcEnabled.value,
                        nfcMessage = nfcMessage.value,
                        onSnoozeClick = { snoozeAlarm() },
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

    private fun snoozeAlarm() {
        val scheduled = WakeTagAlarmManager(this).scheduleSnooze(
            alarmId = alarmId,
            hour = alarmHour,
            minute = alarmMinute,
            dismissType = dismissType,
            label = alarmLabel
        )
        if (!scheduled) {
            Toast.makeText(
                this,
                "Gagal menunda alarm. Coba lagi.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlarmDismissReceiver.dismissNow(
            context = applicationContext,
            alarmId = alarmId
        )
        finish()
    }

    private fun dismissAlarm() {
        WakeTagAlarmManager(this).cancelSnooze(alarmId)
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
            dismissType: String,
            label: String = ""
        ): Intent {
            return Intent(context, AlarmScreenActivity::class.java)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, alarmId)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, hour)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, minute)
                .putExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE, dismissType)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_LABEL, label)
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
    label: String,
    dismissType: DismissType,
    nfcScanning: Boolean,
    nfcEnabled: Boolean,
    nfcMessage: String?,
    onSnoozeClick: () -> Unit,
    onDismissClick: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onForceStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showForceStopDialog by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val isNfcAlarm = dismissType == DismissType.NFC

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WAKE TAG",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 3.sp
            )
            Text(
                text = "ALARM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 3.sp
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (label.isNotBlank()) {
                AlarmLabelPill(
                    label = label,
                    prominent = true,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            Text(
                text = "%02d:%02d".format(hour, minute),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isNfcAlarm) "Tag NFC dibutuhkan" else "Alarm biasa",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )

            if (isNfcAlarm) {
                NfcScanPanel(
                    scanning = nfcScanning,
                    enabled = nfcEnabled,
                    message = nfcMessage,
                    onOpenNfcSettings = onOpenNfcSettings,
                    modifier = Modifier.padding(top = 28.dp)
                )
            } else {
                Text(
                    text = "Tekan Matikan untuk menghentikan alarm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 28.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!isNfcAlarm) {
                Button(
                    onClick = { haptic(); onDismissClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Matikan",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            OutlinedButton(
                onClick = { haptic(); onSnoozeClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Tunda 5 menit",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (isNfcAlarm) {
                TextButton(onClick = { haptic(); showForceStopDialog = true }) {
                    Text(
                        text = "Matikan paksa",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Gunakan hanya jika tag tidak bisa dipakai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Alarm akan berbunyi lagi dalam 5 menit.",
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
                        haptic()
                        showForceStopDialog = false
                        onForceStopClick()
                    }
                ) {
                    Text(text = "Ya, matikan")
                }
            },
            dismissButton = {
                TextButton(onClick = { haptic(); showForceStopDialog = false }) {
                    Text(text = "Batal")
                }
            }
        )
    }
}

@Composable
private fun NfcScanPanel(
    scanning: Boolean,
    enabled: Boolean,
    message: String?,
    onOpenNfcSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val hasError = message != null

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (hasError) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusDot(
                    active = scanning && !hasError,
                    color = if (hasError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Text(
                    text = when {
                        hasError -> "Perlu perhatian"
                        scanning -> "Menunggu tag NFC"
                        else -> "Menyiapkan pembaca NFC"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (hasError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = message ?: "Tempelkan tag ke bagian belakang HP. " +
                    "Alarm berhenti setelah tag yang terdaftar terbaca.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (!enabled) {
                OutlinedButton(onClick = { haptic(); onOpenNfcSettings() }) {
                    Text(text = "Aktifkan NFC")
                }
            }
        }
    }
}

@Composable
private fun StatusDot(active: Boolean, color: Color) {
    val alpha = if (active) {
        val transition = rememberInfiniteTransition(label = "scan")
        val value by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scanAlpha"
        )
        value
    } else {
        0.5f
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenNfcPreview() {
    WakeTagTheme(darkTheme = true) {
        AlarmScreenContent(
            hour = 7,
            minute = 30,
            label = "Kuliah",
            dismissType = DismissType.NFC,
            nfcScanning = true,
            nfcEnabled = true,
            nfcMessage = null,
            onSnoozeClick = {},
            onDismissClick = {},
            onOpenNfcSettings = {},
            onForceStopClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenNormalPreview() {
    WakeTagTheme(darkTheme = true) {
        AlarmScreenContent(
            hour = 5,
            minute = 45,
            label = "",
            dismissType = DismissType.NORMAL,
            nfcScanning = false,
            nfcEnabled = true,
            nfcMessage = null,
            onSnoozeClick = {},
            onDismissClick = {},
            onOpenNfcSettings = {},
            onForceStopClick = {}
        )
    }
}
