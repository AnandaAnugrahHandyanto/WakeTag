package com.savarez.waketag.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WakeTagDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var instance: WakeTagDatabase? = null

        fun getInstance(context: Context): WakeTagDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WakeTagDatabase::class.java,
                    "waketag.db"
                ).build().also { instance = it }
            }
        }
    }
}
