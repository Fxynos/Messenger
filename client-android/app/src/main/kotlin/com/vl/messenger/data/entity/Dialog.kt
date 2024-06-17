package com.vl.messenger.data.entity

sealed interface Dialog {
    val id: Int
    val name: String
    val image: String?
}