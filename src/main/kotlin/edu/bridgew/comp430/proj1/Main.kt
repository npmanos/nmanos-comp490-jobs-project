package edu.bridgew.comp430.proj1

import edu.bridgew.comp430.proj1.api.ApiResult
import edu.bridgew.comp430.proj1.api.GoogleJobSearchServiceImpl
import edu.bridgew.comp430.proj1.api.SerpApiClient
import edu.bridgew.comp430.proj1.api.data.UnknownExtension
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.runBlocking

private val dotenv = dotenv {
    ignoreIfMissing = false
}

@OptIn(ExperimentalCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {
    val retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).retrofit
    val jobSearchClient = GoogleJobSearchServiceImpl(retrofit)
    val pages = 3

    (0..<3).asFlow()
        .flatMapMerge { page -> jobSearchClient.getJobs("software engineer", page) }
        .buffer(pages)
        .collect { result ->
            when (result) {
                is ApiResult.Success -> {
                    result.body.forEach {
                        println(it)
                        println("===================================")
                    }
                }
                is ApiResult.Error -> println("ERROR! ${result.errorBody}")
            }
        }
}
