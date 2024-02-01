package edu.bridgew.comp490.proj1.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Job(
    val title: String,
    @Json(name = "company_name") val companyName: String,
    val location: String?,
    val description: String?,
    @Json(name = "job_highlights") val jobHighlights: List<JobHighlight>?,
    @Json(name = "related_links") val relatedLinks: List<Link>?,
    val thumbnail: String?,
    val extensions: List<String>?,
    @Json(name = "detected_extensions") val detectedExtensions: List<Extension>?,
    @Json(name = "job_id") val jobId: String,
)
