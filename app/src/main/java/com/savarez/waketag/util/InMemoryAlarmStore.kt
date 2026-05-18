package com.savarez.waketag.util

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType

class InMemoryAlarmStore {
    private var nextId by mutableLongStateOf(1L)
    private val _alarms = mutableStateListOf<Alarm>()

    val alarms: List<Alarm> = _alarms

    fun addAlarm(hour: Int, minute: Int, dismissType: DismissType): Alarm {
        val alarm = Alarm(
            id = nextId++,
            hour = hour,
            minute = minute,
            enabled = true,
            dismissType = dismissType
        )
        _alarms.add(alarm)
        return alarm
    }

    fun setEnabled(id: Long, enabled: Boolean): Alarm? {
        val index = _alarms.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedAlarm = _alarms[index].copy(enabled = enabled)
            _alarms[index] = updatedAlarm
            return updatedAlarm
        }
        return null
    }

    fun deleteAlarm(id: Long) {
        _alarms.removeAll { it.id == id }
    }
}
