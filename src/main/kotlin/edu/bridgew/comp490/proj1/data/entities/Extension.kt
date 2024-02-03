package edu.bridgew.comp490.proj1.data.entities

import java.time.LocalDateTime

sealed class Extension(val id: Int) {
    companion object {
        @JvmStatic
        fun getById(id: Int, value: String): Extension {
            return when (id) {
                1 -> ScheduleType(value)
                2 -> PostedAt(LocalDateTime.parse(value))
                3 -> Salary(value)
                else -> throw IllegalArgumentException("id value expected to be in (1, 2, 3), got $id")
            }
        }
    }
}

data class ScheduleType(val type: String) : Extension(1)
data class PostedAt(val date: LocalDateTime) : Extension(2)
data class Salary(val salaryRange: String) : Extension(3)
data class WorkFromHome(val isWFH: Boolean) : Extension(-1)
data class UnknownExtension(val extension: String, val value: String) : Extension(-1)
