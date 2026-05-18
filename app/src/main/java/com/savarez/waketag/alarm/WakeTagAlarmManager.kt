package com.savarez.waketag.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.receiver.AlarmReceiver
import java.util.Calendar

class WakeTagAlarmManager(
    private val context: Context
) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm): Long {
        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute)
        val pendingIntent = createPendingIntent(
            alarmId = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            dismissType = alarm.dismissType,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        return triggerTime
    }

    fun cancelAlarm(alarmId: Long) {
        val pendingIntent = createPendingIntent(
            alarmId = alarmId,
            hour = 0,
            minute = 0,
            dismissType = DismissType.NORMAL,
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
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

    private fun createPendingIntent(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        flags: Int
    ): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
            .putExtra(EXTRA_ALARM_ID, alarmId)
            .putExtra(EXTRA_ALARM_HOUR, hour)
            .putExtra(EXTRA_ALARM_MINUTE, minute)
            .putExtra(EXTRA_DISMISS_TYPE, dismissType.name)

        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            flags
        )
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
        const val EXTRA_DISMISS_TYPE = "extra_dismiss_type"
    }
}
