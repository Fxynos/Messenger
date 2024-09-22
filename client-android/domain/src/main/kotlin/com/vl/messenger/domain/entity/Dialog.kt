package com.vl.messenger.domain.entity

data class Dialog(
    val id: Long,
    val isPrivate: Boolean,
    val title: String,
    val image: String?
)