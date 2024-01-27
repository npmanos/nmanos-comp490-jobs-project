package edu.bridgew.comp430.proj1

import edu.bridgew.comp430.proj1.api.SerpApiClient
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val dotenv = dotenv {
    ignoreIfMissing = false
}

fun main(args: Array<String>) = runBlocking {
    val jobSearchClient = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).jobSearchApi
    val jobs = async {
        jobSearchClient.search("software engineer")
    }
    println(jobs.await())
}

//fun main(args: Array<String>) {
//    println(dotenv["JOBSPROJ_API_KEY"])
//}
