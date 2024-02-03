package edu.bridgew.comp490.proj1.data.entities

import java.time.LocalDateTime

sealed class Extension(val id: Int)

data class ScheduleType(val type: String) : Extension(1)
data class PostedAt(val date: LocalDateTime) : Extension(2)
data class Salary(val salaryRange: String) : Extension(3)
data class WorkFromHome(val isWFH: Boolean) : Extension(-1)
data class UnknownExtension(val extension: String, val value: String) : Extension(4)
