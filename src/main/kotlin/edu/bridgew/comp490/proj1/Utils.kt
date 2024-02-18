package edu.bridgew.comp490.proj1

import app.cash.sqldelight.ExecutableQuery
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.ocpsoft.prettytime.PrettyTime
import java.time.LocalDateTime

val LocalDateTime.relativeTimeString: String
    get() = PrettyTime().format(this)

fun <C : Collection<*>> C.nullIfEmpty() = this.ifEmpty { null }

fun <RowType : Any> ExecutableQuery<RowType>.executeAsListOrNull() = this.executeAsList().nullIfEmpty()

operator fun XSSFWorkbook.get(index: Int): XSSFSheet = this.getSheetAt(index)
operator fun XSSFWorkbook.get(name: String): XSSFSheet? = this.getSheet(name)
operator fun XSSFSheet.get(rowIndex: Int): XSSFRow? = this.getRow(rowIndex)
operator fun XSSFRow.get(columnIndex: Int): XSSFCell? = this.getCell(columnIndex)
operator fun XSSFSheet.get(rowIndex: Int, columnIndex: Int): XSSFCell? = this[rowIndex]?.get(columnIndex)
