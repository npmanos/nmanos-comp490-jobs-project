package edu.bridgew.comp490.proj1.data.entities

import com.kotlinspirit.core.ParseException
import com.kotlinspirit.core.Rule
import com.kotlinspirit.core.Rules.caseInsensitiveOneOf
import com.kotlinspirit.core.Rules.char
import com.kotlinspirit.core.Rules.double
import com.kotlinspirit.grammar.Grammar
import edu.bridgew.comp490.proj1.data.SalaryUnit
import edu.bridgew.comp490.proj1.data.Wage
import edu.bridgew.comp490.proj1.equalsDelta
import edu.bridgew.comp490.proj1.foldThousands
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import java.io.Serializable
import java.time.LocalDateTime

sealed class Extension(open val extType: String) : Serializable {
    companion object {
        @JvmStatic
        fun getById(extType: String, value: String): Extension {
            return when (extType) {
                "schedule" -> ScheduleType(value)
                "posted_at" -> PostedAt(LocalDateTime.parse(value))
                "salary" -> Salary.parse(value)
                "work_from_home" -> WorkFromHome(value == "true")
                else -> UnknownExtension(extType, value)
            }
        }
    }
}

data class ScheduleType(val type: String) : Extension("schedule")
data class PostedAt(val date: LocalDateTime) : Extension("posted_at")
data class Salary(val min: Measure<Wage>, val max: Measure<Wage>, val unit: SalaryUnit, val originalJson: String) : Extension("salary") {
    companion object {
        @JvmStatic
        fun parse(jsonStr: String): Salary {
            val parser = salaryRule.compile(debug = true)
            try {
                return parser.parseGetResultOrThrow(jsonStr)
            } catch (e: ParseException) {
                println(parser.getDebugTree())
                println(jsonStr)
                throw e
            }
        }

        @JvmStatic
        private val salaryRule = object : Grammar<Salary>() {
            private var minSalary = -1.0
            private var maxSalary = -1.0
            private var perUnit: CharSequence = ""
            private var originalJson: CharSequence = ""

            override fun resetResult() {
                minSalary = -1.0
                maxSalary = -1.0
                perUnit = ""
                originalJson = ""
            }

            override fun defineRule(): Rule<*> {
                val digits = (double % ',')
                val multiplier = char('k', 'K')
                val wagePeriod = caseInsensitiveOneOf(
                    "hour",
                    "week",
                    "month",
                    "year",
                    "hourly",
                    "weekly",
                    "monthly",
                    "yearly",
                )

                return (
                    digits { minSalary = it.foldThousands() } +
                        -multiplier { minSalary *= 1000 } +
                        -char('-', '–', '—') +
                        -digits { maxSalary = it.foldThousands() } +
                        -multiplier { maxSalary *= 1000 } +
                        ' ' + -char('a') + -char('n') + -char(' ') +
                        wagePeriod { perUnit = it }
                    ) { originalJson = it }
            }

            override val result: Salary
                get() {
                    val salaryUnit = when (perUnit) {
                        "hour", "hourly" -> SalaryUnit.Hourly
                        "week", "weekly" -> SalaryUnit.Weekly
                        "month", "monthly" -> SalaryUnit.Monthly
                        "year", "yearly" -> SalaryUnit.Yearly
                        else -> throw IllegalArgumentException()
                    }

                    mapOf(
                        "hourly" to "an hour",
                        "weekly" to "a week",
                        "monthly" to "a month",
                        "yearly" to "a year",
                    ).forEach { (oldUnit, newUnit) ->
                        if (originalJson.endsWith(oldUnit)) {
                            originalJson = originalJson.removeSuffix(oldUnit).toString() + newUnit
                        }
                    }

                    val min = minSalary * salaryUnit.unit
                    val max = if (maxSalary == -1.0) min else maxSalary * salaryUnit.unit

                    return Salary(min, max, salaryUnit, originalJson.toString()).apply { resetResult() }
                }
        }.toRule()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Salary

        if (!min.amount.equalsDelta(other.min.amount)) return false
        if (!max.amount.equalsDelta(other.max.amount)) return false
        if (unit != other.unit) return false
        if (originalJson != other.originalJson) return false

        return true
    }

    override fun hashCode(): Int {
        var result = min.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + originalJson.hashCode()
        return result
    }
}
data class WorkFromHome(val isWFH: Boolean) : Extension("work_from_home")
data class UnknownExtension(override val extType: String, val value: String) : Extension(extType)
