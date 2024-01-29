package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import edu.bridgew.comp430.proj1.api.adapters.SearchStatusAdapter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class SearchStatusTest : JsonClassTestBase() {
    override lateinit var moshi: Moshi

    @JsonClass(generateAdapter = true)
    internal data class Container(val status: SearchStatus)

    @BeforeTest
    override fun setupMoshi() {
        moshi = Moshi.Builder()
            .add(SearchStatusAdapter())
            .build()
    }

    @Nested
    @DisplayName("StatusProcessing tests")
    inner class StatusProcessingTest {
        private val json = "{\"status\":\"Processing\"}"
        private lateinit var adapter: JsonAdapter<Container>

        @BeforeTest
        fun setupJsonAdapter() {
            adapter = moshi.adapter()
        }

        @Test
        fun `StatusProcessing deserialize test`() {
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
    inner class StatusSuccessTest {
        private val json = "{\"status\":\"Success\"}"
        private lateinit var adapter: JsonAdapter<Container>

        @BeforeTest
        fun setupContainer() {
            adapter = moshi.adapter()
        }

        @Test
        fun `StatusSuccess deserialize test`() {
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
}
