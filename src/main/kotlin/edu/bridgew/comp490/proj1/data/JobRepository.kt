package edu.bridgew.comp490.proj1.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import edu.bridgew.comp490.proj1.data.db.JobDAO
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.data.entities.Extension
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.JobHighlight
import edu.bridgew.comp490.proj1.data.entities.Link
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.data.entities.Salary
import edu.bridgew.comp490.proj1.data.entities.ScheduleType
import edu.bridgew.comp490.proj1.data.entities.UnknownExtension
import edu.bridgew.comp490.proj1.data.entities.WorkFromHome
import edu.bridgew.comp490.proj1.executeAsListOrNull
import edu.bridgew.comp490.proj1.get
import edu.bridgew.comp490.proj1.io.JobXlsx
import edu.bridgew.comp490.proj1.nullIfEmpty
import edu.bridgew.comp490.proj1.relativeTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class JobRepository(private val apiService: GoogleJobSearchServiceImpl, private val db: JobSearchDB) {
    private val queries = db.jobQueries

    suspend fun getJobs(query: String, pages: Int = 1): Flow<Job> = withContext(Dispatchers.IO) {
        (0 until pages).asFlow()
            .flatMapMerge { apiService.getJobs(query, it) }
            .flatMapMerge { result ->
                when (result) {
                    is ApiResult.Success -> {
                        result.body.asFlow()
                    }
                    is ApiResult.Error -> {
                        println("ERROR! ${result.errorBody}")
                        listOf<Job>().asFlow()
                    }
                }
            }.onEach { upsertJob(query, it) }
            .launchIn(this)

        return@withContext getJobsFromDB(query)
    }

    suspend fun saveJobsFromExcel(query: String, xlsx: JobXlsx) = withContext(Dispatchers.IO) {
        xlsx.forEach { row ->
            val postedAt = row.postedAt?.let { PostedAt(it) }

            val salary = when (row.salaryType) {
                "hourly" -> Salary("${row.salaryMin}${row.salaryMax} an hour")
                "yearly" -> Salary("${row.salaryMin}${row.salaryMax} a year")
                else -> null
            }

            val extensions = mutableListOf<String>().apply {
                if (postedAt != null) this.add(postedAt.date.relativeTimeString)
                if (salary != null) this.add(salary.salaryRange)
            }

            val detectedExtensions = mutableListOf<Extension>().apply {
                if (postedAt != null) this.add(postedAt)
                if (salary != null) this.add(salary)
            }

            val job = Job(
                row.title,
                row.companyName,
                row.location,
                extensions = extensions.nullIfEmpty(),
                detectedExtensions = detectedExtensions,
                jobId = row.jobId
            )

            upsertJob(query, job)
        }
    }

    private fun upsertJob(query: String, job: Job) {
        queries.transaction {
            val isWFH: Boolean? = (job.detectedExtensions?.firstOrNull { it is WorkFromHome } as WorkFromHome?)?.isWFH
            queries.insertJob(
                job.title,
                job.companyName,
                job.location,
                job.description,
                job.thumbnail,
                isWFH,
                job.jobId,
            )

            queries.insertQuery(query, job.jobId)
            job.jobHighlights?.forEach { queries.insertHighlight(it.title, it.items.joinToString("\n"), job.jobId) }
            job.relatedLinks?.forEach { queries.insertLink(it.link, it.text, job.jobId) }
            job.extensions?.forEach { queries.insertExtension(it, job.jobId) }
            job.detectedExtensions?.forEach {
                when (it) {
                    is ScheduleType -> queries.insertDetectedExtension(it.extType, it.type, job.jobId)
                    is PostedAt -> queries.insertDetectedExtension(it.extType, it.date.toString(), job.jobId)
                    is Salary -> queries.insertDetectedExtension(it.extType, it.salaryRange, job.jobId)
                    is WorkFromHome -> return@forEach
                    is UnknownExtension -> queries.insertDetectedExtension(it.extType, it.value, job.jobId)
                }
            }
        }
    }

    private fun getJobsFromDB(query: String): Flow<Job> = queries.getJobsForSearch(query)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .transformToJob()

    private fun Flow<List<JobDAO>>.transformToJob() = this.transform { jobList ->
        jobList.forEach { job ->
            val highlights = queries.getHighlights(job.jobId, JobHighlight::daoMapper).executeAsListOrNull()
            val links = queries.getLinks(job.jobId, ::Link).executeAsListOrNull()
            val extensions = queries.getExtensions(job.jobId).executeAsListOrNull()
            val detectedExtensions = getDetectedExtensions(job)

            emit(Job.daoMapper(job, highlights, links, extensions, detectedExtensions))
        }
    }

    private fun getDetectedExtensions(job: JobDAO): List<Extension>? {
        val extensions: List<Extension> = mutableListOf<Extension>() +
            queries.getDetectedExtensions(job.jobId, Extension::getById).executeAsList()

        if (job.isWFH != null) {
            return extensions + when (job.isWFH) {
                true -> WorkFromHome(true)
                false -> WorkFromHome(false)
            }
        }

        return extensions.nullIfEmpty()
    }
}
