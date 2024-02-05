package edu.bridgew.comp490.proj1.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class JobRepository(private val apiService: GoogleJobSearchServiceImpl, private val db: JobSearchDB) {
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

    private fun upsertJob(query: String, job: Job) {
        db.jobQueries.transaction {
            val isWFH: Boolean? = (job.detectedExtensions?.firstOrNull { it is WorkFromHome } as WorkFromHome?)?.isWFH
            db.jobQueries.insertJob(
                job.title,
                job.companyName,
                job.location,
                job.description,
                job.thumbnail,
                isWFH,
                job.jobId
            )

            db.jobQueries.insertQuery(query, job.jobId)
            job.jobHighlights?.forEach { db.jobQueries.insertHighlight(it.title, it.items.joinToString("\n"), job.jobId) }
            job.relatedLinks?.forEach { db.jobQueries.insertLink(it.link, it.text, job.jobId) }
            job.extensions?.forEach { db.jobQueries.insertExtension(it, job.jobId) }
            job.detectedExtensions?.forEach {
                when (it) {
                    is ScheduleType -> db.jobQueries.insertDetectedExtension(it.extType, it.type, job.jobId)
                    is PostedAt -> db.jobQueries.insertDetectedExtension(it.extType, it.date.toString(), job.jobId)
                    is Salary -> db.jobQueries.insertDetectedExtension(it.extType, it.salaryRange, job.jobId)
                    is WorkFromHome -> return@forEach
                    is UnknownExtension -> db.jobQueries.insertDetectedExtension(it.extType, it.value, job.jobId)
                }
            }
        }
    }

    private suspend fun getJobsFromDB(query: String): Flow<Job> = db.jobQueries.getJobsForSearch(query)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .transform { jobList ->
            jobList.forEach { job ->
                val highlights = db.jobQueries.getHighlights(job.jobId) { title, items -> JobHighlight(title, items.split("\n")) }.executeAsList().ifEmpty { null }
                val links = db.jobQueries.getLinks(job.jobId) { link, text -> Link(link, text) }.executeAsList().ifEmpty { null }
                val extensions = db.jobQueries.getExtensions(job.jobId).executeAsList().ifEmpty { null }
                val detectedExtensions = listOf(
                    db.jobQueries.getDetectedExtensions(job.jobId) { type, value -> Extension.getById(type, value) }.executeAsList(),
                    when (job.isWFH) {
                        null -> listOf()
                        true -> listOf(WorkFromHome(true))
                        false -> listOf(WorkFromHome(false))
                    }
                ).flatten().ifEmpty { null }

                emit(
                    Job(
                        job.title,
                        job.companyName,
                        job.location,
                        job.description,
                        highlights,
                        links,
                        job.thumbnail,
                        extensions,
                        detectedExtensions,
                        job.jobId
                    )
                )
            }
        }
}
