package edu.bridgew.comp490.proj1.data.debug

import okhttp3.Interceptor
import okhttp3.Response
import okio.buffer
import okio.source
import okio.use
import java.io.FileNotFoundException
import kotlin.random.Random

class DebugSearchInterceptor : Interceptor {
    val urls: MutableList<String> = mutableListOf()

    init {
        val resource = javaClass.getResourceAsStream("/edu/bridgew/comp490/proj1/data/debug/search_result_urls.txt")
            ?: throw FileNotFoundException("ERROR: search_result_urls.txt resource missing from jar")

        resource.source().use { resourceSource ->
            resourceSource.buffer().use { bufferedSource ->
                while (true) {
                    val url = bufferedSource.readUtf8Line() ?: break
                    urls.add(url)
                }
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val responseUrl = urls[Random.nextInt(urls.size)]
        return chain.proceed(request.newBuilder().url(responseUrl).build())
    }
}
