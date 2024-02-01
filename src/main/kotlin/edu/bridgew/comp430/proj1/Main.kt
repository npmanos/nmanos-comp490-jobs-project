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
import kotlin.system.exitProcess

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@OptIn(ExperimentalCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {
    val query = parseArgs(args)

    val writer = JobsFileWriter("./output/jobs.txt".toPath())
    val retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).retrofit
    val jobSearchClient = GoogleJobSearchServiceImpl(retrofit)
    val pages = 1

    (0 until pages).asFlow()
        .flatMapMerge { page -> jobSearchClient.getJobs(query, page) }
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

private fun parseArgs(args: Array<String>): String {
    if (args.isEmpty()) return "software engineer boston"
    if (args.size == 1) {
        if (args[0].startsWith("--query=")) {
            return args[0].removePrefix("--query=")
        } else {
            println("Error! Unknown option '${args[0]}'")
            exitProcess(1)
        }
    }

    if (args.size == 2) {
        if (args[0] == "-q") return args[1]
        println("Error! Unknown option '${args[1]}'")
        exitProcess(1)
    }

    println("Error! Too many options")
    exitProcess(2)
}
