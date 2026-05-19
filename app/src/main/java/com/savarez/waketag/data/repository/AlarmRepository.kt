package com.savarez.waketag.data.repository

import com.savarez.waketag.data.local.AlarmDao
import com.savarez.waketag.data.local.AlarmEntity
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepository(
    private val alarmDao: AlarmDao
) {
    val alarms: Flow<List<Alarm>> = alarmDao.observeAlarms().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun addAlarm(hour: Int, minute: Int, dismissType: DismissType): Alarm {
        val id = alarmDao.insert(
            AlarmEntity(
                hour = hour,
                minute = minute,
                enabled = true,
                dismissType = dismissType.name
            )
        )
        return requireNotNull(getAlarmById(id))
    }

    suspend fun getAlarmById(alarmId: Long): Alarm? {
        return alarmDao.getById(alarmId)?.toModel()
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms().map { it.toModel() }
    }

    suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean): Alarm? {
        val alarm = alarmDao.getById(alarmId) ?: return null
        val updatedAlarm = alarm.copy(enabled = enabled)
        alarmDao.update(updatedAlarm)
        return updatedAlarm.toModel()
    }

    suspend fun updateAlarm(
        alarmId: Long,
        hour: Int,
        minute: Int,
        dismissType: DismissType,
        enabled: Boolean
    ): Alarm? {
        val currentAlarm = alarmDao.getById(alarmId) ?: return null
        val updatedAlarm = currentAlarm.copy(
            hour = hour,
            minute = minute,
            dismissType = dismissType.name,
            enabled = enabled
        )
        alarmDao.update(updatedAlarm)
        return updatedAlarm.toModel()
    }

    suspend fun deleteAlarm(alarmId: Long) {
        alarmDao.deleteById(alarmId)
    }

    private fun AlarmEntity.toModel(): Alarm {
        return Alarm(
            id = id,
            hour = hour,
            minute = minute,
            enabled = enabled,
            dismissType = DismissType.entries.firstOrNull { it.name == dismissType } ?: DismissType.NORMAL
        )
    }
}
