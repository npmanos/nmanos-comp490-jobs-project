package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import edu.bridgew.comp490.proj1.data.db.JobDAO
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Job(
    override val title: String,
    @Json(name = "company_name") override val companyName: String,
    override val location: String? = null,
    val description: String? = null,
    @Json(name = "job_highlights") val jobHighlights: List<JobHighlight>? = null,
    @Json(name = "related_links") val relatedLinks: List<Link>? = null,
    override val thumbnail: String? = null,
    val extensions: List<String>? = null,
    @Json(name = "detected_extensions") val detectedExtensions: List<Extension>?,
    @Json(name = "job_id") override val jobId: String,
) : ShortJob, Serializable {
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

interface ShortJob {
    val title: String
    val companyName: String
    val location: String?
    val thumbnail: String?
    val jobId: String
}

data class ShortJobDAO(
    override val title: String,
    override val companyName: String,
    override val location: String?,
    override val thumbnail: String?,
    val postedAt: String?,
    override val jobId: String,
) : ShortJob {
    companion object Factory {
        @JvmStatic
        fun build(
            title: String,
            companyName: String,
            location: String?,
            thumbnail: String?,
            postedAt: String?,
            jobId: String,
        ) = ShortJobDAO(title, companyName, location, thumbnail, postedAt, jobId)
    }
}
