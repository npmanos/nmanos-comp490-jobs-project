package edu.bridgew.comp490.proj1.data

import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.JobSearchResult
import edu.bridgew.comp490.proj1.data.entities.adapters.ExtensionJsonAdapter
import edu.bridgew.comp490.proj1.data.entities.adapters.SearchStatusAdapter
import edu.bridgew.comp490.proj1.data.entities.adapters.ZonedDateTimeAdapter
import io.github.cdimascio.dotenv.dotenv
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.excludeRecords
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds


private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class JobRepositoryTest {
    private val jsonTestData: MutableList<Path> = Path(dotenv["JOBSPROJ_TEST_DIR"]).listDirectoryEntries("software_engineer-*.json").toMutableList()
    lateinit var driver: JdbcSqliteDriver
    lateinit var db: JobSearchDB
    lateinit var testData: List<Job>
    @MockK lateinit var apiService: GoogleJobSearchServiceImpl
    lateinit var jobRepository: JobRepository

    @BeforeTest
    fun setupTests() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties().apply { put("foreign_keys", "true") })
        JobSearchDB.Schema.create(driver)
        db = JobSearchDB(driver)

        testData = getTestData().jobsResults!!

        coEvery {
            apiService.getJobs(any(), more(0, andEquals = true))
        } coAnswers { flow { emit(ApiResult.Success(testData)) } }

        jobRepository = JobRepository(apiService, db)
    }

    @Test
    fun `verify JobSearchDB created correctly`() {
        val actualSchema = Query(
            366_887,
            driver,
            """
            |SELECT sql
            |FROM sqlite_master
            |WHERE sql IS NOT NULL
            |ORDER BY tbl_name, type DESC, name
            |""".trimMargin()
        ) { cursor -> cursor.getString(0)!! }
            .executeAsList()
            .reduce { acc, s -> "$acc\n$s" }

        assertEquals(jobSearchDDL, actualSchema)
    }

    @RepeatedTest(value = 32)
    fun `verify db stored and retrieved correctly`() = runTest {
        var jobs = 0

        val coRo = launch {
            jobRepository.getJobs("software engineer")
                .collect {
                    assertContains(testData, it)

                    jobs++
                    if (jobs == testData.size) cancel()
                }
        }

        coRo.join()

        val jobRows = db.jobQueries.getJobCount().executeAsOne().toInt()
        assertEquals(testData.size, jobRows)

        coVerify { apiService.getJobs(any(), more(0, andEquals = true)) }
    }

    companion object {
        val moshi: Moshi = Moshi.Builder()
                .add(ZonedDateTimeAdapter())
                .addAdapter(SearchStatusAdapter())
                .addAdapter(ExtensionJsonAdapter())
                .build()

        @JvmStatic
        private val jobSearchDDL =
            """
            CREATE TABLE DetectedExtensionDAO (
                extType TEXT NOT NULL,
                value TEXT NOT NULL,
                jobId TEXT NOT NULL,
                PRIMARY KEY (extType, jobId),
                FOREIGN KEY (jobId) REFERENCES JobDAO(jobId)
            )
            CREATE TABLE JobDAO (
              title TEXT NOT NULL,
              companyName TEXT NOT NULL,
              location TEXT,
              description TEXT,
              thumbnail TEXT,
              isWFH INTEGER,
              jobId TEXT PRIMARY KEY
            )
            CREATE TABLE JobHighlightDAO (
                title TEXT,
                items TEXT NOT NULL,
                jobId TEXT NOT NULL,
                PRIMARY KEY (title, jobId),
                FOREIGN KEY (jobId) REFERENCES JobDAO(jobId)
            )
            CREATE TABLE LinkDAO (
                link TEXT NOT NULL,
                `text` TEXT NOT NULL,
                jobId TEXT NOT NULL,
                PRIMARY KEY (link, jobId),
                FOREIGN KEY (jobId) REFERENCES JobDAO(jobId)
            )
            CREATE TABLE OtherExtensionDAO (
                value TEXT NOT NULL,
                jobId TEXT NOT NULL,
                PRIMARY KEY (value, jobId),
                FOREIGN KEY (jobId) REFERENCES JobDAO(jobId)
            )
            CREATE TABLE QueryDAO (
                query TEXT NOT NULL,
                jobId TEXT NOT NULL,
                UNIQUE (query, jobId),
                FOREIGN KEY (jobId) REFERENCES JobDAO(jobId)
            )
            CREATE INDEX search_query_idx ON QueryDAO(query)
            """.trimIndent()
    }

    private fun getTestData(): JobSearchResult {
        return jsonTestData.removeLast().run { moshi.adapter<JobSearchResult>().fromJson(this.readText()) }!!
    }
}
