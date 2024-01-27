package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonClass
import java.net.URI

@JsonClass(generateAdapter = true)
data class Link(
    val link: URI,
    val title: String
)
