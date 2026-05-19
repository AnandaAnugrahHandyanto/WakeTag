package com.savarez.waketag.data.repository

import android.content.Context
import com.savarez.waketag.data.local.WakeTagDatabase

object AlarmRepositoryProvider {
    @Volatile
    private var instance: AlarmRepository? = null

    fun get(context: Context): AlarmRepository {
        return instance ?: synchronized(this) {
            instance ?: AlarmRepository(
                WakeTagDatabase.getInstance(context.applicationContext).alarmDao()
            ).also { instance = it }
        }
    }
}
