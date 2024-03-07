package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class JobSearchResult(
    @Json(name = "search_metadata") val searchMetadata: SearchMetadata?,
    @Json(name = "search_parameters") val searchParameters: SearchParameters?,
    @Json(name = "jobs_results") val jobsResults: List<Job>?,
    val chips: List<Chip>?,
) : Serializable
