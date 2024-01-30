package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.ZonedDateTime

@JsonClass(generateAdapter = true)
data class SearchMetadata(
    val id: String,
    val status: SearchStatus,
    val error: String?,
    @Json(name = "json_endpoint") val jsonEndpoint: String,
    @Json(name = "created_at") val createdAt: ZonedDateTime,
    @Json(name = "processed_at") val processedAt: ZonedDateTime,
    @Json(name = "google_jobs_url") val googleJobsUrl: String,
    @Json(name = "raw_html_file") val rawHtmlFile: String,
    @Json(name = "total_time_taken") val totalTimeTaken: Double,
)
