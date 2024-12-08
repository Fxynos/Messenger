package com.vl.messenger.domain.entity

data class ConversationMember(
    val user: User,
    val role: String
)