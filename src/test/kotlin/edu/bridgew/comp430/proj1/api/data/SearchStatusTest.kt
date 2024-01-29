package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.*
import edu.bridgew.comp430.proj1.api.adapters.SearchStatusAdapter
//import edu.bridgew.comp430.proj1.api.adapters.SearchStatusAdapter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class SearchStatusTest : JsonClassTestBase() {
    override lateinit var moshi: Moshi
    private lateinit var adapter: JsonAdapter<Container>

    @JsonClass(generateAdapter = true)
    internal data class Container(val status: SearchStatus)

    @BeforeTest
    override fun setupMoshi() {
        moshi = Moshi.Builder()
            .addAdapter(SearchStatusAdapter())
            .build()

        adapter = moshi.adapter()
    }

    @Nested
    @DisplayName("StatusProcessing tests")
    inner class Processing {
        private val json = "{\"status\":\"Processing\"}"

        @Test
        fun `deserialize StatusProcessing`() {
            val container = adapter.fromJson(json)

            assertNotNull(container)
            assertIs<StatusProcessing>(container.status)
            assertSame(StatusProcessing, container.status)
        }

        @Test
        fun `StatusProcessing serialize test`() {
            val container = Container(StatusProcessing)
            val serializedContainer = adapter.toJson(container)

            assertEquals(json, serializedContainer)
        }
    }

    @Nested
    @DisplayName("StatusSuccess tests")
    inner class Success {
        private val json = "{\"status\":\"Success\"}"

        @Test
        fun `deserialize StatusSuccess`() {
            val container = adapter.fromJson(json)

            assertNotNull(container)
            assertIs<StatusSuccess>(container.status)
            assertSame(StatusSuccess, container.status)
        }

        @Test
        fun `StatusSuccess serialize test`() {
            val container = Container(StatusSuccess)
            val serializedContainer = adapter.toJson(container)

            assertEquals(json, serializedContainer)
        }
    }

    @Nested
    @DisplayName("StatusError tests")
    inner class Error {
        private val json = "{\"status\":\"Error\"}"

        @Test
        fun `deserialize StatusError`() {
            val container = adapter.fromJson(json)

            assertNotNull(container)
            assertIs<StatusError>(container.status)
            assertSame(StatusError, container.status)
        }

        @Test
        fun `StatusError serialize test`() {
            val container = Container(StatusError)
            val serializedContainer = adapter.toJson(container)

            assertEquals(json, serializedContainer)
        }
    }

    @Nested
    @DisplayName("StatusUnknown tests")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class Unknown {
        private val statusValues by lazy {
            arrayOf(
                "\"foobar\"",
                "\"\"",
                "47",
                "0",
                "-74",
                "7.4",
                "0.0",
                "-4.7",
                "{}",
                "{\"foo\":\"bar\"}",
                "[]",
                "[\"foo\", \"bar\"]",
                "true",
                "false",
                "null"
            )
        }

        @ParameterizedTest(name = "'{'\"status\":{0}'}'")
        @MethodSource("getStatusValues")
        fun `deserialize StatusUnknown`(status: String) {
            val json = "{\"status\":$status}"
            val container = adapter.fromJson(json)

            val parsedStatus = status.trim('"')

            assertNotNull(container)
            assertIs<StatusUnknown>(container.status)
            assertEquals(parsedStatus, container.status.value)
        }

        @ParameterizedTest(name = "'{'\"status\":{0}'}'")
        @MethodSource("getStatusValues")
        fun `serialize StatusUnknown`(status: String) {
            val json = "{\"status\":$status}"
            val container = Container(StatusUnknown(status))
            val serializedContainer = adapter.toJson(container)

            assertEquals(json, serializedContainer)
        }
    }
}
