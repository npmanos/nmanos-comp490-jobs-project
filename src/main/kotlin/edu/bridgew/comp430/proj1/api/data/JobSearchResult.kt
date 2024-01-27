package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JobSearchResult(
    @Json(name = "search_metadata") val searchMetadata: SearchMetadata,
    @Json(name = "search_parameters") val searchParameters: SearchParameters,
    @Json(name = "job_results") val jobResults: List<Job>,
    val chips: List<Chip>
)
