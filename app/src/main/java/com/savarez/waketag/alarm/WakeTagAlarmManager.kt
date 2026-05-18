package com.savarez.waketag.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import android.content.Context
import android.content.Intent
import android.util.Log
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

        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute)
        val pendingIntent = createSchedulePendingIntent(
            alarmId = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            dismissType = alarm.dismissType
        )

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(
                    TAG,
                    "Exact alarm permission unavailable; scheduling inexact alarm for id=${alarm.id}"
                )
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
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

    fun cancelAlarm(alarmId: Long) {
        val pendingIntent = createCancelPendingIntent(
            alarmId = alarmId,
            hour = 0,
            minute = 0,
            dismissType = DismissType.NORMAL
        )

        pendingIntent?.let {
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
        dismissType: DismissType
    ): PendingIntent {
        return createAlarmIntent(alarmId, hour, minute, dismissType).let { intent ->
            PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
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
            alarmId.hashCode(),
            createAlarmIntent(alarmId, hour, minute, dismissType),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAlarmIntent(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType
    ): Intent {
        return Intent(context, AlarmReceiver::class.java)
            .putExtra(EXTRA_ALARM_ID, alarmId)
            .putExtra(EXTRA_ALARM_HOUR, hour)
            .putExtra(EXTRA_ALARM_MINUTE, minute)
            .putExtra(EXTRA_DISMISS_TYPE, dismissType.name)
    }

    companion object {
        private const val TAG = "WakeTagAlarmManager"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
        const val EXTRA_DISMISS_TYPE = "extra_dismiss_type"
    }
}
