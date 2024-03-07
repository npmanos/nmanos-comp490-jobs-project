package edu.bridgew.comp490.proj1.data

import edu.bridgew.comp490.proj1.data.Currency.Companion.dollars
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.hour
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.month
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.week
import edu.bridgew.comp490.proj1.data.WagePeriod.Companion.year
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Units
import io.nacular.measured.units.UnitsRatio
import io.nacular.measured.units.div
import io.nacular.measured.units.times

class Currency private constructor(ratio: Double) : Units("", ratio) {
    override val spaceBetweenMagnitude = false

    companion object {
        val dollars = Currency(1.0)
    }
}

class WagePeriod private constructor(ratio: Double, val guiSuffix: String) : Units(guiSuffix, ratio) {
    companion object {
        val hour = WagePeriod(1.0, "hour")
        val day = WagePeriod(8.0, "day")
        val week = WagePeriod(5 * day.ratio, "week")
        val month = WagePeriod((52 * week.ratio) / 12.0, "month")
        val year = WagePeriod(52 * week.ratio, "year")
    }
}

typealias Wage = UnitsRatio<Currency, WagePeriod>

fun Number.hourly(): Measure<Wage> = this * dollars / hour
fun Measure<Wage>.hourly() = this `as` dollars / hour

fun Number.weekly(): Measure<Wage> = this * dollars / week
fun Measure<Wage>.weekly() = this `as` dollars / week

fun Number.monthly(): Measure<Wage> = this * dollars / month
fun Measure<Wage>.monthly() = this `as` dollars / month

fun Number.yearly(): Measure<Wage> = this * dollars / year
fun Measure<Wage>.yearly() = this `as` dollars / year

enum class SalaryUnit(val unit: Wage) {
    Hourly(dollars / hour),
    Weekly(dollars / week),
    Monthly(dollars / month),
    Yearly(dollars / year),
}
