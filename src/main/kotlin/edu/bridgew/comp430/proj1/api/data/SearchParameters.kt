package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchParameters(
    val q: String,
    val engine: String,
    @Json(name = "google_domain") val googleDomain: String,
    val hl: String?
)
