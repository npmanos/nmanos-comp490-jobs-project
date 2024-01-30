package edu.bridgew.comp430.proj1.api.data

sealed class SearchStatus(val value: String)

data object StatusProcessing : SearchStatus("Processing")
data object StatusSuccess : SearchStatus("Success")
data object StatusError : SearchStatus("Error")
class StatusUnknown(value: String) : SearchStatus(value) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is StatusUnknown -> value == other.value
            else -> false
        }
    }

    override fun hashCode(): Int {
        return value::javaClass.hashCode()
    }

    override fun toString(): String {
        return "${javaClass.name.substringAfterLast('.')}(value = $value)"
    }
}
