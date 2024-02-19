package edu.bridgew.comp490.proj1.io

import io.mockk.junit5.MockKExtension
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.extension.ExtendWith
import java.io.FileInputStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class JobXlsxTest {
    lateinit var workbook: XSSFWorkbook

    @BeforeTest
    fun setup() {
        workbook = XSSFWorkbook(FileInputStream("data/Sprint3Data.xlsx"))
    }

    @Test
    fun `reads at least 300 rows`() {
        val jobXlsx = JobXlsx(workbook)

        assertTrue(jobXlsx.count() >= 300)
    }
}
