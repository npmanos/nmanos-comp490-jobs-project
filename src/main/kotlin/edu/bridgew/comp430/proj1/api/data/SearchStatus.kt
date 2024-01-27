package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.DefaultObject
import dev.zacsweers.moshix.sealed.annotations.TypeLabel

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class SearchStatus {

    @TypeLabel("Processing")
    @JsonClass(generateAdapter = true)
    class SearchProcessing : SearchStatus()

    @TypeLabel("Success")
    @JsonClass(generateAdapter = true)
    class SearchSuccess : SearchStatus()

    @TypeLabel("Error")
    @JsonClass(generateAdapter = true)
    class SearchError(val error: String) : SearchStatus()

    @DefaultObject
    object Unknown : SearchStatus()
}
