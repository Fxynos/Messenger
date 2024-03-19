package com.vl.messenger

import retrofit2.Response

class ApiException(
    private val response: Response<*>
): Exception("${response.code()} \"${response.errorBody()!!.string()}\"") {
    val code: Int
        get() = response.code()
    val body: String
        get() = response.errorBody()!!.string()
}