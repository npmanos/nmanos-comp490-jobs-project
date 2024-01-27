package edu.bridgew.comp430.proj1.api.data

import java.net.URI

data class Job(
    val title: String,
    val companyName: String,
    val location: String,
    val description: String,
    val jobHighlights: List<JobHighlight>,
    val relatedLinks: List<Link>,
    val thumbnail: URI,
    val extensions: List<String>,
    val detectedExtensions: List<String>
)
