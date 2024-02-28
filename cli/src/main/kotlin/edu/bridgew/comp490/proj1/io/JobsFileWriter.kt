package edu.bridgew.comp490.proj1.io

import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.data.entities.Salary
import edu.bridgew.comp490.proj1.data.entities.ScheduleType
import edu.bridgew.comp490.proj1.data.entities.UnknownExtension
import edu.bridgew.comp490.proj1.data.entities.WorkFromHome
import edu.bridgew.comp490.proj1.relativeTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.FileSystem
import okio.Path
import okio.buffer
import java.io.Flushable

/**
 * Class for writing job data to a file.
 *
 * This class provides methods to write job data to a file. The job data is written in a specific format.
 *
 * @param path The file where the job data will be written as a [Path].
 */
class JobsFileWriter(path: Path) : AutoCloseable, Flushable {
    private val buffer: BufferedSink
    private val newline = "\n"

    /**
     * Indicates whether the buffer is open or not.
     */
    val isOpen: Boolean
        get() = buffer.isOpen

    init {
        val fileSink = FileSystem.SYSTEM.sink(path)
        buffer = fileSink.buffer()
    }

    /**
     * Writes a job to the file.
     *
     * This method writes the details of a job to the file. The job details include the job title, company name, location, salary, posting date, highlights, and description.
     *
     * @param job The job to be written to the file.
     */
    suspend fun writeJob(job: Job): Unit = withContext(Dispatchers.IO) {
        var scheduleType: ScheduleType? = null
        var postedAt: PostedAt? = null
        var salary: Salary? = null
        var wfh: WorkFromHome? = null
        val otherExtensions = mutableListOf<UnknownExtension>()

        job.detectedExtensions?.forEach { extension ->
            when (extension) {
                is ScheduleType -> scheduleType = extension
                is PostedAt -> postedAt = extension
                is Salary -> salary = extension
                is WorkFromHome -> wfh = extension
                is UnknownExtension -> otherExtensions.add(extension)
            }
        }

        // Header
        writeHeader(job, scheduleType, wfh)

        // Location
        if (job.location != null) {
            buffer.writeLnUtf8(job.location!!.trimStart())
        }

        // Salary
        if (salary != null) {
            buffer.writeUtf8("Salary: ")
            buffer.writeLnUtf8(salary!!.salaryRange)
        }

        // Posted at
        if (postedAt != null) {
            buffer.writeUtf8("Posted ")
            buffer.writeLnUtf8(postedAt!!.date.relativeTimeString)
        }
        buffer.writeLnUtf8()

        // Highlights
        writeHighlights(job)

        // Job Description
        if (job.description != null) {
            buffer.writeLnUtf8("Description")
            buffer.writeLnUtf8(job.description!!)
        }

        // Two line separator (between jobs)
        buffer.writeLnUtf8()
        buffer.writeLnUtf8()

        buffer.flush()
    }

    private fun writeHighlights(job: Job) {
        job.jobHighlights?.forEach { highlight ->
            if (highlight.title != null) {
                buffer.writeLnUtf8(highlight.title!!)
            }

            highlight.items.forEach {
                buffer.writeUtf8(" - ")
                buffer.writeLnUtf8(it)
            }

            buffer.writeLnUtf8()
        }
    }

    private fun writeHeader(
        job: Job,
        scheduleType: ScheduleType?,
        wfh: WorkFromHome?,
    ) {
        // Job Header
        buffer.writeUtf8(job.title)
        buffer.writeUtf8(" - ")
        buffer.writeUtf8(job.companyName)
        if (scheduleType != null || wfh != null) {
            buffer.writeUtf8(" (")

            if (scheduleType != null) {
                buffer.writeUtf8(scheduleType.type)
            }

            if (scheduleType != null && wfh != null) {
                buffer.writeUtf8("/")
            }

            if (wfh != null && wfh.isWFH) {
                buffer.writeUtf8("WFH")
            }

            buffer.writeUtf8(")")
        }
        buffer.writeLnUtf8()
    }

    override fun flush() {
        buffer.flush()
    }

    override fun close() {
        flush()
        buffer.close()
    }

    private fun BufferedSink.writeLnUtf8(string: String) {
        this.writeUtf8(string)
        this.writeUtf8(newline)
    }

    private fun BufferedSink.writeLnUtf8() = this.writeUtf8(newline)
}
