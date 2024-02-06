package edu.bridgew.comp490.proj1

import app.cash.sqldelight.ExecutableQuery
import org.ocpsoft.prettytime.PrettyTime
import java.time.LocalDateTime

val LocalDateTime.relativeTimeString: String
    get() = PrettyTime().format(this)

fun <C : Collection<*>> C.nullIfEmpty() = this.ifEmpty { null }

fun <RowType : Any> ExecutableQuery<RowType>.executeAsListOrNull() = this.executeAsList().nullIfEmpty()
