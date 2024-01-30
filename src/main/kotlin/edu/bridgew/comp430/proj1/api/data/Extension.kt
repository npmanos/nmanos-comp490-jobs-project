package edu.bridgew.comp430.proj1.api.data

import java.time.LocalDateTime

sealed class Extension

data class ScheduleType(val type: String) : Extension()
data class PostedAt(val date: LocalDateTime) : Extension()
data class Salary(val salaryRange: String) : Extension()
data class WorkFromHome(val isWFH: Boolean) : Extension()
data class UnknownExtension(val extension: String, val value: String) : Extension()
