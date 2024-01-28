package edu.bridgew.comp430.proj1.api

import edu.bridgew.comp430.proj1.api.data.JobSearchResult
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleJobSearchService {
    @GET("search.json?engine=google_jobs")
    suspend fun search(
        @Query("q") query: String,
        @Query("start") start: Int = 0
    ) : Response<JobSearchResult>
}
