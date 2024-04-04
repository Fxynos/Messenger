package com.vl.messenger.data.entity

class StatusResponse<T> {
    private val response: T? = null
    val code: Int = 0
    val message: String = ""

    fun requireResponse(): T = response!!
}