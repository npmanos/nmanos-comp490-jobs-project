package edu.bridgew.comp490.proj1

import app.cash.sqldelight.ExecutableQuery
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.ocpsoft.prettytime.PrettyTime
import java.time.LocalDateTime

val LocalDateTime.relativeTimeString: String
    get() = PrettyTime().format(this)

fun <C : Collection<*>> C.nullIfEmpty() = this.ifEmpty { null }

fun <RowType : Any> ExecutableQuery<RowType>.executeAsListOrNull() = this.executeAsList().nullIfEmpty()

operator fun Workbook.get(index: Int): Sheet = this.getSheetAt(index)
operator fun Workbook.get(name: String): Sheet? = this.getSheet(name)
operator fun Sheet.get(rowIndex: Int): Row? = this.getRow(rowIndex)
operator fun Row.get(columnIndex: Int): Cell? = this.getCell(columnIndex)
operator fun Sheet.get(rowIndex: Int, columnIndex: Int): Cell? = this[rowIndex]?.get(columnIndex)
