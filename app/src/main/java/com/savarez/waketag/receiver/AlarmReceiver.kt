package com.savarez.waketag.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.alarm.AlarmTriggerPayload
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.service.AlarmPlaybackService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val appContext = context?.applicationContext ?: return
        val triggeredAtMillis = System.currentTimeMillis()
        Log.d(
            "WakeTag",
            "AlarmReceiver invoked with action=${intent?.action}, receivedAt=$triggeredAtMillis"
        )
        val payload = AlarmTriggerPayload.fromIntent(intent) ?: return
        val scheduledAtMillis = payload.scheduledAtMillis
        val triggerDelayMillis = if (scheduledAtMillis > 0L) {
            triggeredAtMillis - scheduledAtMillis
        } else {
            -1L
        }

        Log.d(
            "WakeTag",
            "Alarm triggered: id=${payload.alarmId} at %02d:%02d (%s), scheduledAt=$scheduledAtMillis, triggeredAt=$triggeredAtMillis, delayMs=$triggerDelayMillis"
                .format(payload.hour, payload.minute, payload.dismissType.name)
        )
        Log.d("WakeTag", "Starting alarm playback service for id=${payload.alarmId}")
        runCatching {
            AlarmPlaybackService.start(appContext, payload)
        }.onFailure { throwable ->
            Log.e("WakeTag", "Failed to start alarm playback service for id=${payload.alarmId}", throwable)
        }
        val scheduled = WakeTagAlarmManager(appContext).scheduleAlarm(
            Alarm(
                id = payload.alarmId,
                hour = payload.hour,
                minute = payload.minute,
                enabled = true,
                dismissType = payload.dismissType
            )
        )
        if (!scheduled) {
            Log.e("WakeTag", "Failed to re-schedule repeating alarm id=${payload.alarmId}")
        }
    }
}
