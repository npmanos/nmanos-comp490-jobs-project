package edu.bridgew.comp430.proj1.api

import okhttp3.ResponseBody

sealed interface ApiResult<T> {
    class Success<T>(val body: T) : ApiResult<T>
    class Error<T>(val code: Int, val errorBody: ResponseBody?) : ApiResult<T>
}
