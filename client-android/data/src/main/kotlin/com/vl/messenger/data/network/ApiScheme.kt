package com.vl.messenger.data.network

import com.vl.messenger.data.network.dto.ConversationMembersResponse
import com.vl.messenger.data.network.dto.CreateConversationResponse
import com.vl.messenger.data.network.dto.Credentials
import com.vl.messenger.data.network.dto.DialogResponse
import com.vl.messenger.data.network.dto.MessageDto
import com.vl.messenger.data.network.dto.MessageForm
import com.vl.messenger.data.network.dto.MessagesDto
import com.vl.messenger.data.network.dto.NotificationsResponse
import com.vl.messenger.data.network.dto.ProfileDto
import com.vl.messenger.data.network.dto.RoleResponse
import com.vl.messenger.data.network.dto.RolesResponse
import com.vl.messenger.data.network.dto.StatusResponse
import com.vl.messenger.data.network.dto.TokenDto
import com.vl.messenger.data.network.dto.UserDto
import com.vl.messenger.data.network.dto.UsersDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

internal interface ApiScheme {

    /* Auth */

    @POST("/auth/sign-in")
    fun signIn(@Body credentials: Credentials): Call<StatusResponse<TokenDto>>
    @POST("/auth/sign-up")
    fun signUp(@Body credentials: Credentials): Call<ResponseBody>

    /* Profile */

    @GET("/users/me")
    suspend fun getProfile(
        @Header("Authorization") auth: String
    ): StatusResponse<ProfileDto>

    @POST("/users/me/set-image")
    @Multipart
    suspend fun uploadProfileImage(
        @Header("Authorization") auth: String,
        @Part image: MultipartBody.Part
    )

    @PUT("/users/me/visibility")
    suspend fun updateVisibility(
        @Header("Authorization") auth: String,
        @Query("is_hidden") isHidden: Boolean
    )

    @GET("/notifications/")
    suspend fun getNotifications(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int,
        @Query("from_id") key: Long?
    ): StatusResponse<NotificationsResponse>

    @POST("/users/friends/invites/{id}/accept")
    suspend fun acceptFriendRequest(
        @Header("Authorization") auth: String,
        @Path("id") inviteId: Long
    )

    @POST("/conversations/invites/{id}/accept")
    suspend fun acceptConversationInvite(
        @Header("Authorization") auth: String,
        @Path("id") inviteId: Long
    )

    /* Friends */

    @GET("/users/friends")
    suspend fun getFriends(
        @Header("Authorization") auth: String
    ): StatusResponse<UsersDto>

    @PUT("/users/add-friend")
    suspend fun addFriend(
        @Header("Authorization") auth: String,
        @Query("user_id") userId: Int
    )

    @DELETE("/users/friend")
    suspend fun removeFriend(
        @Header("Authorization") auth: String,
        @Query("user_id") userId: Int
    )

    /* Users */

    @GET("/users/search/{pattern}")
    suspend fun search(
        @Header("Authorization") auth: String,
        @Path("pattern") pattern: String,
        @Query("limit") limit: Int,
        @Query("from_id") key: Int?
    ): StatusResponse<UsersDto>

    @GET("/users/{id}")
    suspend fun getUser(
        @Header("Authorization") auth: String,
        @Path("id") userId: Int
    ): StatusResponse<UserDto>

    /* Dialogs */

    @GET("/dialogs")
    suspend fun getDialogs(
        @Header("Authorization") auth: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int?
    ): StatusResponse<List<DialogResponse>>

    @GET("/dialogs/{id}")
    suspend fun getDialog(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    ): StatusResponse<DialogResponse>

    @GET("/dialogs/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") auth: String,
        @Path("id") dialogId: String,
        @Query("limit") limit: Int,
        @Query("from_id") key: Long?
    ): StatusResponse<MessagesDto>

    @POST("/dialogs/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") auth: String,
        @Path("id") dialogId: String,
        @Body message: MessageForm
    ): StatusResponse<MessageDto>

    /* Conversations */

    @POST("/conversations")
    suspend fun createConversation(
        @Header("Authorization") auth: String,
        @Query("name") conversationName: String
    ): StatusResponse<CreateConversationResponse>

    @PUT("/conversations/{id}/leave")
    suspend fun leaveConversation(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long
    )

    @POST("/conversations/{id}/members/{user_id}/invite")
    suspend fun inviteConversationMember(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long,
        @Path("user_id") userId: Int
    )

    @DELETE("/conversations/{id}/members/{user_id}")
    suspend fun removeConversationMember(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long,
        @Path("user_id") userId: Int
    )

    @GET("/conversations/{id}/members")
    suspend fun getConversationMembers(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): StatusResponse<ConversationMembersResponse>

    @PUT("/conversations/{id}/members/{user_id}/role")
    suspend fun setConversationMemberRole(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long,
        @Path("user_id") userId: Int,
        @Query("role") role: Int
    )

    @GET("/conversations/{id}/roles")
    suspend fun getConversationRoles(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long
    ): StatusResponse<RolesResponse>

    @GET("/conversations/{id}/roles/mine")
    suspend fun getOwnConversationRole(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long
    ): StatusResponse<RoleResponse>

    @Streaming
    @GET("/conversations/{id}/report")
    suspend fun getConversationReport(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long
    ): ResponseBody

    @PUT("/conversations/{id}/set-name")
    suspend fun setConversationName(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long,
        @Query("name") name: String
    )

    @POST("/conversations/{id}/set-image")
    @Multipart
    suspend fun uploadConversationImage(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long,
        @Part image: MultipartBody.Part
    )
}