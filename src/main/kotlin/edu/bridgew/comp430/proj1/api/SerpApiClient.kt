package edu.bridgew.comp430.proj1.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import edu.bridgew.comp430.proj1.api.adapters.ExtensionJsonAdapter
import edu.bridgew.comp430.proj1.api.adapters.ZonedDateTimeAdapter
import edu.bridgew.comp430.proj1.api.adapters.SearchStatusAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class SerpApiClient(private val apiKey: String) {
    private class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
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
            .add(SearchStatusAdapter())
            .addAdapter(ExtensionJsonAdapter())
            .build()
    }

    private val client by lazy {
        Retrofit.Builder()
            .client(OkHttpClient()
                .newBuilder()
                .addInterceptor(ApiKeyInterceptor(apiKey))
                .build(),
            )
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val jobSearchApi by lazy {
        client.create(GoogleJobSearchService::class.java)
    }
}
