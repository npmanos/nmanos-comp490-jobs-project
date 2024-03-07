package edu.bridgew.comp490.proj1.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import edu.bridgew.comp490.proj1.data.debug.DebugSearchInterceptor
import edu.bridgew.comp490.proj1.data.entities.adapters.ExtensionJsonAdapter
import edu.bridgew.comp490.proj1.data.entities.adapters.SearchStatusAdapter
import edu.bridgew.comp490.proj1.data.entities.adapters.ZonedDateTimeAdapter
import io.github.cdimascio.dotenv.dotenv
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private val dotenv = dotenv {
    ignoreIfMissing = true
    ignoreIfMalformed = true
}

/**
 * Client class for the Serp API.
 *
 * This class is used to interact with the Serp API. It uses an API key for authentication and provides a [Retrofit] instance
 * for making API calls. The Retrofit instance is configured with a base URL and a [Moshi] converter factory.
 *
 * @param apiKey The API key used for authentication with the Serp API.
 */
class SerpApiClient(private val apiKey: String = "") {
    /**
     * Interceptor class for adding the API key to the request.
     *
     * This class intercepts the original request and adds the API key as a query parameter to the request URL.
     * The modified request is then proceeded with.
     *
     * @param apiKey The API key used for authentication with the Serp API.
     */
    private inner class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val url = request.url.newBuilder()
                .addQueryParameter("api_key", apiKey)
                .build()
            return chain.proceed(request.newBuilder().url(url).build())
        }
    }

    companion object {
        private const val BASE_URL = "https://serpapi.com/"
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val moshi by lazy {
        Moshi.Builder()
            .add(ZonedDateTimeAdapter())
            .addAdapter(SearchStatusAdapter())
            .addAdapter(ExtensionJsonAdapter())
            .build()
    }

    private val okHttp by lazy {
        val builder = OkHttpClient().newBuilder()

        if ((dotenv["JOBSPROJ_DEBUG_API"] ?: "false").toBoolean()) {
            builder.addInterceptor(DebugSearchInterceptor())
        } else {
            builder.addInterceptor(ApiKeyInterceptor(apiKey))
        }

        return@lazy builder.build()
    }

    /**
     * Lazy initialization of the [Retrofit] instance.
     *
     * This property is a Retrofit instance that is lazily initialized. The Retrofit instance is configured
     * with the [OkHttp client][OkHttpClient], the base URL for the Serp API, and a [Moshi] converter factory.
     * The Moshi converter factory is used to convert JSON to Kotlin objects and vice versa.
     */
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .client(okHttp)
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
