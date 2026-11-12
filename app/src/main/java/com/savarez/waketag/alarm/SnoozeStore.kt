package com.savarez.waketag.alarm

import android.content.Context
import com.savarez.waketag.data.model.DismissType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Menyimpan informasi snooze yang sedang menunggu.
 *
 * AlarmManager hanya tahu kapan alarm berbunyi; halaman utama juga perlu tahu
 * alarm mana yang sedang ditunda supaya bisa menampilkan hitung mundur seperti
 * "5 menit lagi". Data kecil ini disimpan di SharedPreferences.
 */
object SnoozeStore {

    data class SnoozeInfo(
        val alarmId: Long,
        val hour: Int,
        val minute: Int,
        val label: String,
        val dismissType: DismissType,
        val triggerAtMillis: Long
    )

    private const val PREFS_NAME = "waketag_snooze"
    private const val KEY_ALARM_ID = "alarm_id"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"
    private const val KEY_LABEL = "label"
    private const val KEY_DISMISS_TYPE = "dismiss_type"
    private const val KEY_TRIGGER_AT = "trigger_at"

    private val _snooze = MutableStateFlow<SnoozeInfo?>(null)
    val snooze: StateFlow<SnoozeInfo?> = _snooze.asStateFlow()

    /** Membaca ulang data dari penyimpanan, dipakai saat aplikasi kembali dibuka. */
    fun refresh(context: Context) {
        _snooze.value = readActive(context)
    }

    /** Mengembalikan snooze yang belum lewat, sekaligus membersihkan data basi. */
    fun peek(context: Context): SnoozeInfo? = readActive(context)

    fun save(context: Context, info: SnoozeInfo) {
        prefs(context).edit()
            .putLong(KEY_ALARM_ID, info.alarmId)
            .putInt(KEY_HOUR, info.hour)
            .putInt(KEY_MINUTE, info.minute)
            .putString(KEY_LABEL, info.label)
            .putString(KEY_DISMISS_TYPE, info.dismissType.name)
            .putLong(KEY_TRIGGER_AT, info.triggerAtMillis)
            .apply()
        _snooze.value = info
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
        _snooze.value = null
    }

    /** Hapus snooze hanya jika memang milik alarm tertentu. */
    fun clearForAlarm(context: Context, alarmId: Long) {
        if (peek(context)?.alarmId == alarmId) {
            clear(context)
        }
    }

    private fun readActive(context: Context): SnoozeInfo? {
        val prefs = prefs(context)
        val triggerAt = prefs.getLong(KEY_TRIGGER_AT, 0L)
        if (triggerAt <= System.currentTimeMillis()) {
            if (prefs.contains(KEY_TRIGGER_AT)) {
                prefs.edit().clear().apply()
            }
            return null
        }

        val alarmId = prefs.getLong(KEY_ALARM_ID, -1L)
        if (alarmId < 0L) return null

        return SnoozeInfo(
            alarmId = alarmId,
            hour = prefs.getInt(KEY_HOUR, 0),
            minute = prefs.getInt(KEY_MINUTE, 0),
            label = prefs.getString(KEY_LABEL, "").orEmpty(),
            dismissType = prefs
                .getString(KEY_DISMISS_TYPE, null)
                ?.let { name -> DismissType.entries.firstOrNull { it.name == name } }
                ?: DismissType.NORMAL,
            triggerAtMillis = triggerAt
        )
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
