package com.vl.messenger.data.network.dto

import com.google.gson.annotations.SerializedName

class RoleDto {
    val id: Int = 0
    val name: String = ""
    @SerializedName("get_reports") val canGetReports: Boolean = false
    @SerializedName("edit_data") val canEditData: Boolean = false
    @SerializedName("edit_members") val canEditMembers: Boolean = false
    @SerializedName("edit_rights") val canEditRights: Boolean = false
}