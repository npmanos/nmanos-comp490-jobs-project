package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JobHighlight(
    val title: String,
    val items: List<String>
)
