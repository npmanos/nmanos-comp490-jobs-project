package edu.bridgew.comp430.proj1.api.data

sealed class SearchStatus(val status: String)

data object StatusProcessing : SearchStatus("Processing")
data object StatusSuccess : SearchStatus("Success")
data object StatusError : SearchStatus("Error")
class StatusUnknown(status: String) : SearchStatus(status)
