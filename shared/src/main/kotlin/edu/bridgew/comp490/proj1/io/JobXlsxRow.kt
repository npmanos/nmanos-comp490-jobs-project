package edu.bridgew.comp490.proj1.io

import java.time.LocalDateTime

/**
 * Interface for representing a row in a Job Excel file.
 *
 * This interface defines the properties that a row in a Job Excel file should have.
 * Each property corresponds to a column in the Excel file.
 */
interface JobXlsxRow {
    /**
     * The title of the job.
     */
    val title: String

    /**
     * The name of the company offering the job.
     */
    val companyName: String

    /**
     * The location where the job is based.
     *
     * This can be null if the location is not specified.
     */
    val location: String?

    /**
     * The date and time when the job was posted.
     *
     * This can be null if the posting date is not specified.
     */
    val postedAt: LocalDateTime?

    /**
     * The minimum salary for the job.
     *
     * @see salaryMax
     * @see salaryType
     */
    val salaryMin: Double

    /**
     * The maximum salary for the job.
     *
     * @see salaryMin
     * @see salaryType
     */
    val salaryMax: Double

    /**
     * The pay period for the listed salary.
     *
     * One of:
     *  - `"hourly"`
     *  - `"weekly"`
     *  - `"monthly"`
     *  - `"yearly"`
     *
     * The string is null when the value in the spreadsheet is `"N/A"`.
     */
    val salaryType: String?

    /**
     * The unique identifier for the job.
     */
    val jobId: String

    /**
     * The age of the job posting.
     *
     * This can be null if the posting age is not specified.
     */
    val postingAge: String?

    /**
     * The country where the job is based.
     *
     * This can be null if the country is not specified.
     */
    val country: String?
}
