package edu.bridgew.comp490.proj1.data.entities.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeAdapter {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz")

    @FromJson
    fun fromJson(dateTimeStr: String): ZonedDateTime {
        return ZonedDateTime.parse(dateTimeStr, formatter)
    }

    @ToJson
    fun toJson(dateTime: ZonedDateTime): String {
        return dateTime.format(formatter)
    }
}
