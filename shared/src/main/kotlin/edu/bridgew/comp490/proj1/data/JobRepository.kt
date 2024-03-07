package edu.bridgew.comp490.proj1.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import edu.bridgew.comp490.proj1.data.Currency.Companion.dollars
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.hour
import edu.bridgew.comp490.proj1.data.db.JobDAO
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.data.entities.Extension
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.JobHighlight
import edu.bridgew.comp490.proj1.data.entities.Link
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.data.entities.Salary
import edu.bridgew.comp490.proj1.data.entities.ScheduleType
import edu.bridgew.comp490.proj1.data.entities.ShortJobDAO
import edu.bridgew.comp490.proj1.data.entities.UnknownExtension
import edu.bridgew.comp490.proj1.data.entities.WorkFromHome
import edu.bridgew.comp490.proj1.executeAsListOrNull
import edu.bridgew.comp490.proj1.io.JobXlsx
import edu.bridgew.comp490.proj1.nullIfEmpty
import edu.bridgew.comp490.proj1.relativeTimeString
import edu.bridgew.comp490.proj1.toLong
import io.nacular.measured.units.Measure
import io.nacular.measured.units.div
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext

/**
 * Repository class for managing job data.
 *
 * This class provides methods to save jobs from the Google Job Search Service or an Excel file to a database,
 * and then all the jobs from the database.
 *
 * @property apiService The [GoogleJobSearchServiceImpl] instance used to get jobs from the Google Job Search Service.
 * @param db The [JobSearchDB] instance used to interact with the local database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JobRepository(private val apiService: GoogleJobSearchServiceImpl, db: JobSearchDB) {
    private val queries = db.jobQueries

    /**
     * Get jobs from the Google Job Search Service and the local database.
     *
     * This method first gets jobs from the Google Job Search Service based on the provided query and number of pages.
     * The jobs are then saved to the local database. Finally, the jobs are retrieved from the local database and returned.
     *
     * @param query The search query string.
     * @param pages The number of pages to get from the Google Job Search Service. Default value is 1.
     * @return A [Flow] of [Jobs][Job] from the local database.
     */
    suspend fun searchAndGetJobs(query: String, pages: Int = 1): Flow<Job> = withContext(Dispatchers.IO) {
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

        return@withContext getJobsAsync(query)
    }

    /**
     * Save jobs from an Excel file to the local database.
     *
     * This method reads jobs from the provided Excel file and saves them to the local database.
     *
     * @param query The search query string to associate the jobs with in the database.
     * @param xlsx The JobXlsx instance representing the Excel file.
     */
    suspend fun saveJobsFromExcel(query: String, xlsx: JobXlsx) = withContext(Dispatchers.IO) {
        xlsx.forEach { row ->
            val postedAt = row.postedAt?.let { PostedAt(it) }

            val salaryMaxStr = if (row.salaryMax != -1.0) "-${row.salaryMax}" else ""

            val salary = when (row.salaryType) {
                "hourly" -> Salary.parse("${row.salaryMin}$salaryMaxStr an hour")
                "weekly" -> Salary.parse("${row.salaryMin}$salaryMaxStr a week")
                "monthly" -> Salary.parse("${row.salaryMin}$salaryMaxStr a month")
                "yearly" -> Salary.parse("${row.salaryMin}$salaryMaxStr a year")
                else -> null
            }

            val extensions = mutableListOf<String>().apply {
                if (postedAt != null) this.add(postedAt.date.relativeTimeString)
                if (salary != null) this.add(salary.originalJson)
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
                jobId = row.jobId,
            )

            upsertJob(query, job)
        }
    }

    internal fun upsertJob(query: String, job: Job) {
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

            job.jobHighlights?.forEach { highlight ->
                highlight.items.forEach { item ->
                    queries.insertHighlight(highlight.title, item, job.jobId)
                }
            }

            job.relatedLinks?.forEach { queries.insertLink(it.link, it.text, job.jobId) }
            job.extensions?.forEach { queries.insertExtension(it, job.jobId) }
            job.detectedExtensions?.forEach {
                when (it) {
                    is ScheduleType -> queries.insertDetectedExtension(it.extType, it.type, job.jobId)
                    is PostedAt -> queries.insertDetectedExtension(it.extType, it.date.toString(), job.jobId)
                    is Salary -> queries.insertSalary(it.min `in` dollars / hour, it.max `in` dollars / hour, it.unit, it.originalJson, job.jobId)
                    is WorkFromHome -> return@forEach
                    is UnknownExtension -> queries.insertDetectedExtension(it.extType, it.value, job.jobId)
                }
            }
        }
    }

    fun getJob(jobId: String): Job = queries.getJob(jobId)
        .executeAsOne()
        .transformToJob()

    fun getJobs(): List<Job> = queries.getAllJobs()
        .executeAsList()
        .map { it.transformToJob() }

    fun getJobs(query: String): List<Job> = queries.getJobsForSearch(query)
        .executeAsList()
        .map { it.transformToJob() }

    fun getFilteredShortJobs(
        keywordFilter: String,
        isWFH: Boolean? = null,
        locationFilterEnabled: Boolean = false,
        selectedLocations: Collection<String>? = null,
        salaryFilterEnabled: Boolean = false,
        minSalary: Measure<Wage>? = null,
    ): List<ShortJobDAO> = queries.getFilteredShortJobs(
        keywordFilter,
        isWFH,
        locationFilterEnabled.toLong(),
        selectedLocations ?: listOf(),
        salaryFilterEnabled.toLong(),
        minSalary?.`in`(dollars / hour) ?: -1.0,
        ShortJobDAO::build,
    ).executeAsList()

    fun getJobsAsync(): Flow<Job> = queries.getAllJobs()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .transformToJobs()

    fun getJobsAsync(query: String): Flow<Job> = queries.getJobsForSearch(query)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .transformToJobs()

    fun getLocations(): List<String> = queries.getLocations().executeAsList()

    private fun JobDAO.transformToJob(): Job {
        val highlights = queries.getHighlights(jobId)
            .executeAsListOrNull()
            ?.groupBy(
                keySelector = { it.title },
                valueTransform = { it.item },
            )
            ?.map { JobHighlight(it.key, it.value) }

        val links = queries.getLinks(jobId, ::Link).executeAsListOrNull()
        val extensions = queries.getExtensions(jobId).executeAsListOrNull()
        val detectedExtensions = getDetectedExtensions(this)

        return Job.daoMapper(this, highlights, links, extensions, detectedExtensions)
    }

    private fun List<JobDAO>.transformToJobs() = this.map { it.transformToJob() }

    private fun Flow<List<JobDAO>>.transformToJobs() = this.transform { jobList -> jobList.forEach { emit(it.transformToJob()) } }

    private fun getDetectedExtensions(job: JobDAO): List<Extension>? {
        val extensions: List<Extension> = mutableListOf<Extension>() +
            queries.getDetectedExtensions(job.jobId, Extension::getById).executeAsList() +
            queries.getSalary(job.jobId) { min, max, unit, originalJson -> Salary(min.hourly() `as` unit.unit, max.hourly() `as` unit.unit, unit, originalJson) }.executeAsList()

        if (job.isWFH != null) {
            return extensions + when (job.isWFH) {
                true -> WorkFromHome(true)
                false -> WorkFromHome(false)
            }
        }

        return extensions.nullIfEmpty()
    }
}
