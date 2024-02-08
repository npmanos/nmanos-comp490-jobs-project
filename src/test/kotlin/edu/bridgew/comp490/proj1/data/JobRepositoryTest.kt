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
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@OptIn(ExperimentalStdlibApi::class)
@ExtendWith(MockKExtension::class)
@MockKExtension.ConfirmVerification
@MockKExtension.CheckUnnecessaryStub
class JobRepositoryTest {
    private val jsonTestData = Path(dotenv["JOBSPROJ_TEST_DIR"]).listDirectoryEntries("software_engineer-*.json")
    lateinit var driver: JdbcSqliteDriver
    lateinit var db: JobSearchDB
    @MockK lateinit var apiService: GoogleJobSearchServiceImpl
    lateinit var jobRepository: JobRepository

    @BeforeTest
    fun setupTests() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties().apply { put("foreign_keys", "true") })
        JobSearchDB.Schema.create(driver)
        db = JobSearchDB(driver)
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

    private fun getTestData(): JobSearchResult = jsonTestData.random().run { moshi.adapter<JobSearchResult>().fromJson(this.toString()) }!!
}
