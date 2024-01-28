package edu.bridgew.comp430.proj1.api.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import edu.bridgew.comp430.proj1.api.data.*
import org.ocpsoft.prettytime.PrettyTime
import org.ocpsoft.prettytime.nlp.PrettyTimeParser
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.ZoneId

class ExtensionJsonAdapter : JsonAdapter<List<Extension>>() {

    private companion object {
        private val parser = PrettyTimeParser()
        private val formatter = PrettyTime()

        private fun toLocalDateTime(relativeDateTime: String): LocalDateTime {
            val parsedDates = parser.parse(relativeDateTime)

            if (parsedDates.size != 1) throw IllegalArgumentException()

            val date = parsedDates[0]

            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        }

        private fun toRelativeTimeStr(dateTime: LocalDateTime): String {
            return formatter.format(dateTime)
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
                is PostedAt -> name("posted_at").value(toRelativeTimeStr(extension.date))
                is Salary -> name("salary").value(extension.salaryRange)
                is UnknownExtension -> name(extension.extension).value(extension.value)
            }
        }
        endObject()

        return@with
    }

}
