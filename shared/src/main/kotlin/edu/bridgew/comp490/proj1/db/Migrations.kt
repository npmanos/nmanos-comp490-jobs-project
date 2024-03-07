package edu.bridgew.comp490.proj1.db

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import edu.bridgew.comp490.proj1.data.Currency.Companion.dollars
import edu.bridgew.comp490.proj1.data.SalaryUnit
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.hour
import edu.bridgew.comp490.proj1.data.db.DetectedExtensionDAO
import edu.bridgew.comp490.proj1.data.entities.Salary
import io.nacular.measured.units.div

internal object Migrations {
    object After3 {
        @JvmStatic
        fun onMigration(driver: SqlDriver) {
            val enumAdapter = EnumColumnAdapter<SalaryUnit>()

            Query(
                333_444,
                driver,
                """
                SELECT *
                FROM DetectedExtensionDAO
                WHERE extType = 'salary'
                """.trimIndent(),
            ) { cursor ->
                DetectedExtensionDAO(
                    cursor.getString(0)!!,
                    cursor.getString(1)!!,
                    cursor.getString(2)!!,
                )
            }.executeAsList().forEach {
                val salary = Salary.parse(it.value_)
                val rowsInserted = driver.execute(
                    null,
                    """
                    |INSERT INTO SalaryDAO(min, max, unit, originalJson, jobId)
                    |VALUES (?, ?, ?, ?, ?)
                    """.trimMargin(),
                    5,
                ) {
                    bindDouble(0, salary.min `in` dollars / hour)
                    bindDouble(1, salary.max `in` dollars / hour)
                    bindString(2, enumAdapter.encode(salary.unit))
                    bindString(3, salary.originalJson)
                    bindString(4, it.jobId)
                }

                require(rowsInserted.value == 1L)

                val rowsDeleted = driver.execute(
                    null,
                    """
                    DELETE FROM DetectedExtensionDAO
                    WHERE jobId = ? AND extType = 'salary'
                    """.trimIndent(),
                    1,
                ) {
                    bindString(0, it.jobId)
                }

                require(rowsDeleted.value == 1L)
            }
        }
    }
}
