package com.vl.messenger.data.manager

import com.google.gson.annotations.SerializedName
import com.vl.messenger.ApiException
import com.vl.messenger.data.entity.Message
import com.vl.messenger.data.entity.StatusResponse
import com.vl.messenger.data.entity.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

class DialogManager(
    retrofit: Retrofit,
    private val sessionStore: SessionStore
) {
    private val api = retrofit.create(Api::class.java)

    fun getDialogs(): List<User> {
        val response = api.getDialogs(
            "Bearer ${sessionStore.accessTokenFlow.value!!.token}"
        ).execute()

        if (!response.isSuccessful)
            throw ApiException(response)

        return response.body()!!.requireResponse().users.map { dto ->
            User(dto.id, dto.login, dto.image)
        }
    }

    fun getMessages(userId: Int, limit: Int, key: Long?): List<Message> {
        val response = api.getMessages(
            "Bearer ${sessionStore.accessTokenFlow.value!!.token}",
            userId, key, limit
        ).execute()

        if (!response.isSuccessful)
            throw ApiException(response)

        return response.body()!!.requireResponse().messages.map { dto ->
            Message(dto.id, dto.senderId, dto.timestamp, dto.content)
        }
    }

    fun sendMessage(userId: Int, content: String): Message {
        val response = api.sendMessage(
            "Bearer ${sessionStore.accessTokenFlow.value!!.token}",
            Api.MessageForm(userId, content)
        ).execute()

        if (!response.isSuccessful)
            throw ApiException(response)

        return response.body()!!.requireResponse().let { dto ->
            Message(dto.id, dto.senderId, dto.timestamp, dto.content)
        }
    }

    private interface Api {
        @GET("/dialogs")
        fun getDialogs(
            @Header("Authorization") token: String
        ): Call<StatusResponse<UsersDto>>

        @GET("/messages/private")
        fun getMessages(
            @Header("Authorization") token: String,
            @Query("user_id") userId: Int,
            @Query("from_id") key: Long?,
            @Query("limit") limit: Int
        ): Call<StatusResponse<MessagesDto>>

        @POST("/messages/private/send")
        fun sendMessage(
            @Header("Authorization") token: String,
            @Body message: MessageForm
        ): Call<StatusResponse<MessageDto>>

        class UsersDto {
            val users: List<UserDto> = listOf()
        }

        class UserDto {
            val id: Int = 0
            val login: String = ""
            @SerializedName("image_url")
            val image: String? = null
        }

        class MessagesDto {
            val messages: List<MessageDto> = listOf()
        }

        class MessageDto {
            val id: Long = 0
            @SerializedName("sender_id")
            val senderId: Int = 0
            val timestamp: Long = 0
            val content: String = ""
        }

        class MessageForm(
            @SerializedName("user_id")
            val userId: Int,
            val content: String
        )
    }
}