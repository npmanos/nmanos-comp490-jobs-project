package edu.bridgew.comp430.proj1.api.data

import java.net.URI
import java.time.LocalDateTime

data class SearchMetadata(
    val id: String,
    val status: SearchStatus,
    val jsonEndpoint: URI,
    val createdAt: LocalDateTime,
    val processedAt: LocalDateTime,
    val googleJobsUrl: URI,
    val rawHtmlFile: URI,
    val totalTimeTaken: Double
)
