package edu.bridgew.comp490.proj1.data.entities.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import edu.bridgew.comp490.proj1.data.entities.SearchStatus
import edu.bridgew.comp490.proj1.data.entities.StatusError
import edu.bridgew.comp490.proj1.data.entities.StatusProcessing
import edu.bridgew.comp490.proj1.data.entities.StatusSuccess
import edu.bridgew.comp490.proj1.data.entities.StatusUnknown

class SearchStatusAdapter : JsonAdapter<SearchStatus>() {
    override fun fromJson(reader: JsonReader): SearchStatus = with(reader) {
        return try {
            val status = peekJson().nextString()
            when (status) {
                "Processing" -> {
                    nextString()
                    StatusProcessing
                }
                "Success" -> {
                    nextString()
                    StatusSuccess
                }
                "Error" -> {
                    nextString()
                    StatusError
                }
                else -> StatusUnknown(nextString())
            }
        } catch (e: JsonDataException) {
            StatusUnknown(nextSource().readUtf8())
        }
    }

    override fun toJson(writer: JsonWriter, value: SearchStatus?) = with(writer) {
        if (value is StatusUnknown) {
            valueSink().writeUtf8(value.value).close()
            return@with
        }

        value(value?.value)
    }
}
