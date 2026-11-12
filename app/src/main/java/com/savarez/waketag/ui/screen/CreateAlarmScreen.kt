package com.savarez.waketag.ui.screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.nfc.NfcTagStore
import com.savarez.waketag.ui.component.AlarmTimePicker
import com.savarez.waketag.ui.component.DismissTypeSelector
import com.savarez.waketag.ui.component.WakeTagBackdrop
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.rememberHapticFeedback

private const val CREATE_ALARM_LOG_TAG = "CreateAlarmScreen"
private const val MAX_LABEL_LENGTH = 24
private val LABEL_PRESETS = listOf("Kuliah", "Kerja", "Pribadi")

@Composable
fun CreateAlarmScreen(
    modifier: Modifier = Modifier,
    title: String = "Buat Alarm",
    saveButtonText: String = "Simpan",
    initialHour: Int = 7,
    initialMinute: Int = 0,
    initialDismissType: DismissType = DismissType.NORMAL,
    initialEnabled: Boolean = true,
    initialLabel: String = "",
    initialDaysOfWeek: Int = 127,
    showEnabledToggle: Boolean = false,
    onBackClick: () -> Unit = {},
    onSaveAlarm: (
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        enabled: Boolean,
        label: String,
        daysOfWeek: Int
    ) -> Unit = { hour, minute, dismissType, enabled, label, daysOfWeek ->
        Log.d(
            CREATE_ALARM_LOG_TAG,
            "Save alarm tapped: %02d:%02d, dismissType=%s, enabled=%s, days=%d, label=%s"
                .format(hour, minute, dismissType.name, enabled, daysOfWeek, label.ifBlank { "-" })
        )
    }
) {
    var selectedHour by rememberSaveable(initialHour) { mutableIntStateOf(initialHour) }
    var selectedMinute by rememberSaveable(initialMinute) { mutableIntStateOf(initialMinute) }
    var selectedDismissType by rememberSaveable(initialDismissType) { mutableStateOf(initialDismissType) }
    var selectedEnabled by rememberSaveable(initialEnabled) { mutableStateOf(initialEnabled) }
    var selectedLabel by rememberSaveable(initialLabel) { mutableStateOf(initialLabel) }
    var selectedDaysOfWeek by rememberSaveable(initialDaysOfWeek) { mutableIntStateOf(initialDaysOfWeek) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val nfcTagRegistered = NfcTagStore.getTagId(context) != null

    CreateAlarmContent(
        title = title,
        saveButtonText = saveButtonText,
        hour = selectedHour,
        minute = selectedMinute,
        dismissType = selectedDismissType,
        enabled = selectedEnabled,
        label = selectedLabel,
        daysOfWeek = selectedDaysOfWeek,
        showEnabledToggle = showEnabledToggle,
        nfcTagRegistered = nfcTagRegistered,
        onHourChange = { selectedHour = it },
        onMinuteChange = { selectedMinute = it },
        onDismissTypeChange = { selectedDismissType = it },
        onEnabledChange = { selectedEnabled = it },
        onLabelChange = { selectedLabel = it },
        onDaysChange = { selectedDaysOfWeek = it },
        onBackClick = onBackClick,
        onSaveClick = {
            saveError = null
            if (selectedHour !in 0..23 || selectedMinute !in 0..59) {
                saveError = "Pilih waktu alarm yang valid."
                Log.e(
                    CREATE_ALARM_LOG_TAG,
                    "Invalid alarm time selected: %02d:%02d".format(selectedHour, selectedMinute)
                )
            } else if (selectedDismissType == DismissType.NFC && !nfcTagRegistered) {
                saveError = "Daftarkan tag NFC dulu di halaman utama."
            } else {
                runCatching {
                    onSaveAlarm(
                        selectedHour,
                        selectedMinute,
                        selectedDismissType,
                            selectedEnabled,
                            selectedLabel.trim(),
                            selectedDaysOfWeek
                    )
                }.onFailure { throwable ->
                    saveError = "Gagal menyimpan alarm. Coba lagi."
                    Log.e(CREATE_ALARM_LOG_TAG, "Alarm save failed", throwable)
                }
            }
        },
        errorMessage = saveError,
        modifier = modifier
    )
}

@Composable
fun CreateAlarmContent(
    title: String,
    saveButtonText: String,
    hour: Int,
    minute: Int,
    dismissType: DismissType,
    enabled: Boolean,
    label: String,
    daysOfWeek: Int = 127,
    showEnabledToggle: Boolean,
    nfcTagRegistered: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onDismissTypeChange: (DismissType) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onLabelChange: (String) -> Unit,
    onDaysChange: (Int) -> Unit = {},
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        WakeTagBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val horizontalPadding = when {
                maxWidth < 360.dp -> 16.dp
                maxWidth < 600.dp -> 20.dp
                else -> 28.dp
            }
            val verticalPadding: Dp = if (maxHeight < 700.dp) 16.dp else 24.dp
            val maxContentWidth: Dp = if (maxWidth >= 840.dp) 640.dp else 560.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .widthIn(max = maxContentWidth),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "WAKE TAG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.4.sp
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                AlarmTimePicker(
                    hour = hour,
                    minute = minute,
                    onHourChange = onHourChange,
                    onMinuteChange = onMinuteChange
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Label alarm",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LABEL_PRESETS.forEach { preset ->
                            val selected = label.equals(preset, ignoreCase = true)
                            androidx.compose.material3.FilterChip(
                                selected = selected,
                                onClick = {
                                    haptic()
                                    onLabelChange(if (selected) "" else preset)
                                },
                                label = { Text(text = preset) },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = label,
                        onValueChange = { newValue ->
                            onLabelChange(newValue.take(MAX_LABEL_LENGTH))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Tulis label") },
                        placeholder = { Text(text = "Contoh: Kuliah pagi") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                ActiveDaysSelector(daysOfWeek = daysOfWeek, onDaysChange = onDaysChange)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Metode mematikan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    DismissTypeSelector(
                        selected = dismissType,
                        onSelect = onDismissTypeChange
                    )

                    if (dismissType == DismissType.NFC) {
                        NfcStatusNote(registered = nfcTagRegistered)
                    }
                }

                if (showEnabledToggle) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Alarm aktif",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = { checked -> haptic(); onEnabledChange(checked) }
                            )
                        }
                    }
                }

                errorMessage?.let { message ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { haptic(); onBackClick() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Batal")
                    }
                    Button(
                        onClick = { haptic(); onSaveClick() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = saveButtonText)
                    }
                }
            }
        }
        }
    }
}

private val ACTIVE_DAY_LABELS = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

@Composable
private fun ActiveDaysSelector(daysOfWeek: Int, onDaysChange: (Int) -> Unit) {
    val haptic = rememberHapticFeedback()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Aktif pada", style = MaterialTheme.typography.titleMedium)
            Text(
                text = activeDaysLabel(daysOfWeek),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            ACTIVE_DAY_LABELS.forEachIndexed { index, label ->
                val bit = 1 shl index
                val selected = daysOfWeek and bit != 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                        haptic()
                        val next = if (selected) daysOfWeek and bit.inv() else daysOfWeek or bit
                        if (next != 0) onDaysChange(next)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun activeDaysLabel(days: Int): String = when (days) {
    127 -> "Setiap hari"
    else -> ACTIVE_DAY_LABELS.filterIndexed { index, _ -> days and (1 shl index) != 0 }.joinToString(", ")
}

@Composable
private fun NfcStatusNote(registered: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (registered) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Text(
            text = if (registered) {
                "Tag NFC sudah terdaftar. Alarm ini hanya bisa dimatikan dengan tag tersebut."
            } else {
                "Belum ada tag NFC. Daftarkan dulu di halaman utama sebelum menyimpan."
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (registered) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateAlarmContentPreview() {
    WakeTagTheme {
        CreateAlarmContent(
            title = "Ubah Alarm",
            saveButtonText = "Perbarui",
            hour = 6,
            minute = 45,
            dismissType = DismissType.NFC,
            enabled = true,
            label = "Kuliah",
            showEnabledToggle = true,
            nfcTagRegistered = true,
            onHourChange = {},
            onMinuteChange = {},
            onDismissTypeChange = {},
            onEnabledChange = {},
            onLabelChange = {},
            onBackClick = {},
            onSaveClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateAlarmScreenPreview() {
    WakeTagTheme {
        CreateAlarmScreen()
    }
}
