package com.savarez.waketag.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

private const val ALARM_SOUND_PLAYER_LOG_TAG = "AlarmSoundPlayer"

object AlarmSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    fun play(context: Context) {
        stop()
        startVibration(context.applicationContext)

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
                setOnPreparedListener { preparedPlayer ->
                    preparedPlayer.start()
                    Log.d(ALARM_SOUND_PLAYER_LOG_TAG, "Alarm ringtone started with uri=$alarmUri")
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(
                        ALARM_SOUND_PLAYER_LOG_TAG,
                        "Alarm ringtone playback error what=$what extra=$extra"
                    )
                    stop()
                    true
                }
                prepareAsync()
            }
        }.onSuccess { player ->
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
        vibrator?.runCatching {
            Log.d(ALARM_SOUND_PLAYER_LOG_TAG, "Stopping alarm vibration")
            cancel()
        }
        vibrator = null
    }

    private fun resolveAlarmUri(): Uri? {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }

    private fun startVibration(context: Context) {
        val resolvedVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (resolvedVibrator?.hasVibrator() != true) {
            Log.w(ALARM_SOUND_PLAYER_LOG_TAG, "Vibrator unavailable on this device")
            return
        }
        vibrator = resolvedVibrator
        runCatching {
            val pattern = longArrayOf(0L, 500L, 500L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                resolvedVibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, 0),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                resolvedVibrator.vibrate(pattern, 0)
            }
            Log.d(ALARM_SOUND_PLAYER_LOG_TAG, "Alarm vibration started")
        }.onFailure { throwable ->
            Log.e(ALARM_SOUND_PLAYER_LOG_TAG, "Failed to start vibration", throwable)
            vibrator = null
        }
    }
}
