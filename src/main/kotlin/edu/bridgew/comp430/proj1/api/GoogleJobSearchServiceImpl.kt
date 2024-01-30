package edu.bridgew.comp430.proj1.api

import edu.bridgew.comp430.proj1.api.data.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.create

class GoogleJobSearchServiceImpl(retrofit: Retrofit) : GoogleJobSearchService by retrofit.create() {
    suspend fun getJobsPages(query: String, pages: Int = 1): List<Job> = withContext(Dispatchers.IO) {
        return@withContext (0..<pages).map { page -> async { search(query, page * 10) } }
            .awaitAll()
            .flatMap { it.body()?.jobsResults ?: listOf() }
    }
}
