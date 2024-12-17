package com.vl.messenger.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RoleDto(
    val id: Int,
    val name: String,
    @get:JsonProperty("get_reports") val canGetReports: Boolean,
    @get:JsonProperty("edit_data") val canEditData: Boolean,
    @get:JsonProperty("edit_members") val canEditMembers: Boolean,
    @get:JsonProperty("edit_rights") val canEditRights: Boolean
)