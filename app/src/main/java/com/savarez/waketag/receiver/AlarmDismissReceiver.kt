package com.savarez.waketag.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.data.repository.AlarmRepositoryProvider
import com.savarez.waketag.service.AlarmPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val appContext = context?.applicationContext ?: return
        if (intent?.action != ACTION_DISMISS_ALARM) return
        val alarmId = intent.getLongExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, -1L)
        Log.d("WakeTag", "Alarm dismiss broadcast received for alarmId=$alarmId")

        AlarmPlaybackService.stopPlaybackNow(appContext)

        if (alarmId < 0L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                AlarmRepositoryProvider.get(appContext).setAlarmEnabled(alarmId, false)
                WakeTagAlarmManager(appContext).cancelAlarm(alarmId)
            }.onFailure { throwable ->
                Log.e("WakeTag", "Failed to persist alarm dismissal for alarmId=$alarmId", throwable)
            }
            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_DISMISS_ALARM = "com.savarez.waketag.action.DISMISS_ALARM"

        fun createDismissPendingIntent(context: Context, alarmId: Long): PendingIntent {
            val dismissIntent = Intent(context, AlarmDismissReceiver::class.java)
                .setAction(ACTION_DISMISS_ALARM)
                .setPackage(context.packageName)
                .putExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, alarmId)
            return PendingIntent.getBroadcast(
                context,
                alarmId.hashCode() + DISMISS_REQUEST_CODE_OFFSET,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun dismissNow(context: Context, alarmId: Long) {
            context.sendBroadcast(
                Intent(context, AlarmDismissReceiver::class.java)
                    .setAction(ACTION_DISMISS_ALARM)
                    .setPackage(context.packageName)
                    .putExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, alarmId)
            )
        }

        private const val DISMISS_REQUEST_CODE_OFFSET = 40_000
    }
}
