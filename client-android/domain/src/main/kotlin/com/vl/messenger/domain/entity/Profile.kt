package com.vl.messenger.domain.entity

data class Profile(
    val id: Int,
    val login: String,
    val imageUrl: String?,
    val isHidden: Boolean
) {
    fun asUser() = User(id, login, imageUrl)
}