package com.savarez.waketag.data.model

data class Alarm(
    val id: Long,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val dismissType: DismissType,
    val label: String = "",
    val daysOfWeek: Int = 127
)
