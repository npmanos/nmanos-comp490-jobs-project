package edu.bridgew.comp490.proj1.data

import edu.bridgew.comp490.proj1.data.entities.JobSearchResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleJobSearchService {
    @GET("search.json?engine=google_jobs")
    suspend fun search(
        @Query("q") query: String,
        @Query("start") start: Int = 0,
    ): Response<JobSearchResult>
}
