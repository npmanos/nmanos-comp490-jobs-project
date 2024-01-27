package edu.bridgew.comp430.proj1.api

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

    val client by lazy {
        Retrofit.Builder()
            .client(OkHttpClient()
                .newBuilder()
                .addInterceptor(ApiKeyInterceptor(apiKey))
                .build(),
            )
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val jobSearchApi by lazy {
        client.create(GoogleJobSearchService::class.java)
    }
}
