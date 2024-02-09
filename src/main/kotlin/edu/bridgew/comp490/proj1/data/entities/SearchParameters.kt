package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchParameters(
    val q: String,
    val engine: String,
    @Json(name = "google_domain") val googleDomain: String,
    val hl: String?,
)
