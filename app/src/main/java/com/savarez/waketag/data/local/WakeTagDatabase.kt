package com.savarez.waketag.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AlarmEntity::class],
    version = 2,
    exportSchema = false
)
abstract class WakeTagDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var instance: WakeTagDatabase? = null

        /**
         * Versi 2 menambahkan kolom label, misalnya "Kuliah" atau "Kerja".
         * Data alarm lama tetap aman karena label diberi nilai kosong.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN label TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context): WakeTagDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WakeTagDatabase::class.java,
                    "waketag.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
