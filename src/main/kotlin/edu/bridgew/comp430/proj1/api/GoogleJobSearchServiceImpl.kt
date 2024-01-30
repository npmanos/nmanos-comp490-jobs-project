package edu.bridgew.comp430.proj1.api

import edu.bridgew.comp430.proj1.api.data.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit
import retrofit2.create

class GoogleJobSearchServiceImpl(retrofit: Retrofit) : GoogleJobSearchService by retrofit.create() {
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
