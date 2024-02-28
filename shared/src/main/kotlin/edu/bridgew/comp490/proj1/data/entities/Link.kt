package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Link(
    val link: String,
    val text: String,
)
