package edu.bridgew.comp490.proj1.data

import edu.bridgew.comp490.proj1.data.entities.JobSearchResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Google Job Search Service.
 *
 * This service is used to search for jobs using Google's job search engine
 * using SerpAPI. Obtain an instance of this service by using
 * [Retrofit.create][retrofit2.create].
 *
 * @see [GoogleJobSearchServiceImpl]
 */
interface GoogleJobSearchService {
    /**
     * Search for jobs using a query string and an optional start index.
     *
     * @param query The search query string.
     * @param start The start index for the search results. Default value is 0.
     * @return A [Response] object containing a [JobSearchResult] with up to 10 jobs.
     */
    @GET("search.json?engine=google_jobs")
    suspend fun search(
        @Query("q") query: String,
        @Query("start") start: Int = 0,
    ): Response<JobSearchResult>
}
