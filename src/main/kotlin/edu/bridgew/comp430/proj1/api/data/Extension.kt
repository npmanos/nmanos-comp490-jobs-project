package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel
import java.util.Date

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class Extension(val value: String)

@TypeLabel("schedule_type")
@JsonClass(generateAdapter = true)
class ScheduleType(value: String) : Extension(value)

@TypeLabel("posted_at")
@JsonClass(generateAdapter = true)
class PostedAt(value: String) : Extension(value) //todo: make this a date

@TypeLabel("salary")
@JsonClass(generateAdapter = true)
class Salary(value: String) : Extension(value)

