package edu.bridgew.comp490.proj1

import app.cash.sqldelight.ExecutableQuery
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.ocpsoft.prettytime.PrettyTime
import java.time.LocalDateTime
import kotlin.math.pow

/**
 * Relative time string representation of this LocalDateTime object.
 *
 * This property uses the PrettyTime library to format the LocalDateTime instance into a relative time string.
 * For example, if the LocalDateTime instance represents a date 5 days ago, the returned string will be `"5 days ago"`.
 */
val LocalDateTime.relativeTimeString: String
    get() = PrettyTime().format(this)

/**
 * @return This collection if it's not empty or `null` if the collection is empty.
 */
fun <C : Collection<*>> C.nullIfEmpty() = ifEmpty { null }

/**
 * @return The result set of the underlying SQL statement as a list of [RowType] or `null` if empty.
 */
fun <RowType : Any> ExecutableQuery<RowType>.executeAsListOrNull() = executeAsList().nullIfEmpty()

/**
 * Get the [Sheet] object at the given index.
 *
 * @param index The index of the sheet number (0-based physical & logical).
 * @return [Sheet] at the provided index.
 *
 * @see Workbook.getSheetAt
 */
operator fun Workbook.get(index: Int): Sheet = getSheetAt(index)

/**
 * Get sheet with the given name
 *
 * @param name The name of the sheet.
 * @return [Sheet] with the name provided or `null` if it does not exist.
 *
 * @see Workbook.getSheet
 */
operator fun Workbook.get(name: String): Sheet? = getSheet(name)

/**
 * Returns the logical row (not physical) 0-based. If you ask for a row that is not defined you get a `null`. This is to
 * say row 4 represents the fifth row on a sheet.
 *
 * @param rowIndex [Row] to get (0-based).
 * @return [Row] representing the row-number or `null` if it's not defined on the sheet.
 *
 * @see Sheet.getRow
 */
operator fun Sheet.get(rowIndex: Int): Row? = getRow(rowIndex)

/**
 * Get the cell representing a given column (logical cell) 0-based. If you ask for a cell that is not defined....you get a `null`.
 *
 * @param columnIndex 0 based column number.
 * @return [Cell] representing that column or `null` if undefined.
 *
 * @see Row.getCell
 */
operator fun Row.get(columnIndex: Int): Cell? = getCell(columnIndex)

/**
 * Get the cell representing a  given column (logical cell) 0-based from the logical row (not physical) 0-based. This is to say
 * row 4 represents the fifth row on a sheet. If you ask for a row or cell that is not defined you get a `null`.
 *
 * @param rowIndex [Row] to get the cell from (0-based)
 * @param columnIndex 0 based column number.
 * @return [Cell] representing that column in that row or `null` if undefined.
 *
 * @see Sheet.getRow
 * @see Row.getCell
 */
operator fun Sheet.get(rowIndex: Int, columnIndex: Int): Cell? = getRow(rowIndex)?.getCell(columnIndex)


fun List<Double>.foldThousands() = foldIndexed(0.0) { idx, acc, next ->
    next * 10.0.pow((size - idx - 1) * 3) + acc
}

fun Boolean.toLong(): Long = if (this) 1 else 0
