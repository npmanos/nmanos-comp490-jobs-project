package edu.bridgew.comp430.proj1.api.data

sealed class Extension

data class ScheduleType(val type: String) : Extension()
data class PostedAt(val date: String) : Extension() // TODO: make this a date
data class Salary(val salaryRange: String) : Extension()
data class UnknownExtension(val extension: String, val value: String) : Extension()
