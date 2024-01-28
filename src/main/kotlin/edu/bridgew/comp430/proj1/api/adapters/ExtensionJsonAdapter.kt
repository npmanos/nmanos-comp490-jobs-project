package edu.bridgew.comp430.proj1.api.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import edu.bridgew.comp430.proj1.api.data.*

class ExtensionJsonAdapter : JsonAdapter<List<Extension>>() {
    override fun fromJson(reader: JsonReader): List<Extension> = with(reader) {
        val extList = mutableListOf<Extension>()
        beginObject()
        while (hasNext()) {
            val key = nextName()
            when (key) {
                "schedule_type" -> extList.add(ScheduleType(nextString()))
                "posted_at" -> extList.add(PostedAt(nextString()))
                "salary" -> extList.add(Salary(nextString()))
                else -> extList.add(UnknownExtension(key, nextString()))
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
                is PostedAt -> name("posted_at").value(extension.date)
                is Salary -> name("salary").value(extension.salaryRange)
                is UnknownExtension -> name(extension.extension).value(extension.value)
            }
        }
        endObject()

        return@with
    }

}
