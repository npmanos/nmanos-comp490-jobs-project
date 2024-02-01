package edu.bridgew.comp490.proj1.io

import edu.bridgew.comp490.proj1.api.data.Job
import edu.bridgew.comp490.proj1.api.data.PostedAt
import edu.bridgew.comp490.proj1.api.data.Salary
import edu.bridgew.comp490.proj1.api.data.ScheduleType
import edu.bridgew.comp490.proj1.api.data.UnknownExtension
import edu.bridgew.comp490.proj1.api.data.WorkFromHome
import edu.bridgew.comp490.proj1.relativeTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.FileSystem
import okio.Path
import okio.buffer

class JobsFileWriter(path: Path) {
    private val buffer: BufferedSink
    private val newline = "\n"
    val open: Boolean
        get() = buffer.isOpen

    init {
        val fileSink = FileSystem.SYSTEM.sink(path)
        buffer = fileSink.buffer()
    }

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
            buffer.writeLnUtf8(job.location.trimStart())
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
            buffer.writeLnUtf8(job.description)
        }

        // Two line separator (between jobs)
        buffer.writeLnUtf8()
        buffer.writeLnUtf8()
    }

    private fun writeHighlights(job: Job) {
        job.jobHighlights?.forEach { highlight ->
            if (highlight.title != null) {
                buffer.writeLnUtf8(highlight.title)
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
        buffer.flush()
    }

    fun close() {
        buffer.flush()
        buffer.close()
    }

    private fun BufferedSink.writeLnUtf8(string: String) {
        this.writeUtf8(string)
        this.writeUtf8(newline)
    }

    private fun BufferedSink.writeLnUtf8() = this.writeUtf8(newline)
}
