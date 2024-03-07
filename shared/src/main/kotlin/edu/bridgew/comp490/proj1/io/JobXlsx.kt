package edu.bridgew.comp490.proj1.io

import edu.bridgew.comp490.proj1.get
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.COMPANY_NAME
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.COUNTRY
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.JOB_ID
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.JOB_TITLE
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.LOCATION
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.POSTING_AGE
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.PUB_DATE
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.SALARY_MAX
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.SALARY_MIN
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.SALARY_TYPE
import edu.bridgew.comp490.proj1.io.JobXlsxHeader.headerNames
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Class for representing a Job Excel file.
 *
 * The Excel file must contain a sheet named `"Comp490 Jobs"` and must contain the following fields in this order:
 *
 * | Company Name | Posting Age | Job Id | Country | Location | Publication Date | Salary Max | Salary Min | Salary Type | Job Title |
 * |:------------:|:-----------:|:------:|:-------:|:--------:|:----------------:|:----------:|:----------:|:-----------:|:---------:|
 *
 * This class provides an iterator over the rows in the `"Comp490 Jobs"` sheet of the provided Excel workbook.
 * Each row is represented as a [JobXlsxRow] instance.
 *
 * @param xlsx The [Excel workbook][Workbook] containing the job data.
 *
 * @see XSSFWorkbook
 * @see HSSFWorkbook
 * @see SXSSFWorkbook
 */
class JobXlsx(xlsx: Workbook) : Iterable<JobXlsxRow> {
    private val sheet = requireNotNull(xlsx["Comp490 Jobs"]) { "ERROR! Excel file is missing sheet named \"Comp490 Jobs\"" }

    constructor(xlsxFile: File) : this(XSSFWorkbook(xlsxFile.inputStream()))

    init {
        requireNotNull(sheet[0]) { "ERROR! \"Comp490 Jobs\" sheet is empty" }

        for (i in 0 until 10) {
            val colHeader = requireNotNull(sheet[0, i]?.stringCellValue) { "ERROR! Missing \"${headerNames[i]}\" column" }
            require(colHeader == headerNames[i]) { "ERROR! Column ${i + 1} was expected to be \"${headerNames[i]}\" but was actually $colHeader" }
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
                ZoneOffset.UTC,
            )
        }
    }

    override val salaryMin by lazy { row[SALARY_MIN]?.numericCellValue ?: -1.0 }
    override val salaryMax by lazy { row[SALARY_MAX]?.numericCellValue ?: -1.0 }
    override val salaryType by lazy { (row[SALARY_TYPE]?.stringCellValue) }

    override val jobId by lazy { requireNotNull(row[JOB_ID]?.stringCellValue) { nullColumnError("Job Id") } }

    override val postingAge by lazy { row[POSTING_AGE]?.stringCellValue }
    override val country by lazy { row[COUNTRY]?.stringCellValue }

    private fun nullColumnError(columnTitle: String) = "ERROR! $columnTitle is missing in row ${row.rowNum + 1}"
}

private object JobXlsxHeader {
    @JvmStatic
    val headerNames = listOf("Company Name", "Posting Age", "Job Id", "Country", "Location", "Publication Date", "Salary Max", "Salary Min", "Salary Type", "Job Title")
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
