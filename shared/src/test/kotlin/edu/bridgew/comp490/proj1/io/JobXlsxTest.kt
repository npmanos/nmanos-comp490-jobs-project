package edu.bridgew.comp490.proj1.io

import io.mockk.junit5.MockKExtension
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.extension.ExtendWith
import java.io.FileInputStream
import java.time.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class JobXlsxTest {
    lateinit var workbook: XSSFWorkbook

    @BeforeTest
    fun setup() {
        workbook = XSSFWorkbook(FileInputStream("../data/Sprint3Data.xlsx"))
    }

    @Test
    fun `reads data from multiple columns`() {
        var readLocation = false
        var readPostedAt = false
        var readSalaryMin = false
        var readSalaryMax = false
        var readSalaryType = false
        var readPostingAge = false
        var readCountry = false

        val jobXlsx = JobXlsx(workbook)

        jobXlsx.forEach {
            assertNotNull(it.title)
            assertTrue { it.title.isNotEmpty() }

            assertNotNull(it.companyName)
            assertTrue { it.companyName.isNotEmpty() }

            assertNotNull(it.jobId)
            assertTrue { it.jobId.isNotEmpty() }

            if (it.location != null) {
                assertTrue { it.location!!.isNotEmpty() }
                readLocation = true
            }

            if (it.postedAt != null) {
                assertIs<LocalDateTime>(it.postedAt)
                readPostedAt = true
            }

            if (it.salaryMax >= -1.0) readSalaryMax = true
            if (it.salaryMin >= -1.0) readSalaryMin = true

            if (it.salaryType != null) {
                assertContains(listOf("N/A", "hourly", "weekly", "monthly", "yearly"), it.salaryType)
                readSalaryType = true
            }

            if (it.postingAge != null) {
                assertTrue { it.postingAge!!.isNotEmpty() }
                readPostingAge = true
            }

            if (it.country != null) {
                assertTrue { it.country!!.isNotEmpty() }
                readCountry = true
            }
        }

        assertTrue(readLocation)
        assertTrue(readPostedAt)
        assertTrue(readSalaryMax)
        assertTrue(readSalaryMin)
        assertTrue(readSalaryType)
        assertTrue(readPostingAge)
        assertTrue(readCountry)
    }

    @Test
    fun `reads at least 300 rows`() {
        val jobXlsx = JobXlsx(workbook)

        assertTrue(jobXlsx.count() >= 300)
    }
}
