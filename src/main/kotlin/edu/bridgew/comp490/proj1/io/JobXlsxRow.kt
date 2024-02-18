package edu.bridgew.comp490.proj1.io

import java.time.LocalDateTime

interface JobXlsxRow {
    val title: String
    val companyName: String
    val location: String?
    val postedAt: LocalDateTime?
    val salaryMin: String?
    val salaryMax: String?
    val salaryType: String?
    val jobId: String

    val postingAge: String?
    val country: String?
}
