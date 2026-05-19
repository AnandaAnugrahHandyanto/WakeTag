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
            scheduledAtMillis = triggerTime
        )
        val canScheduleExact = canScheduleExactAlarms()

        Log.d(
            TAG,
            "Scheduling alarm id=${alarm.id}, requestCode=$requestCode, now=${now.timeInMillis}, triggerAt=$triggerTime, exactAllowed=$canScheduleExact"
        )

        return runCatching {
            if (canScheduleExact) {
                alarmManager.setAlarmClock(
                    AlarmClockInfo(triggerTime, createAlarmClockInfoIntent(alarm.id)),
                    pendingIntent
                )
                Log.d(
                    TAG,
                    "Alarm scheduled with setAlarmClock for id=${alarm.id} at $triggerTime"
                )
            } else {
                Log.w(
                    TAG,
                    "Exact alarm permission unavailable; scheduling inexact alarm for id=${alarm.id}"
                )
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(
                    TAG,
                    "Alarm scheduled with setAndAllowWhileIdle for id=${alarm.id} at $triggerTime"
                )
            }
            true
        }.getOrElse { throwable ->
            Log.e(TAG, "Failed to schedule alarm id=${alarm.id}", throwable)
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
        val pendingIntent = createCancelPendingIntent(
            alarmId = alarmId,
            hour = 0,
            minute = 0,
            dismissType = DismissType.NORMAL
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
        scheduledAtMillis: Long
    ): PendingIntent {
        return createAlarmIntent(
            alarmId = alarmId,
            hour = hour,
            minute = minute,
            dismissType = dismissType,
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
        dismissType: DismissType
    ): PendingIntent? {
        return PendingIntent.getBroadcast(
            context,
            createRequestCode(alarmId),
            createAlarmIntent(
                alarmId = alarmId,
                hour = hour,
                minute = minute,
                dismissType = dismissType,
                scheduledAtMillis = 0L
            ),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmClockInfoIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_ALARM_ID, alarmId)

        return PendingIntent.getActivity(
            context,
            createRequestCode(alarmId) + ALARM_CLOCK_REQUEST_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmIntent(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        scheduledAtMillis: Long
    ): Intent {
        return Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_TRIGGER_ALARM)
            .setPackage(context.packageName)
            .putExtra(EXTRA_ALARM_ID, alarmId)
            .putExtra(EXTRA_ALARM_HOUR, hour)
            .putExtra(EXTRA_ALARM_MINUTE, minute)
            .putExtra(EXTRA_DISMISS_TYPE, dismissType.name)
            .putExtra(EXTRA_SCHEDULED_AT_MILLIS, scheduledAtMillis)
    }

    private fun createRequestCode(alarmId: Long): Int = alarmId.hashCode()

    companion object {
        private const val TAG = "WakeTagAlarmManager"
        private const val ALARM_CLOCK_REQUEST_OFFSET = 10_000
        const val ACTION_TRIGGER_ALARM = "com.savarez.waketag.action.TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
        const val EXTRA_DISMISS_TYPE = "extra_dismiss_type"
        const val EXTRA_SCHEDULED_AT_MILLIS = "extra_scheduled_at_millis"
    }
}
