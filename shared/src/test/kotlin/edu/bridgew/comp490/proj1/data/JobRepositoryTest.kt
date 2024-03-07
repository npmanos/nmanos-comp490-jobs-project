package edu.bridgew.comp490.proj1.data

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import edu.bridgew.comp490.proj1.data.Currency.Companion.dollars
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.hour
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.month
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.week
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.year
import edu.bridgew.comp490.proj1.data.db.JobSearchDB
import edu.bridgew.comp490.proj1.data.db.SalaryDAO
import edu.bridgew.comp490.proj1.data.entities.Extension
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.JobSearchResult
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.data.entities.Salary
import edu.bridgew.comp490.proj1.data.entities.WorkFromHome
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
import io.nacular.measured.units.div
import io.nacular.measured.units.times
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
            jobRepository.searchAndGetJobs("UNIT_TEST")
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

        jobRepository.saveJobsFromExcel("UNIT_TEST", xlsx)

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

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(JobTestDataProvider::class)
    fun `test text search filter`(testJobSearchResult: JobSearchResult) {
        val testData = testJobSearchResult.jobsResults!!

        testData.forEach { jobRepository.upsertJob("UNIT_TEST", it) }

        val expectedJava = testData
            .filter { it.title.lowercase().contains("java") || it.description?.lowercase()?.contains("java") ?: false }
        val actualJava = jobRepository.getFilteredShortJobs("java")
        assertEquals(expectedJava.size, actualJava.size, "Different number of java results")
        val actualJavaIds = actualJava.map { it.jobId }
        expectedJava.forEach { assertContains(actualJavaIds, it.jobId, "Missing java job ID") }

        val expectedPython = testData
            .filter { it.title.lowercase().contains("python") || it.description?.lowercase()?.contains("python") ?: false }
        val actualPython = jobRepository.getFilteredShortJobs("python")
        assertEquals(expectedPython.size, actualPython.size, "Different number of python results")
        val actualPythonIds = actualPython.map { it.jobId }
        expectedPython.forEach { assertContains(actualPythonIds, it.jobId, "Missing python job ID") }

        val expectedFooBar = testData
            .filter { it.title.lowercase().contains("foo bar") || it.description?.lowercase()?.contains("foo bar") ?: false }
        val actualFooBar = jobRepository.getFilteredShortJobs("foo bar")
        assertEquals(expectedFooBar.size, actualFooBar.size, "Different number of foo bar results")
        val actualFooBarIds = actualFooBar.map { it.jobId }
        expectedFooBar.forEach { assertContains(actualFooBarIds, it.jobId, "Missing foo bar job ID") }

        val actualFull = jobRepository.getFilteredShortJobs("")
        assertEquals(testData.size, actualFull.size, "Different number of unfiltered results")
        val actualFullIds = actualFull.map { it.jobId }
        testData.forEach { assertContains(actualFullIds, it.jobId, "Missing unfiltered job ID") }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(JobTestDataProvider::class)
    fun `test work from home filter`(testJobSearchResult: JobSearchResult) {
        val testData = testJobSearchResult.jobsResults!!

        testData.forEach { jobRepository.upsertJob("UNIT_TEST", it) }

        val actualUnfiltered = jobRepository.getFilteredShortJobs("", null)
        assertEquals(testData.size, actualUnfiltered.size, "Different number of unfiltered results")
        val actualUnfilteredIds = actualUnfiltered.map { it.jobId }
        testData.forEach { assertContains(actualUnfilteredIds, it.jobId, "Missing unfiltered job ID") }

        val expectedTrue = testData
            .filter { (it.detectedExtensions?.firstOrNull { it is WorkFromHome } as WorkFromHome?)?.isWFH ?: false }
        val actualTrue = jobRepository.getFilteredShortJobs("", true)
        assertEquals(expectedTrue.size, actualTrue.size, "Different number of true results")
        val actualTrueIds = actualTrue.map { it.jobId }
        expectedTrue.forEach { assertContains(actualTrueIds, it.jobId, "Missing true job ID") }

        val expectedFalse = testData
            .filter { !((it.detectedExtensions?.firstOrNull { it is WorkFromHome } as WorkFromHome?)?.isWFH ?: true) }
        val actualFalse = jobRepository.getFilteredShortJobs("", false)
        assertEquals(expectedFalse.size, actualFalse.size, "Different number of false results")
        val actualFalseIds = actualFalse.map { it.jobId }
        expectedFalse.forEach { assertContains(actualFalseIds, it.jobId, "Missing false job ID") }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(JobTestDataProvider::class)
    fun `test unique location retrieval`(testJobSearchResult: JobSearchResult) {
        val testData = testJobSearchResult.jobsResults!!

        testData.forEach { jobRepository.upsertJob("UNIT_TEST", it) }

        val expectedLocations = testData.mapNotNull { it.location }.distinct()
        val actualLocations = jobRepository.getLocations()
        assertEquals(expectedLocations.size, actualLocations.size, "Different number of locations")
        actualLocations.forEach { assertContains(expectedLocations, it, "Missing location") }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(JobTestDataProvider::class)
    fun `test location filter`(testJobSearchResult: JobSearchResult) {
        val testData = testJobSearchResult.jobsResults!!

        testData.forEach { jobRepository.upsertJob("UNIT_TEST", it) }
        val locations = testData.mapNotNull { it.location }.distinct()

        locations.forEach { location ->
            val expectedJobs = testData.filter { it.location == location }
            val actualJobs = jobRepository.getFilteredShortJobs("", locationFilterEnabled = true, selectedLocations = listOf(location))
            assertEquals(expectedJobs.size, actualJobs.size, "Different number of $location jobs")
            val expectedJobIds = expectedJobs.map { it.jobId }
            actualJobs.forEach { assertContains(expectedJobIds, it.jobId, "Missing $location job id") }
        }

        val expectedAllJobIds = testData.map { it.jobId }

        val actualFilterDisabled = jobRepository.getFilteredShortJobs("", locationFilterEnabled = false, selectedLocations = locations)
        assertEquals(testData.size, actualFilterDisabled.size, "Different number of disabled jobs")
        actualFilterDisabled.forEach { assertContains(expectedAllJobIds, it.jobId, "Missing disabled job id") }

        val actualEmptyList = jobRepository.getFilteredShortJobs("", locationFilterEnabled = true, selectedLocations = listOf())
        assertEquals(testData.size, actualEmptyList.size, "Different number of empty list jobs")
        actualEmptyList.forEach { assertContains(expectedAllJobIds, it.jobId, "Missing empty list job id") }

        val actualNull = jobRepository.getFilteredShortJobs("", locationFilterEnabled = true, selectedLocations = null)
        assertEquals(testData.size, actualNull.size, "Different number of null jobs")
        actualNull.forEach { assertContains(expectedAllJobIds, it.jobId, "Missing null job id") }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(JobTestDataProvider::class)
    fun `test minimum salary filter`(testJobSearchResult: JobSearchResult) {
        val testData = testJobSearchResult.jobsResults!!

        testData.forEach { jobRepository.upsertJob("UNIT_TEST", it) }

        val salaries = listOf(30 * dollars / hour, 1200 * dollars / week, 5200 * dollars / month, 62_400 * dollars / year)
        salaries.forEach { salary ->
            val expectedSalaryJobs = testData
                .filter { ((it.detectedExtensions?.firstOrNull { it is Salary } as Salary?)?.min ?: (-1 * dollars / hour)) >= salary }
            val actualSalaryJobs = jobRepository.getFilteredShortJobs("", salaryFilterEnabled = true, minSalary = salary)
            assertEquals(expectedSalaryJobs.size, actualSalaryJobs.size, "Different number of jobs for $salary")
            val expectedSalaryIds = expectedSalaryJobs.map { it.jobId }
            expectedSalaryJobs.forEach { assertContains(expectedSalaryIds, it.jobId, "Missing job id for $salary") }
        }

        val expectedAllJobIds = testData.map { it.jobId }

        val actualFilterDisabled = jobRepository.getFilteredShortJobs("", salaryFilterEnabled = false, minSalary = 30 * dollars / hour)
        assertEquals(testData.size, actualFilterDisabled.size)
        actualFilterDisabled.forEach { assertContains(expectedAllJobIds, it.jobId, "Different number of disabled jobs") }

        val actualNull = jobRepository.getFilteredShortJobs("", salaryFilterEnabled = true, minSalary = null)
        assertEquals(testData.size, actualNull.size)
        actualNull.forEach { assertContains(expectedAllJobIds, it.jobId, "Different number of disabled jobs") }
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
