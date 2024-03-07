package edu.bridgew.comp490.proj1.data.entities

import java.io.Serializable

sealed class SearchStatus(open val value: String) : Serializable

data object StatusProcessing : SearchStatus("Processing") {
    private fun readResolve(): Any = StatusProcessing
}

data object StatusSuccess : SearchStatus("Success") {
    private fun readResolve(): Any = StatusSuccess
}

data object StatusError : SearchStatus("Error") {
    private fun readResolve(): Any = StatusError
}

data class StatusUnknown(override val value: String) : SearchStatus(value) {

//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        return when (other) {
//            is StatusUnknown -> value == other.value
//            else -> false
//        }
//    }
//
//    override fun hashCode(): Int {
//        return value::javaClass.hashCode()
//    }
//
//    override fun toString(): String {
//        return "${javaClass.name.substringAfterLast('.')}(value = $value)"
//    }
}
