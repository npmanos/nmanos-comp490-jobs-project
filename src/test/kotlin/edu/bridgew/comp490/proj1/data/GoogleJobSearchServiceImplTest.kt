package edu.bridgew.comp490.proj1.data

import edu.bridgew.comp490.proj1.data.entities.Job
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class GoogleJobSearchServiceImplTest {
    private lateinit var retrofit: Retrofit
    lateinit var service: GoogleJobSearchServiceImpl
    lateinit var server: MockWebServer

    @BeforeTest
    fun setup() {
        server = MockWebServer()
        val baseUrl = server.url("/")

        retrofit = SerpApiClient(dotenv["JOBSPROJ_API_KEY"])
            .retrofit
            .newBuilder()
            .baseUrl(baseUrl)
            .build()

        service = GoogleJobSearchServiceImpl(retrofit)
    }

    @AfterTest
    fun tareDown() {
        server.shutdown()
    }

    @Test
    fun `verify 50 jobs retrieved from api`() = runTest {
        val pages = 5

        val testBodies = getRandomTestData(pages).map { path -> path.readText() }

        testBodies.forEach {
            server.enqueue(MockResponse().setBody(it))
        }

        val jobs = mutableListOf<Job>()

        (0 until pages).asFlow()
            .flatMapMerge { service.getJobs("software engineer", it) }
            .transform {
                assertIs<ApiResult.Success<List<Job>>>(it)

                val request = server.takeRequest()
                val requestUrl = request.requestUrl
                assertEquals(dotenv["JOBSPROJ_API_KEY"]!!, requestUrl?.queryParameter("api_key"))
                assertEquals("google_jobs", requestUrl?.queryParameter("engine"))
                assertEquals("software engineer", requestUrl?.queryParameter("q"))

                it.body.forEach { emit(it) }
            }.collect {
                jobs.add(it)
            }

        assertEquals(50, jobs.size)
    }

    private fun getRandomTestData(qty: Int): List<Path> {
        val dataSet = Path(dotenv["JOBSPROJ_TEST_DIR"]!!).listDirectoryEntries("software_engineer-*.json")
        val subset = mutableListOf<Path>()

        for (i in 0 until qty) {
            subset.add(dataSet.random())
        }

        return subset
    }
}
