package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import edu.bridgew.comp490.proj1.data.db.JobDAO

@JsonClass(generateAdapter = true)
data class Job(
    val title: String,
    @Json(name = "company_name") val companyName: String,
    val location: String? = null,
    val description: String? = null,
    @Json(name = "job_highlights") val jobHighlights: List<JobHighlight>? = null,
    @Json(name = "related_links") val relatedLinks: List<Link>? = null,
    val thumbnail: String? = null,
    val extensions: List<String>? = null,
    @Json(name = "detected_extensions") val detectedExtensions: List<Extension>?,
    @Json(name = "job_id") val jobId: String,
) {
    companion object {
        @JvmStatic
        fun daoMapper(
            job: JobDAO,
            highlights: List<JobHighlight>?,
            links: List<Link>?,
            extensions: List<String>?,
            detectedExtensions: List<Extension>?,
        ) = Job(
            job.title,
            job.companyName,
            job.location,
            job.description,
            highlights,
            links,
            job.thumbnail,
            extensions,
            detectedExtensions,
            job.jobId,
        )
    }
}
