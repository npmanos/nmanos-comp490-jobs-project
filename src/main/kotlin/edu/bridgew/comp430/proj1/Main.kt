package edu.bridgew.comp430.proj1

import edu.bridgew.comp430.proj1.api.ApiResult
import edu.bridgew.comp430.proj1.api.GoogleJobSearchServiceImpl
import edu.bridgew.comp430.proj1.api.SerpApiClient
import edu.bridgew.comp430.proj1.api.data.UnknownExtension
import edu.bridgew.comp430.proj1.io.JobsFileWriter
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@OptIn(ExperimentalCoroutinesApi::class)
fun main(): Unit = runBlocking {
    val writer = JobsFileWriter("./output/jobs.txt".toPath())
    val retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).retrofit
    val jobSearchClient = GoogleJobSearchServiceImpl(retrofit)
    val pages = 1

    (0 until pages).asFlow()
        .flatMapMerge { page -> jobSearchClient.getJobs("software engineer", page) }
        .buffer(pages)
        .onCompletion { writer.close() }
        .collect { result ->
            when (result) {
                is ApiResult.Success -> {
                    result.body.forEach {
                        println(it.title)
                        it.detectedExtensions.filterIsInstance<UnknownExtension>().forEach { println("${it.extension}: ${it.value}") }
                        println()
                        writer.writeJob(it)
                    }
                }
                is ApiResult.Error -> println("ERROR! ${result.errorBody}")
            }
        }
}
