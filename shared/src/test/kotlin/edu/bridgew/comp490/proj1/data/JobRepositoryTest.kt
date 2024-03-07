package edu.bridgew.comp490.proj1.data

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.data.db.SalaryDAO
import edu.bridgew.comp490.proj1.data.entities.Extension
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.JobSearchResult
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.data.entities.Salary
import edu.bridgew.comp490.proj1.data.entities.adapters.ExtensionJsonAdapter
import edu.bridgew.comp490.proj1.data.entities.adapters.SearchStatusAdapter
import edu.bridgew.comp490.proj1.data.entities.adapters.ZonedDateTimeAdapter
import edu.bridgew.comp490.proj1.io.JobXlsx
import edu.bridgew.comp490.proj1.io.JobXlsxRow
import edu.bridgew.comp490.proj1.nullIfEmpty
import edu.bridgew.comp490.proj1.relativeTimeString
import io.github.cdimascio.dotenv.dotenv
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@OptIn(ExperimentalStdlibApi::class)
@ExtendWith(MockKExtension::class)
class JobRepositoryTest {
    lateinit var driver: JdbcSqliteDriver
    lateinit var db: JobSearchDB

    @MockK lateinit var apiService: GoogleJobSearchServiceImpl

    @MockK lateinit var xlsx: JobXlsx

    lateinit var jobRepository: JobRepository

    @BeforeTest
    fun setupTests() {
        driver = JdbcSqliteDriver(
            JdbcSqliteDriver.IN_MEMORY,
            Properties().apply { put("foreign_keys", "true") },
            JobSearchDB.Schema,
        )

        db = JobSearchDB(driver, SalaryDAO.Adapter(EnumColumnAdapter()))

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
            |
            """.trimMargin(),
        ) { cursor -> cursor.getString(0)!! }
            .executeAsList()
            .reduce { acc, s -> "$acc\n$s" }

        println(actualSchema)

        assertEquals(jobSearchDDL, actualSchema)
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(JobTestDataProvider::class)
    fun `verify db stored and retrieved correctly`(testJobSearchResult: JobSearchResult) = runTest {
        val testData = testJobSearchResult.jobsResults!!

        coEvery {
            apiService.getJobs(any(), more(0, andEquals = true))
        } coAnswers {
            flow { emit(ApiResult.Success(testData)) }
        }

        var jobs = 0

        val coRo = launch {
            jobRepository.searchAndGetJobs("software engineer")
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

    @ParameterizedTest
    @MethodSource("getExcelTestData")
    fun `verify saveJobsFromExcel correctly stores records in the database`(
        minSalary: Double,
        maxSalary: Double,
        typeOfSalary: String?,
        jobTitle: String,
        nameOfCompany: String,
        jobLocation: String?,
        timePosted: LocalDateTime?,
        salaryRange: String?,
        idOfJob: String,
    ) = runTest {
        val testExtensions = mutableListOf<String>()
        val testDetectedExtensions = mutableListOf<Extension>()

        if (timePosted != null) {
            testExtensions.add(timePosted.relativeTimeString)
            testDetectedExtensions.add(PostedAt(timePosted))
        }

        if (salaryRange != null) {
            val salary = Salary.parse(salaryRange)
            testExtensions.add(salary.originalJson)
            testDetectedExtensions.add(salary)
        }

        val testJob = Job(
            title = jobTitle,
            companyName = nameOfCompany,
            location = jobLocation,
            extensions = testExtensions.nullIfEmpty(),
            detectedExtensions = testDetectedExtensions.nullIfEmpty(),
            jobId = idOfJob,
        )

        val testJobs = listOf(testJob)
        val testIterator = testJobs.iterator()

        coEvery { xlsx.iterator().hasNext() } coAnswers { testIterator.hasNext() }
        coEvery { xlsx.iterator().next() } coAnswers {
            val job = testIterator.next()
            mockk<JobXlsxRow> {
                every { title } returns job.title
                every { companyName } returns job.companyName
                every { location } returns job.location
                every { postedAt } returns timePosted
                every { salaryMin } returns minSalary
                every { salaryMax } returns maxSalary
                every { salaryType } returns typeOfSalary
                every { jobId } returns job.jobId
            }
        }

        jobRepository.saveJobsFromExcel("software engineer", xlsx)

        val jobsInDb = db.jobQueries.getAllJobs().executeAsList()
        assertEquals(testJobs.size, jobsInDb.size)
        assertEquals(testJob.title, jobsInDb[0].title)
        assertEquals(testJob.companyName, jobsInDb[0].companyName)
        assertEquals(testJob.location, jobsInDb[0].location)
        assertEquals(testJob.jobId, jobsInDb[0].jobId)

        val extensionsInDb = db.jobQueries.getExtensions(testJob.jobId).executeAsList()
        assertEquals(testExtensions.size, extensionsInDb.size)
        assertContentEquals(testExtensions, extensionsInDb)

        val detectedExtensionsInDb = db.jobQueries.getDetectedExtensions(testJob.jobId, Extension::getById).executeAsList() +
            db.jobQueries.getSalary(testJob.jobId) { min, max, unit, originalJson -> Salary(min.hourly() `as` unit.unit, max.hourly() `as` unit.unit, unit, originalJson) }.executeAsList()
        assertEquals(testDetectedExtensions.size, detectedExtensionsInDb.size)
        assertContentEquals(testDetectedExtensions, detectedExtensionsInDb)
    }

    companion object {
        @JvmStatic
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
            CREATE TRIGGER JobFTS_ad
            AFTER DELETE ON JobDAO BEGIN
                INSERT INTO JobFTS(JobFTS, rowid, title, description, jobId) VALUES ('delete', old.rowid, old.title, old.description, old.jobId);
            END
            CREATE TRIGGER JobFTS_ai
            AFTER INSERT ON JobDAO BEGIN
                INSERT INTO JobFTS(rowid, title, description, jobId) VALUES (new.rowid, new.title, new.description, new.jobId);
            END
            CREATE TRIGGER JobFTS_au
            AFTER UPDATE ON JobDAO BEGIN
                INSERT INTO JobFTS(JobFTS, rowid, title, description, jobId) VALUES ('delete', old.rowid, old.title, old.description, old.jobId);
                INSERT INTO JobFTS(rowid, title, description, jobId) VALUES (new.rowid, new.title, new.description, new.jobId);
            END
            CREATE TABLE JobDAO (
              title TEXT NOT NULL,
              companyName TEXT NOT NULL,
              location TEXT,
              description TEXT,
              thumbnail TEXT,
              isWFH INTEGER,
              jobId TEXT PRIMARY KEY
            )
            CREATE VIRTUAL TABLE JobFTS
            USING fts5(title, description, jobId UNINDEXED, content=JobDAO, content_rowid=rowid, tokenize='porter')
            CREATE TABLE 'JobFTS_config'(k PRIMARY KEY, v) WITHOUT ROWID
            CREATE TABLE 'JobFTS_data'(id INTEGER PRIMARY KEY, block BLOB)
            CREATE TABLE 'JobFTS_docsize'(id INTEGER PRIMARY KEY, sz BLOB)
            CREATE TABLE 'JobFTS_idx'(segid, term, pgno, PRIMARY KEY(segid, term)) WITHOUT ROWID
            CREATE TABLE JobHighlightDAO (
                title TEXT,
                jobId TEXT NOT NULL,
                item TEXT NOT NULL,
                PRIMARY KEY (title, jobId, item),
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
            CREATE TABLE SalaryDAO (
                min REAL NOT NULL,
                max REAL NOT NULL,
                unit TEXT NOT NULL,
                originalJson TEXT NOT NULL,
                jobId TEXT PRIMARY KEY,
                FOREIGN KEY (jobId) REFERENCES JobDAO(jobId)
            )
            """.trimIndent()

        @JvmStatic
        val excelTestData = Stream.of(
            arguments(
                15.0,
                25.0,
                "hourly",
                "Software Engineer",
                "Test Company",
                "Test Location",
                LocalDateTime.now(),
                "15.0-25.0 an hour",
                "Test JobId",
            ),
            arguments(
                600,
                900,
                "weekly",
                "Software Engineer",
                "Test Company",
                "Test Location",
                LocalDateTime.now(),
                "600.0-900.0 a week",
                "Test JobId",
            ),
            arguments(
                2400,
                3600,
                "monthly",
                "Software Engineer",
                "Test Company",
                "Test Location",
                LocalDateTime.now(),
                "2400.0-3600.0 a month",
                "Test JobId",
            ),
            arguments(
                90000,
                135000,
                "yearly",
                "Software Engineer",
                "Test Company",
                "Test Location",
                LocalDateTime.now(),
                "90000.0-135000.0 a year",
                "Test JobId",
            ),
            arguments(
                2400,
                -1.0,
                "monthly",
                "Software Engineer",
                "Test Company",
                "Test Location",
                LocalDateTime.now(),
                "2400.0 a month",
                "Test JobId",
            ),
            arguments(
                -1.0,
                -1.0,
                "N/A",
                "Software Engineer",
                "Test Company",
                "Test Location",
                LocalDateTime.now(),
                null,
                "Test JobId",
            ),
            arguments(
                90000,
                135000,
                "yearly",
                "Software Engineer",
                "Test Company",
                null,
                LocalDateTime.now(),
                "90000.0-135000.0 a year",
                "Test JobId",
            ),
            arguments(
                90000,
                135000,
                "yearly",
                "Software Engineer",
                "Test Company",
                "Test Location",
                null,
                "90000.0-135000.0 a year",
                "Test JobId",
            ),
        )
    }

    private class JobTestDataProvider : ArgumentsProvider {
        private val jsonTestData: MutableList<Path> = Path(dotenv["JOBSPROJ_TEST_DIR"])
            .listDirectoryEntries("*.json")
//            .listDirectoryEntries("healthcare_recruiter_nh-20240131000808-0.json")
            .toMutableList()

        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return jsonTestData.stream()
                .map {
                    named(
                        it.name,
                        moshi.adapter<JobSearchResult>().fromJson(it.readText())!!,
                    )
                }.map(Arguments::of)
        }
    }
}
