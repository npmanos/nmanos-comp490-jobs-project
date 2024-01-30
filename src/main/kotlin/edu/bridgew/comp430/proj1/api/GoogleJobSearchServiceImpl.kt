package edu.bridgew.comp430.proj1.api

import edu.bridgew.comp430.proj1.api.data.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create

class GoogleJobSearchServiceImpl(retrofit: Retrofit) : GoogleJobSearchService by retrofit.create() {
    suspend fun getJobs(query: String, page: Int = 0): Flow<ApiResult<List<Job>>> = flow {
        // page -> search(query, page * 10).body()?.jobsResults?.asFlow()
        val response = search(query, page * 10)

        if (response.isSuccessful) {
            val jobList = response.body()?.jobsResults!!
            emit(ApiResult.Success(jobList))
        } else {
            emit(ApiResult.Error(response.code(), response.errorBody()))
        }
    }.flowOn(Dispatchers.IO)
}
