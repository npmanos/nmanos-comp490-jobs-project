package edu.bridgew.comp430.proj1.api.data

sealed class SearchStatus

class SearchProcessing : SearchStatus()
class SearchSuccess : SearchStatus()
class SearchError(val error: String) : SearchStatus()
