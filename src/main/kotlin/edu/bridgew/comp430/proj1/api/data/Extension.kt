package edu.bridgew.comp430.proj1.api.data

import java.util.Date

sealed class Extension(val value: Any)

class ScheduleType(value: String) : Extension(value)
class PostedAt(value: String) : Extension(value) //todo: make this a date
class Salary(value: String) : Extension(value)

