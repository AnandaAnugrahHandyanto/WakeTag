package com.savarez.waketag.alarm

import android.content.Intent
import com.savarez.waketag.data.model.DismissType

data class AlarmTriggerPayload(
    val alarmId: Long,
    val hour: Int,
    val minute: Int,
    val dismissType: DismissType,
    val label: String,
    val isSnooze: Boolean,
    val scheduledAtMillis: Long
) {
    fun putExtras(intent: Intent): Intent {
        return intent
            .putExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, alarmId)
            .putExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, hour)
            .putExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, minute)
            .putExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE, dismissType.name)
            .putExtra(WakeTagAlarmManager.EXTRA_ALARM_LABEL, label)
            .putExtra(WakeTagAlarmManager.EXTRA_IS_SNOOZE, isSnooze)
            .putExtra(WakeTagAlarmManager.EXTRA_SCHEDULED_AT_MILLIS, scheduledAtMillis)
    }

    companion object {
        fun fromIntent(intent: Intent?): AlarmTriggerPayload? {
            val source = intent ?: return null
            val alarmId = source.getLongExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, -1L)
            if (alarmId < 0L) return null
            val hour = source.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, 0)
            val minute = source.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, 0)
            val dismissType = source
                .getStringExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE)
                ?.let { name -> DismissType.entries.firstOrNull { it.name == name } }
                ?: DismissType.NORMAL
            val label = source.getStringExtra(WakeTagAlarmManager.EXTRA_ALARM_LABEL).orEmpty()
            val isSnooze = source.getBooleanExtra(WakeTagAlarmManager.EXTRA_IS_SNOOZE, false)
            val scheduledAtMillis = source.getLongExtra(WakeTagAlarmManager.EXTRA_SCHEDULED_AT_MILLIS, 0L)
            return AlarmTriggerPayload(
                alarmId = alarmId,
                hour = hour,
                minute = minute,
                dismissType = dismissType,
                label = label,
                isSnooze = isSnooze,
                scheduledAtMillis = scheduledAtMillis
            )
        }
    }
}
