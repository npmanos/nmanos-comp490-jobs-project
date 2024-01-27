package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Link(
    val link: String, // TODO: convert to URI
    val text: String
)
