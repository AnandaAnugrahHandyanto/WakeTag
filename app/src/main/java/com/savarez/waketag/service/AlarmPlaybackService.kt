package com.savarez.waketag.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.savarez.waketag.R
import com.savarez.waketag.alarm.AlarmTriggerPayload
import com.savarez.waketag.ui.screen.AlarmScreenActivity
import com.savarez.waketag.util.displayLabel

class AlarmPlaybackService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_DISMISS -> {
                stopAlarmPlayback()
                START_NOT_STICKY
            }

            else -> {
                val payload = AlarmTriggerPayload.fromIntent(intent)
                if (payload == null) {
                    stopSelf()
                    START_NOT_STICKY
                } else {
                    startAlarmPlayback(payload)
                    START_NOT_STICKY
                }
            }
        }
    }

    override fun onDestroy() {
        AlarmSoundPlayer.stop()
        super.onDestroy()
    }

    private fun startAlarmPlayback(payload: AlarmTriggerPayload) {
        ensureChannel()
        startForeground(NOTIFICATION_ID, buildNotification(payload))
        AlarmSoundPlayer.play(applicationContext)
    }

    private fun stopAlarmPlayback() {
        AlarmSoundPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(payload: AlarmTriggerPayload): android.app.Notification {
        val fullscreenIntent = AlarmScreenActivity.createIntent(
            context = this,
            alarmId = payload.alarmId,
            hour = payload.hour,
            minute = payload.minute,
            dismissType = payload.dismissType.name
        )
        val fullscreenPendingIntent = PendingIntent.getActivity(
            this,
            payload.alarmId.hashCode(),
            fullscreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissPendingIntent = PendingIntent.getService(
            this,
            payload.alarmId.hashCode() + DISMISS_REQUEST_CODE_OFFSET,
            Intent(this, AlarmPlaybackService::class.java).setAction(ACTION_DISMISS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("WakeTag Alarm")
            .setContentText("%02d:%02d • %s".format(payload.hour, payload.minute, payload.dismissType.displayLabel))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullscreenPendingIntent, true)
            .setContentIntent(fullscreenPendingIntent)
            .addAction(0, "Dismiss", dismissPendingIntent)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "WakeTag Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm alerts and fullscreen wake notifications"
                setBypassDnd(true)
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
        )
    }

    companion object {
        private const val CHANNEL_ID = "waketag_alarm_channel"
        private const val NOTIFICATION_ID = 1_001
        private const val DISMISS_REQUEST_CODE_OFFSET = 40_000
        private const val ACTION_START = "com.savarez.waketag.action.START_ALARM_PLAYBACK"
        private const val ACTION_DISMISS = "com.savarez.waketag.action.DISMISS_ALARM_PLAYBACK"

        fun start(context: Context, payload: AlarmTriggerPayload) {
            val intent = payload.putExtras(Intent(context, AlarmPlaybackService::class.java))
                .setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun dismiss(context: Context) {
            val intent = Intent(context, AlarmPlaybackService::class.java).setAction(ACTION_DISMISS)
            context.startService(intent)
        }
    }
}
