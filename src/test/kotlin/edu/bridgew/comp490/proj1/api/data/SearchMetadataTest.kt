package edu.bridgew.comp490.proj1.api.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.addAdapter
import edu.bridgew.comp490.proj1.api.adapters.SearchStatusAdapter
import edu.bridgew.comp490.proj1.api.adapters.ZonedDateTimeAdapter
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalStdlibApi::class)
class SearchMetadataTest : JsonClassTestBase<SearchMetadata>() {
    override lateinit var moshi: Moshi
    override lateinit var adapter: JsonAdapter<SearchMetadata>
    private lateinit var statusAdapter: SearchStatusAdapter
    private lateinit var readerSlot: CapturingSlot<JsonReader>

    @BeforeTest
    override fun setupMoshi() {
        statusAdapter = spyk()
        readerSlot = slot()

        every { statusAdapter.fromJson(reader = capture(readerSlot)) } answers {
            readerSlot.captured.skipValue()
            return@answers StatusSuccess
        }

        moshi = Moshi.Builder()
            .add(ZonedDateTimeAdapter())
            .addAdapter(statusAdapter)
            .build()

        adapter = moshi.adapter<SearchMetadata>().indent("    ")
    }

    private fun buildJson(
        id: String = "65b6c9222f542e7e6d27389c",
        error: String? = null,
        jsonEndpoint: String = "https://serpapi.com/searches/7427a2a1d27a8477/65b6c9222f542e7e6d27389c.json",
        createdAt: String = "2024-01-28 21:37:38 UTC",
        processedAt: String = "2024-01-28 21:37:38 UTC",
        googleJobsUrl: String = "https://www.google.com/search?q=software+engineer&ibp=htl;jobs&start=10",
        rawHtmlFile: String = "https://serpapi.com/searches/7427a2a1d27a8477/65b6c9222f542e7e6d27389c.html",
        totalTimeTaken: String = "1.32",
    ): String {
        return """|{
        |    "id": "$id",
        |    "status": "Success",${if (error != null) "\n\"error\":\"$error\",\n" else ""}
        |    "json_endpoint": "$jsonEndpoint",
        |    "created_at": "$createdAt",
        |    "processed_at": "$processedAt",
        |    "google_jobs_url": "$googleJobsUrl",
        |    "raw_html_file": "$rawHtmlFile",
        |    "total_time_taken": $totalTimeTaken
        |}
        """.trimMargin()
    }

    private fun getJsonAndClass(
        id: String = "65b6c9222f542e7e6d27389c",
        error: String? = null,
        jsonEndpoint: String = "https://serpapi.com/searches/7427a2a1d27a8477/65b6c9222f542e7e6d27389c.json",
        createdAt: String = "2024-01-28 21:37:38 UTC",
        processedAt: String = "2024-01-28 21:37:38 UTC",
        googleJobsUrl: String = "https://www.google.com/search?q=software+engineer&ibp=htl;jobs&start=10",
        rawHtmlFile: String = "https://serpapi.com/searches/7427a2a1d27a8477/65b6c9222f542e7e6d27389c.html",
        totalTimeTaken: String = "1.32",
    ): Pair<String, SearchMetadata> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz")
        val dtCreatedAt = ZonedDateTime.parse(createdAt, formatter)
        val dtProcessedAt = ZonedDateTime.parse(processedAt, formatter)

        return buildJson(id, error, jsonEndpoint, createdAt, processedAt, googleJobsUrl, rawHtmlFile, totalTimeTaken) to SearchMetadata(id, StatusSuccess, error, jsonEndpoint, dtCreatedAt, dtProcessedAt, googleJobsUrl, rawHtmlFile, totalTimeTaken.toDouble())
    }

    @Nested
    @DisplayName("valid SearchMetadata")
    inner class Valid {

        @Test
        fun deserialize() {
            val (json, dataClass) = getJsonAndClass()
            val metadata = adapter.fromJson(json)

            assertEquals(dataClass, metadata)

            verify {
                statusAdapter.fromJson(any<JsonReader>())
            }
        }

        @Test
        fun `deserialize with error`() {
            val (json, dataClass) = getJsonAndClass(error = "foobar")
            val metadata = adapter.fromJson(json)

            assertEquals(dataClass, metadata)

            verify {
                statusAdapter.fromJson(any<JsonReader>())
            }
        }

        @Test
        fun serialize() {
            val (json, dataClass) = getJsonAndClass()
            val serialized = adapter.toJson(dataClass)

            assertEquals(json, serialized)
        }

        @Test
        fun `serialize with error`() {
            val (json, dataClass) = getJsonAndClass()
            val serialized = adapter.toJson(dataClass)

            assertEquals(json, serialized)
        }
    }

    @Nested
    @DisplayName("invalid SearchMetadata")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class Invalid {
        private val missingKeys by lazy {
            listOf(
                "id",
                "status",
                "json_endpoint",
                "created_at",
                "processed_at",
                "google_jobs_url",
                "raw_html_file",
                "total_time_taken",
            )
        }

        @ParameterizedTest(name = "missing {0}")
        @MethodSource("getMissingKeys")
        fun `reject missing metadata`(removeKey: String) {
            val json = Regex("(\\s{4}\"$removeKey\":\\s.+\\s)")
                .replace(buildJson(), "")

            assertFails { adapter.fromJson(json) }
        }

        private val nullKeys by lazy { missingKeys.filterNot { it == "status" } } // UnknownStatus can cope with null

        @ParameterizedTest(name = "null {0}")
        @MethodSource("getNullKeys")
        fun `reject null metadata values`(removeKey: String) {
            val json = Regex("(?<=\\s{4}\"$removeKey\":\\s)([^,\\s]*)")
                .replace(buildJson(), "null")

            assertFails { adapter.fromJson(json) }
        }

        private val badDateTimes by lazy {
            listOf(
                "foobar",
                "2024-01-28T03:55:12Z[UTC]",
                "2024-01-27T22:55:12-05:00[America/New_York]",
                "20240128T035512Z",
                "2024-01-27T22:55:12",
                "2024-01-27",
                "22:55:12",
                "1706414112",
            )
        }

        @ParameterizedTest(name = "timestamp: {0}")
        @MethodSource("getBadDateTimes")
        fun `reject malformed created_at`(badTime: String) {
            val json = buildJson(createdAt = badTime)

            assertFails { adapter.fromJson(json) }
        }

        @ParameterizedTest(name = "timestamp: {0}")
        @MethodSource("getBadDateTimes")
        fun `reject malformed processed_at`(badTime: String) {
            val json = buildJson(processedAt = badTime)

            assertFails { adapter.fromJson(json) }
        }
    }
}
