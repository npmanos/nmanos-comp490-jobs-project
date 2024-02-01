package edu.bridgew.comp490.proj1.api.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChipOption(val text: String, val value: String?)
