package edu.bridgew.comp430.proj1.api.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import edu.bridgew.comp430.proj1.api.data.*

class SearchStatusAdapter {
    @FromJson
    fun fromJson(status: String): SearchStatus {
        return when (status) {
            "Processing" -> StatusProcessing
            "Success" -> StatusSuccess
            "Error" -> StatusError
            else -> StatusUnknown(status)
        }
    }

    @ToJson
    fun toJson(searchStatus: SearchStatus): String {
        return searchStatus.status
    }
}
