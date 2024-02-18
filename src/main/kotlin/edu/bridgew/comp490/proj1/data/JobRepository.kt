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

    private companion object {
        @JvmStatic
        private val header = listOf("Company Name", "Posting Age", "Job Id", "Country", "Location", "Publication Date", "Salary Max", "Salary Min", "Salary Type", "Job Title")
    }

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

    suspend fun saveJobsFromExcel(query: String, xlsx: XSSFWorkbook) = withContext(Dispatchers.IO) {
        val sheet = requireNotNull(xlsx["Comp490 Jobs"]) { "ERROR! Excel file is missing sheet named \"Comp490 Jobs\"" }

        val headerRow = requireNotNull(sheet[0]) { "ERROR! \"Comp490 Jobs\" sheet is empty" }

        for (i in 0 until 10) {
            val colHeader = requireNotNull(sheet[0, i]?.stringCellValue) { "ERROR! Missing \"${header[i]}\" column" }
            require(colHeader == header[i])
        }

        sheet.forEachIndexed { index, row ->
            if (index == 0) return@forEachIndexed

            val title = requireNotNull(row[9]?.stringCellValue) { "ERROR! Job Title is missing in row ${index + 1}" }
            val companyName = requireNotNull(row[0]?.stringCellValue) { "ERROR! Company Name is missing in row ${index + 1}" }
            val location = row[4]?.stringCellValue

            val extensions = mutableListOf<String>()
            val detectedExtensions = mutableListOf<Extension>()

            val postedAt = row[5]?.let { PostedAt(LocalDateTime.ofEpochSecond(it.numericCellValue.toLong() / 1000, (it.numericCellValue.toLong() % 1000).toInt(), ZoneOffset.UTC)) }
            if (postedAt != null) {
                extensions.add(postedAt.date.relativeTimeString)
                detectedExtensions.add(postedAt)
            }

            val salaryMin = row[7]?.numericCellValue?.toString() ?: ""
            val salaryMax = row[6]?.let { if (it.numericCellValue == -1.0) "" else "-${it.numericCellValue}" } ?: ""
            val salary = when (row[8]?.stringCellValue) {
                "hourly" -> Salary("$salaryMin$salaryMax an hour")
                "yearly" -> Salary("$salaryMin$salaryMax a year")
                else -> null
            }
            if (salary != null) {
                extensions.add(salary.salaryRange)
                detectedExtensions.add(salary)
            }

            val jobId = requireNotNull(row[2]?.stringCellValue) {"ERROR! Job Id is missing in row ${index + 1}"}

            val job = Job(
                title,
                companyName,
                location,
                extensions = extensions.nullIfEmpty(),
                detectedExtensions = detectedExtensions,
                jobId = jobId
            )

            println(job)
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
