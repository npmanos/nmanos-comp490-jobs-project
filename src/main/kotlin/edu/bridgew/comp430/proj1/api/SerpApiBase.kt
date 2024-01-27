package edu.bridgew.comp430.proj1.api

import serpapi.SerpApiSearch


abstract class SerpApiBase<T : SerpApiSearch> {
    protected abstract val apiClient: T
}
