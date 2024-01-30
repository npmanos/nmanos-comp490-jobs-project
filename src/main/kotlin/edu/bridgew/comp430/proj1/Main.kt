package edu.bridgew.comp430.proj1

import edu.bridgew.comp430.proj1.api.GoogleJobSearchServiceImpl
import edu.bridgew.comp430.proj1.api.SerpApiClient
import edu.bridgew.comp430.proj1.api.data.UnknownExtension
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking

private val dotenv = dotenv {
    ignoreIfMissing = false
}

fun main(args: Array<String>): Unit = runBlocking {
    val retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).retrofit
    val jobSearchClient = GoogleJobSearchServiceImpl(retrofit)
//    val jobSearch = withContext(Dispatchers.IO) {
//        val jobs = mutableListOf<Job>()
//            (0..<5).map { page ->
//                async {
//                    jobSearchClient.search("software engineer", page * 10).jobsResults
//                }
//            }
//        }
//
//    val jobs: List<Job> = jobSearch.awaitAll().flatMap { mutableListOf<Job>().apply {
//        if (it != null) {
//            addAll(it)
//        }
//    } }

    val jobs = jobSearchClient.getJobsPages("software engineer", 2)

    jobs.forEach {
        println(it)
        println()
    }

    val unknownExtensions = jobs.flatMap { it.detectedExtensions.filterIsInstance<UnknownExtension>() }
    unknownExtensions.forEach { println(it) }
}
