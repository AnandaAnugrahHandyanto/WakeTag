package com.savarez.waketag.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val dismissType: String,
    @ColumnInfo(defaultValue = "''")
    val label: String = "",
    @ColumnInfo(defaultValue = "127")
    val daysOfWeek: Int = 127
)
