package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.net.URI
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class SearchMetadata(
    val id: String,
//    val status: SearchStatus,
    @Json(name = "json_endpoint") val jsonEndpoint: String, //TODO: convert to URI
    @Json(name = "created_at") val createdAt: String, //TODO: convert to LocalDateTime
    @Json(name = "processed_at") val processedAt: String, //TODO: convert to LocalDateTime
    @Json(name = "google_jobs_url") val googleJobsUrl: String, //TODO: convert to URI
    @Json(name = "raw_html_file") val rawHtmlFile: String, //TODO: convert to URI
    @Json(name = "total_time_taken") val totalTimeTaken: Double
)
