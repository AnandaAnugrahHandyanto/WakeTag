package com.savarez.waketag

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.savarez.waketag.alarm.SnoozeStore
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.data.repository.AlarmRepositoryProvider
import com.savarez.waketag.ui.screen.CreateAlarmScreen
import com.savarez.waketag.ui.screen.HomeScreen
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.rememberHapticFeedback
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class WakeTagScreen {
    HOME,
    EDIT_ALARM
}

private const val WAKE_TAG_APP_LOG_TAG = "WakeTagApp"

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            WakeTagTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WakeTagApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Snooze bisa dijadwalkan dari layar alarm, jadi data dibaca ulang saat
        // halaman utama muncul kembali.
        SnoozeStore.refresh(this)
    }

    private fun requestNotificationPermissionIfNeeded() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun WakeTagApp() {
    val context = LocalContext.current.applicationContext
    val activity = LocalContext.current as? Activity
    val haptic = rememberHapticFeedback()
    val alarmRepository = remember { AlarmRepositoryProvider.get(context) }
    val alarmManager = remember { WakeTagAlarmManager(context) }
    val alarms by alarmRepository.alarms.collectAsState(initial = emptyList())
    val snooze by SnoozeStore.snooze.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(WakeTagScreen.HOME.name) }
    var editingAlarmId by rememberSaveable { mutableStateOf<Long?>(null) }
    var exactAlarmPromptShown by rememberSaveable { mutableStateOf(false) }
    var lastBackPressAt by rememberSaveable { mutableStateOf(0L) }
    var createHour by rememberSaveable {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    }
    var createMinute by rememberSaveable {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE))
    }
    val resolvedScreen = WakeTagScreen.entries.firstOrNull { it.name == currentScreen } ?: WakeTagScreen.HOME

    BackHandler(enabled = resolvedScreen == WakeTagScreen.EDIT_ALARM) {
        haptic()
        editingAlarmId = null
        currentScreen = WakeTagScreen.HOME.name
    }

    BackHandler(enabled = resolvedScreen == WakeTagScreen.HOME) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressAt <= 2000L) {
            haptic()
            activity?.finish()
        } else {
            lastBackPressAt = now
            haptic()
            Toast.makeText(
                context,
                "Tekan kembali sekali lagi untuk keluar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
        SnoozeStore.refresh(context)
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
                snooze = snooze,
                onCreateAlarmClick = {
                    val now = Calendar.getInstance()
                    createHour = now.get(Calendar.HOUR_OF_DAY)
                    createMinute = now.get(Calendar.MINUTE)
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
                title = if (editingAlarm == null) "Buat Alarm" else "Ubah Alarm",
                saveButtonText = if (editingAlarm == null) "Simpan" else "Perbarui",
                initialHour = editingAlarm?.hour ?: createHour,
                initialMinute = editingAlarm?.minute ?: createMinute,
                initialDismissType = editingAlarm?.dismissType ?: com.savarez.waketag.data.model.DismissType.NORMAL,
                initialEnabled = editingAlarm?.enabled ?: true,
                initialLabel = editingAlarm?.label.orEmpty(),
                showEnabledToggle = editingAlarm != null,
                onBackClick = {
                    editingAlarmId = null
                    currentScreen = WakeTagScreen.HOME.name
                },
                onSaveAlarm = { hour, minute, dismissType, enabled, label ->
                    if (!alarmManager.isValidTime(hour, minute)) {
                        Log.e(
                            WAKE_TAG_APP_LOG_TAG,
                            "Skipping save due to invalid time input: %02d:%02d".format(hour, minute)
                        )
                        return@CreateAlarmScreen
                    }
                    coroutineScope.launch {
                        val alarmId = if (editingAlarmId == null) {
                            val createdAlarm = alarmRepository.addAlarm(hour, minute, dismissType, label)
                            createdAlarm.id
                        } else {
                            val existingId = editingAlarmId ?: return@launch
                            alarmManager.cancelAlarm(existingId)
                            val updatedAlarm = alarmRepository.updateAlarm(
                                alarmId = existingId,
                                hour = hour,
                                minute = minute,
                                dismissType = dismissType,
                                enabled = enabled,
                                label = label
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
        "Izinkan alarm tepat waktu agar WakeTag berbunyi sesuai jadwal.",
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
