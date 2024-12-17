package com.vl.messenger.domain.entity

data class Role(
    val id: Int,
    val name: String,
    val canGetReports: Boolean,
    val canEditData: Boolean,
    val canEditMembers: Boolean,
    val canEditRights: Boolean
)