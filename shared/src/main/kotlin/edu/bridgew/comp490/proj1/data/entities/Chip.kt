package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Chip(val type: ChipType, val param: String, val options: List<ChipOption>) : Serializable
