package edu.bridgew.comp490.proj1.api.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.BeforeEach

abstract class JsonClassTestBase<T> {
    protected abstract var moshi: Moshi
    protected abstract var adapter: JsonAdapter<T>

    @BeforeEach
    abstract fun setupMoshi()
}
