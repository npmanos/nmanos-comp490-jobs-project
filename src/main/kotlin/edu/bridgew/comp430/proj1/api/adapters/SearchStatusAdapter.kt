package edu.bridgew.comp430.proj1.api.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import edu.bridgew.comp430.proj1.api.data.SearchStatus
import edu.bridgew.comp430.proj1.api.data.StatusError
import edu.bridgew.comp430.proj1.api.data.StatusProcessing
import edu.bridgew.comp430.proj1.api.data.StatusSuccess
import edu.bridgew.comp430.proj1.api.data.StatusUnknown

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
