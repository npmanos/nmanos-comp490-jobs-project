package edu.bridgew.comp490.proj1.data.entities.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import edu.bridgew.comp490.proj1.data.entities.Extension
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.data.entities.Salary
import edu.bridgew.comp490.proj1.data.entities.ScheduleType
import edu.bridgew.comp490.proj1.data.entities.UnknownExtension
import edu.bridgew.comp490.proj1.data.entities.WorkFromHome
import edu.bridgew.comp490.proj1.relativeTimeString
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.TemporalAmount

class ExtensionJsonAdapter : JsonAdapter<List<Extension>>() {

    private companion object {
//        @JvmStatic
//        private val parser = PrettyTimeParser()

        @JvmStatic
        private fun toLocalDateTime(relativeDateTime: String): LocalDateTime {
            val (numOfUnitString, unit, _) = relativeDateTime.split(" ")
            val numOfUnit = numOfUnitString.toIntOrNull() ?: throw JsonDataException("Unable to parse posted_at value to LocalDateTime: $relativeDateTime")
            val period: TemporalAmount = when (unit) {
                "hour", "hours" -> Duration.ofHours(numOfUnit.toLong())
                "day", "days" -> Period.ofDays(numOfUnit)
                "week", "weeks" -> Period.ofWeeks(numOfUnit)
                "month", "months" -> Period.ofMonths(numOfUnit)
                "year", "years" -> Period.ofYears(numOfUnit)
                else -> throw JsonDataException("Unable to parse posted_at value to LocalDateTime: $relativeDateTime")
            }

            return LocalDateTime.now() - period
        }
    }
    override fun fromJson(reader: JsonReader): List<Extension> = with(reader) {
        val extList = mutableListOf<Extension>()
        beginObject()
        while (hasNext()) {
            val key = nextName()
            when (key) {
                "schedule_type" -> extList.add(ScheduleType(nextString()))
                "posted_at" -> extList.add(PostedAt(toLocalDateTime(nextString())))
                "salary" -> extList.add(Salary(nextString()))
                "work_from_home" -> extList.add(WorkFromHome(nextBoolean()))
                else -> extList.add(UnknownExtension(key, nextSource().readUtf8()))
            }
        }
        endObject()
        return extList
    }

    override fun toJson(writer: JsonWriter, value: List<Extension>?) = with(writer) {
        if (value == null) {
            beginObject()
            endObject()
            return@with
        }

        beginObject()
        for (extension in value) {
            when (extension) {
                is ScheduleType -> name("schedule_type").value(extension.type)
                is PostedAt -> name("posted_at").value(extension.date.relativeTimeString)
                is Salary -> name("salary").value(extension.salaryRange)
                is WorkFromHome -> name("work_from_home").value(extension.isWFH)
                is UnknownExtension -> name(extension.extension).value(extension.value)
            }
        }
        endObject()

        return@with
    }
}
