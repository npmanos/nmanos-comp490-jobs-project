package edu.bridgew.comp490.proj1.api.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Chip(val type: ChipType, val param: String, val options: List<ChipOption>)
