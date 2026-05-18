package com.savarez.waketag.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

private const val ALARM_SOUND_PLAYER_LOG_TAG = "AlarmSoundPlayer"

object AlarmSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context) {
        stop()

        val alarmUri = resolveAlarmUri()
        if (alarmUri == null) {
            Log.w(ALARM_SOUND_PLAYER_LOG_TAG, "No default alarm sound available")
            return
        }

        runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(context.applicationContext, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        }.onSuccess { player ->
            Log.d(ALARM_SOUND_PLAYER_LOG_TAG, "Alarm ringtone started with uri=$alarmUri")
            mediaPlayer = player
        }.onFailure { throwable ->
            Log.e(ALARM_SOUND_PLAYER_LOG_TAG, "Failed to start alarm sound", throwable)
            stop()
        }
    }

    fun stop() {
        mediaPlayer?.runCatching {
            Log.d(ALARM_SOUND_PLAYER_LOG_TAG, "Stopping alarm ringtone playback")
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null
    }

    private fun resolveAlarmUri(): Uri? {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }
}
