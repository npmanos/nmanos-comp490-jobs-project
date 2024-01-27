package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class SearchStatus

@TypeLabel("Processing")
data object SearchProcessing : SearchStatus()

@TypeLabel("Success")
data object SearchSuccess : SearchStatus()

@TypeLabel("Error")
@JsonClass(generateAdapter = true)
class SearchError(val error: String) : SearchStatus()
