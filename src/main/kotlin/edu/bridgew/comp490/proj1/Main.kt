package edu.bridgew.comp490.proj1

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import edu.bridgew.comp490.proj1.data.GoogleJobSearchServiceImpl
import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.data.SerpApiClient
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.io.JobsFileWriter
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class JobSearch : CliktCommand(
    help = """
    |This application saves 50 results from a Google job search for <query> to <output>.
    |
    |You can customize <query> and <output> using the options below.
    """.trimMargin(),
) {
    private val query by option("-q", "--query", help = "Job search query")
        .default("software engineer boston")

    private val output by option("-o", "--output", help = "Output file location")
        .file(mustExist = false, canBeDir = false, mustBeWritable = true)
        .convert { it.toOkioPath() }
        .default("output/jobs.txt".toPath(), "output/jobs.txt")

    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true) }
        }
    }

    @OptIn(FlowPreview::class)
    override fun run() = runBlocking {
        echo("Searching...")
        echo()

        val driver = JdbcSqliteDriver("jdbc:sqlite:output/jobs.db", Properties().apply { put("foreign_keys", "true") })
        JobSearchDB.Schema.create(driver)
        val db = JobSearchDB(driver)

        val writer = JobsFileWriter(output)
        val retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"]).retrofit
        val jobSearchClient = GoogleJobSearchServiceImpl(retrofit)
        val jobRepo = JobRepository(jobSearchClient, db)
        val pages = 5

        jobRepo.getJobs(query, pages)
            .buffer(pages)
            .onCompletion {
                echo()
                echo("Saving file...")
            }
            .timeout(2000.milliseconds)
            .catch { e -> if (e !is TimeoutCancellationException) throw e }
            .collect { job ->
                echo("[${job.companyName}] ${job.title}")
                writer.writeJob(job)
            }
    }
}

fun main(args: Array<String>) = JobSearch().main(args)
