package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class Extension {

    @TypeLabel("schedule_type")
    object ScheduleType : Extension()

    @TypeLabel("posted_at")
    object PostedAt : Extension() //todo: make this a date

    @TypeLabel("salary")
    object Salary : Extension()
}

