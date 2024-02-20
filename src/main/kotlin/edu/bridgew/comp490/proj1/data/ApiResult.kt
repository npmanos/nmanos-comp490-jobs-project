package edu.bridgew.comp490.proj1.data

import okhttp3.ResponseBody

/**
 * Sealed interface for API results.
 *
 * This interface represents the result of an API call. It can either be a Success or an Error.
 *
 * The [Success] class represents a successful API call and contains the body of the response.
 *
 * The [Error] class represents an unsuccessful API call and contains the error code and the error body.
 *
 *
 * @param T The type of the body in a successful API call.
 */
sealed interface ApiResult<T> {
    /**
     * Represents a successful API call.
     *
     * This class contains the body of the response from a successful API call.
     *
     * @property body The body of the response from a successful API call.
     */
    class Success<T>(val body: T) : ApiResult<T>

    /**
     * Represents an unsuccessful API call.
     *
     * This class contains the error code and the error body from an unsuccessful API call.
     *
     * @property code The error code from an unsuccessful API call.
     * @property errorBody The [error body][ResponseBody] from an unsuccessful API call.
     */
    class Error<T>(val code: Int, val errorBody: ResponseBody?) : ApiResult<T>
}
