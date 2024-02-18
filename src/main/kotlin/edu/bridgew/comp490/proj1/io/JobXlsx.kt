package edu.bridgew.comp490.proj1.io

import edu.bridgew.comp490.proj1.data.JobRepository
import edu.bridgew.comp490.proj1.get
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import java.time.LocalDateTime
import java.time.ZoneOffset

class JobXlsx(private val xlsx: Workbook) : Iterable<JobXlsxRow> {
    private val sheet = requireNotNull(xlsx["Comp490 Jobs"]) { "ERROR! Excel file is missing sheet named \"Comp490 Jobs\"" }

    init {
        requireNotNull(sheet[0]) { "ERROR! \"Comp490 Jobs\" sheet is empty" }

        for (i in 0 until 10) {
            val colHeader = requireNotNull(sheet[0, i]?.stringCellValue) { "ERROR! Missing \"${headerNames[i]}\" column" }
            require(colHeader == headerNames[i])
        }
    }

    override fun iterator(): Iterator<JobXlsxRow> = object : AbstractIterator<JobXlsxRow>() {
        private val rowIterator = sheet.rowIterator().also { it.next() } // Skip the header row

        override fun computeNext() {
            if (rowIterator.hasNext()) {
                setNext(JobXlsxRowImpl(rowIterator.next()))
            } else {
                done()
            }
        }

    }

    private companion object {
        @JvmStatic
        private val headerNames = listOf("Company Name", "Posting Age", "Job Id", "Country", "Location", "Publication Date", "Salary Max", "Salary Min", "Salary Type", "Job Title")
        const val COMPANY_NAME = 0
        const val POSTING_AGE = 1
        const val JOB_ID = 2
        const val COUNTRY = 3
        const val LOCATION = 4
        const val PUB_DATE = 5
        const val SALARY_MAX = 6
        const val SALARY_MIN = 7
        const val SALARY_TYPE = 8
        const val JOB_TITLE = 9
    }

    private class JobXlsxRowImpl(private val row: Row) : JobXlsxRow {
        override val title by lazy { requireNotNull(row[JOB_TITLE]?.stringCellValue) { nullColumnError("Job Title") } }
        override val companyName by lazy { requireNotNull(row[COMPANY_NAME]?.stringCellValue) { nullColumnError("Company Name") } }
        override val location by lazy { row[LOCATION]?.stringCellValue }

        override val postedAt by lazy {
            row[PUB_DATE]?.let {
                LocalDateTime.ofEpochSecond(
                    it.numericCellValue.toLong() / 1000,
                    (it.numericCellValue.toLong() % 1000).toInt(),
                    ZoneOffset.UTC
                )
            }
        }

        override val salaryMin by lazy { row[SALARY_MIN]?.numericCellValue?.toString() ?: "" }
        override val salaryMax by lazy { row[SALARY_MAX]?.let { if (it.numericCellValue == -1.0) "" else "-${it.numericCellValue}" } ?: "" }
        override val salaryType by lazy { row[SALARY_TYPE]?.stringCellValue }

        override val jobId by lazy { requireNotNull(row[JOB_ID]?.stringCellValue) { nullColumnError("Job Id") } }

        override val postingAge by lazy { row[POSTING_AGE]?.stringCellValue }
        override val country by lazy { row[COUNTRY]?.stringCellValue }

        private fun nullColumnError(columnTitle: String) = "ERROR! $columnTitle is missing in row ${row.rowNum + 1}"
    }
}
