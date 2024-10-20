package com.vl.messenger.data.network

import com.vl.messenger.data.network.dto.Credentials
import com.vl.messenger.data.network.dto.DialogDto
import com.vl.messenger.data.network.dto.DialogResponse
import com.vl.messenger.data.network.dto.MessageDto
import com.vl.messenger.data.network.dto.MessageForm
import com.vl.messenger.data.network.dto.MessagesDto
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
    suspend fun getProfile(@Header("Authorization") token: String): StatusResponse<UserDto>

    @POST("/users/me/set-image")
    @Multipart
    suspend fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    )

    /* Friends */

    @GET("/users/friends")
    suspend fun getFriends(@Header("Authorization") token: String): StatusResponse<UsersDto>

    @PUT("/users/add-friend")
    suspend fun addFriend(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int
    )

    @DELETE("/users/friend")
    suspend fun removeFriend(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int
    )

    /* Users */

    @GET("/users/search/{pattern}")
    suspend fun search(
        @Header("Authorization") token: String,
        @Path("pattern") pattern: String,
        @Query("limit") limit: Int,
        @Query("from_id") key: Int?
    ): StatusResponse<UsersDto>

    @GET("/users/{id}")
    suspend fun getUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): StatusResponse<UserDto>

    /* Dialogs */

    @GET("/dialogs")
    suspend fun getDialogs(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int?
    ): StatusResponse<List<DialogResponse>>

    @GET("/dialogs/{id}")
    suspend fun getDialog(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): StatusResponse<DialogDto>

    @GET("/dialogs/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Query("id") dialogId: String,
        @Query("limit") limit: Int,
        @Query("from_id") key: Long?
    ): StatusResponse<MessagesDto>

    @POST("/dialogs/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body message: MessageForm
    ): StatusResponse<MessageDto>
}