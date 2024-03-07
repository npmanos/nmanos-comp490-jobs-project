package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ChipOption(val text: String, val value: String?) : Serializable
