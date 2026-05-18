package com.savarez.waketag.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.alarm.WakeTagAlarmManager
import com.savarez.waketag.service.AlarmSoundPlayer
import com.savarez.waketag.ui.screen.AlarmScreenActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val appContext = context?.applicationContext ?: return
        val alarmId = intent?.getLongExtra(WakeTagAlarmManager.EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId < 0L) return
        val hour = intent?.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_HOUR, 0) ?: 0
        val minute = intent?.getIntExtra(WakeTagAlarmManager.EXTRA_ALARM_MINUTE, 0) ?: 0
        val dismissType = intent
            ?.getStringExtra(WakeTagAlarmManager.EXTRA_DISMISS_TYPE)
            ?.let { name -> DismissType.entries.firstOrNull { it.name == name } }
            ?: DismissType.NORMAL

        Log.d(
            "WakeTag",
            "Alarm triggered: id=$alarmId at %02d:%02d (%s)".format(hour, minute, dismissType.name)
        )

        WakeTagAlarmManager(appContext).scheduleAlarm(
            Alarm(
                id = alarmId,
                hour = hour,
                minute = minute,
                enabled = true,
                dismissType = dismissType
            )
        )
        AlarmSoundPlayer.play(appContext)
        appContext.startActivity(
            AlarmScreenActivity.createIntent(
                context = appContext,
                hour = hour,
                minute = minute,
                dismissType = dismissType.name
            )
        )
    }
}
