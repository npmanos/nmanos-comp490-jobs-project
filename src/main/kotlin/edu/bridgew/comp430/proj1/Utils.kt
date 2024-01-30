package edu.bridgew.comp430.proj1

import org.ocpsoft.prettytime.PrettyTime
import java.time.LocalDateTime

val LocalDateTime.relativeTimeString: String
    get() = PrettyTime().format(this)
