package com.vl.messenger.data.network

import com.vl.messenger.data.network.dto.ConversationMembersResponse
import com.vl.messenger.data.network.dto.CreateConversationResponse
import com.vl.messenger.data.network.dto.Credentials
import com.vl.messenger.data.network.dto.DialogResponse
import com.vl.messenger.data.network.dto.MessageDto
import com.vl.messenger.data.network.dto.MessageForm
import com.vl.messenger.data.network.dto.MessagesDto
import com.vl.messenger.data.network.dto.ProfileDto
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
import retrofit2.http.Url

internal interface ApiScheme {
    @Streaming
    @GET
    suspend fun download(@Url url: String): ResponseBody

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
    suspend fun uploadPhoto(
        @Header("Authorization") auth: String,
        @Part image: MultipartBody.Part
    )

    @PUT("/users/me/visibility")
    suspend fun updateVisibility(
        @Header("Authorization") auth: String,
        @Query("is_hidden") isHidden: Boolean
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

    @PUT("/conversations/{id}/members/{user_id}")
    suspend fun addConversationMember(
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

    @PUT("/conversations/{id}/members/{user_id}")
    suspend fun setConversationMemberRole(
        @Header("Authorization") auth: String,
        @Path("id") conversationId: Long,
        @Path("user_id") userId: Int,
        @Query("role") role: String
    )
}