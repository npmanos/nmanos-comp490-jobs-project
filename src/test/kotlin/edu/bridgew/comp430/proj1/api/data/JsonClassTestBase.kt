package edu.bridgew.comp430.proj1.api.data

import com.squareup.moshi.Moshi
import org.junit.jupiter.api.BeforeEach

abstract class JsonClassTestBase {
    protected abstract var moshi: Moshi

    @BeforeEach
    abstract fun setupMoshi()
}
