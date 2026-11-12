package com.savarez.waketag.alarm

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.savarez.waketag.MainActivity
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.receiver.AlarmReceiver
import java.util.Calendar

class WakeTagAlarmManager(
    private val context: Context
) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm): Boolean {
        if (!isValidTime(alarm.hour, alarm.minute)) {
            Log.e(TAG, "Invalid alarm time, skipping schedule: ${alarm.hour}:${alarm.minute}")
            return false
        }

        val now = Calendar.getInstance()
        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute)
        val requestCode = createRequestCode(alarm.id)
        val pendingIntent = createSchedulePendingIntent(
            alarmId = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            dismissType = alarm.dismissType,
            label = alarm.label,
            isSnooze = false,
            scheduledAtMillis = triggerTime
        )
        val canScheduleExact = canScheduleExactAlarms()

        Log.d(
            TAG,
            "Scheduling alarm id=${alarm.id}, requestCode=$requestCode, now=${now.timeInMillis}, triggerAt=$triggerTime, exactAllowed=$canScheduleExact"
        )

        return scheduleAt(
            triggerTime = triggerTime,
            pendingIntent = pendingIntent,
            showIntent = createAlarmClockInfoIntent(alarm.id),
            logLabel = "alarm id=${alarm.id}"
        )
    }

    /**
     * Menjadwalkan alarm sekali lagi setelah [delayMillis].
     * Dipakai oleh tombol "Tunda 5 menit" di layar alarm.
     *
     * Request code-nya sengaja dibedakan dari alarm harian supaya
     * snooze tidak menimpa jadwal alarm besok.
     */
    fun scheduleSnooze(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        label: String = "",
        delayMillis: Long = SNOOZE_DELAY_MILLIS
    ): Boolean {
        if (!isValidTime(hour, minute)) {
            Log.e(TAG, "Invalid snooze time: $hour:$minute")
            return false
        }

        val triggerTime = System.currentTimeMillis() + delayMillis
        val requestCode = snoozeRequestCode(alarmId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            createAlarmIntent(
                alarmId = alarmId,
                hour = hour,
                minute = minute,
                dismissType = dismissType,
                label = label,
                isSnooze = true,
                scheduledAtMillis = triggerTime
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(TAG, "Scheduling snooze for alarm id=$alarmId at $triggerTime")

        val scheduled = scheduleAt(
            triggerTime = triggerTime,
            pendingIntent = pendingIntent,
            showIntent = createAlarmClockInfoIntent(alarmId, requestCode),
            logLabel = "snooze of alarm id=$alarmId"
        )

        if (scheduled) {
            SnoozeStore.save(
                context,
                SnoozeStore.SnoozeInfo(
                    alarmId = alarmId,
                    hour = hour,
                    minute = minute,
                    label = label.trim(),
                    dismissType = dismissType,
                    triggerAtMillis = triggerTime
                )
            )
        }
        return scheduled
    }

    fun cancelSnooze(alarmId: Long) {
        SnoozeStore.clearForAlarm(context, alarmId)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            snoozeRequestCode(alarmId),
            createAlarmIntent(
                alarmId = alarmId,
                hour = 0,
                minute = 0,
                dismissType = DismissType.NORMAL,
                label = "",
                isSnooze = true,
                scheduledAtMillis = 0L
            ),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        Log.d(TAG, "Cancelling snooze for alarm id=$alarmId")
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun scheduleAt(
        triggerTime: Long,
        pendingIntent: PendingIntent,
        showIntent: PendingIntent,
        logLabel: String
    ): Boolean {
        return runCatching {
            if (canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmClockInfo(triggerTime, showIntent),
                    pendingIntent
                )
                Log.d(TAG, "Scheduled $logLabel with setAlarmClock at $triggerTime")
            } else {
                Log.w(
                    TAG,
                    "Exact alarm permission unavailable; scheduling inexact $logLabel"
                )
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled $logLabel with setAndAllowWhileIdle at $triggerTime")
            }
            true
        }.getOrElse { throwable ->
            Log.e(TAG, "Failed to schedule $logLabel", throwable)
            false
        }
    }

    fun isValidTime(hour: Int, minute: Int): Boolean {
        return hour in 0..23 && minute in 0..59
    }

    fun canScheduleExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    fun createExactAlarmSettingsIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return null
        }
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun cancelAlarm(alarmId: Long) {
        cancelSnooze(alarmId)

        val pendingIntent = createCancelPendingIntent(
            alarmId = alarmId,
            hour = 0,
            minute = 0,
            dismissType = DismissType.NORMAL,
            label = ""
        )

        pendingIntent?.let {
            Log.d(TAG, "Cancelling alarm id=$alarmId requestCode=${createRequestCode(alarmId)}")
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun calculateNextTriggerTime(hour: Int, minute: Int, now: Calendar = Calendar.getInstance()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    private fun createSchedulePendingIntent(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        label: String,
        isSnooze: Boolean,
        scheduledAtMillis: Long
    ): PendingIntent {
        return createAlarmIntent(
            alarmId = alarmId,
            hour = hour,
            minute = minute,
            dismissType = dismissType,
            label = label,
            isSnooze = isSnooze,
            scheduledAtMillis = scheduledAtMillis
        ).let { intent ->
            PendingIntent.getBroadcast(
                context,
                createRequestCode(alarmId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private fun createCancelPendingIntent(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        label: String
    ): PendingIntent? {
        return PendingIntent.getBroadcast(
            context,
            createRequestCode(alarmId),
            createAlarmIntent(
                alarmId = alarmId,
                hour = hour,
                minute = minute,
                dismissType = dismissType,
                label = label,
                isSnooze = false,
                scheduledAtMillis = 0L
            ),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmClockInfoIntent(
        alarmId: Long,
        requestCode: Int = createRequestCode(alarmId)
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_ALARM_ID, alarmId)

        return PendingIntent.getActivity(
            context,
            requestCode + ALARM_CLOCK_REQUEST_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmIntent(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        label: String,
        isSnooze: Boolean,
        scheduledAtMillis: Long
    ): Intent {
        return Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_TRIGGER_ALARM)
            .setPackage(context.packageName)
            .putExtra(EXTRA_ALARM_ID, alarmId)
            .putExtra(EXTRA_ALARM_HOUR, hour)
            .putExtra(EXTRA_ALARM_MINUTE, minute)
            .putExtra(EXTRA_DISMISS_TYPE, dismissType.name)
            .putExtra(EXTRA_ALARM_LABEL, label)
            .putExtra(EXTRA_IS_SNOOZE, isSnooze)
            .putExtra(EXTRA_SCHEDULED_AT_MILLIS, scheduledAtMillis)
    }

    private fun createRequestCode(alarmId: Long): Int = alarmId.hashCode()

    private fun snoozeRequestCode(alarmId: Long): Int =
        createRequestCode(alarmId) + SNOOZE_REQUEST_OFFSET

    companion object {
        private const val TAG = "WakeTagAlarmManager"
        private const val ALARM_CLOCK_REQUEST_OFFSET = 10_000
        private const val SNOOZE_REQUEST_OFFSET = 20_000
        const val SNOOZE_DELAY_MILLIS = 5 * 60 * 1000L
        const val ACTION_TRIGGER_ALARM = "com.savarez.waketag.action.TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
        const val EXTRA_DISMISS_TYPE = "extra_dismiss_type"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
        const val EXTRA_SCHEDULED_AT_MILLIS = "extra_scheduled_at_millis"
    }
}
