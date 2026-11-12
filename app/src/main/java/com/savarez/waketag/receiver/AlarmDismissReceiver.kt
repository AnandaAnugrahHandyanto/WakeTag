package com.savarez.waketag.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.service.AlarmPlaybackService

class AlarmDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val appContext = context?.applicationContext ?: return
        if (intent?.action != ACTION_DISMISS_ALARM) return

        val alarmId = intent.getLongExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, -1L)
        Log.d("WakeTag", "Alarm dismiss broadcast received for alarmId=$alarmId")

        AlarmPlaybackService.stopPlaybackNow(appContext)

        // Alarm tetap aktif supaya bisa berbunyi lagi besok.
        // Alarm berikutnya sudah dijadwalkan ulang oleh AlarmReceiver.
        Log.d("WakeTag", "Alarm playback stopped; alarm remains enabled for the next day")
    }

    companion object {
        const val ACTION_DISMISS_ALARM = "com.savarez.waketag.action.DISMISS_ALARM"

        fun dismissNow(context: Context, alarmId: Long) {
            context.sendBroadcast(
                Intent(context, AlarmDismissReceiver::class.java)
                    .setAction(ACTION_DISMISS_ALARM)
                    .setPackage(context.packageName)
                    .putExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, alarmId)
            )
        }
    }
}
