package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.net.URI
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class SearchMetadata(
    val id: String,
    val status: SearchStatus,
    @Json(name = "json_endpoint") val jsonEndpoint: URI,
    @Json(name = "created_at") val createdAt: LocalDateTime,
    @Json(name = "processed_at") val processedAt: LocalDateTime,
    @Json(name = "google_jobs_url") val googleJobsUrl: URI,
    @Json(name = "raw_html_file") val rawHtmlFile: URI,
    @Json(name = "total_time_taken") val totalTimeTaken: Double
)
