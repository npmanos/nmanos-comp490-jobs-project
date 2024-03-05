package edu.bridgew.comp490.proj1.data.entities

import java.io.Serializable
import java.time.LocalDateTime

sealed class Extension(open val extType: String) : Serializable {
    companion object {
        @JvmStatic
        fun getById(extType: String, value: String): Extension {
            return when (extType) {
                "schedule" -> ScheduleType(value)
                "posted_at" -> PostedAt(LocalDateTime.parse(value))
                "salary" -> Salary(value)
                "work_from_home" -> WorkFromHome(value == "true")
                else -> UnknownExtension(extType, value)
            }
        }
    }
}

data class ScheduleType(val type: String) : Extension("schedule")
data class PostedAt(val date: LocalDateTime) : Extension("posted_at")
data class Salary(val salaryRange: String) : Extension("salary")
data class WorkFromHome(val isWFH: Boolean) : Extension("work_from_home")
data class UnknownExtension(override val extType: String, val value: String) : Extension(extType)


