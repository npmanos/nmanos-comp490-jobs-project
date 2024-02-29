package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class JobHighlight(
    val title: String?,
    val items: List<String>,
) : Serializable
