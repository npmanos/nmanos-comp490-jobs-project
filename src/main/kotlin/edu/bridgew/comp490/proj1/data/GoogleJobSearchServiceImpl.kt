package edu.bridgew.comp490.proj1.data

import edu.bridgew.comp490.proj1.data.entities.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit
import retrofit2.create

/**
 * Implementation of the GoogleJobSearchService interface.
 *
 * This class uses [Retrofit] to create an instance of the GoogleJobSearchService interface
 * and provides a method to get jobs from the Google Job Search Service.
 *
 * @param retrofit The [Retrofit] instance used to create the GoogleJobSearchService.
 */
class GoogleJobSearchServiceImpl(retrofit: Retrofit) : GoogleJobSearchService by retrofit.create() {

    /**
     * Get jobs from the Google Job Search Service.
     *
     * This method uses [GoogleJobSearchService.search] to get a list of jobs based on the
     * provided query and page number. The page number is multiplied by 10 to get the start
     * index for the search results. The results are then emitted as a [Flow] of [ApiResult].
     *
     * @param query The search query string.
     * @param page The page number for the search results. Default value is 0.
     * @return A [Flow] of ApiResult containing a [List] of [Jobs][Job].
     */
    suspend fun getJobs(query: String, page: Int = 0): Flow<ApiResult<List<Job>>> = flow {
        val response = search(query, page * 10)

        if (response.isSuccessful) {
            val jobList = response.body()?.jobsResults!!
            emit(ApiResult.Success(jobList))
        } else {
            emit(ApiResult.Error(response.code(), response.errorBody()))
        }
    }.flowOn(Dispatchers.IO)
}
