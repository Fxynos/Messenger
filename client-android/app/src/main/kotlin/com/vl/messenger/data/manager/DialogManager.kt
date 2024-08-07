package com.vl.messenger.data.manager

import com.google.gson.annotations.SerializedName
import com.vl.messenger.ApiException
import com.vl.messenger.data.entity.Dialog
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

    fun getDialogs(limit: Int, offset: Int? = null): List<Dialog> {
        val response = api.getDialogs(
            "Bearer ${sessionStore.accessTokenFlow.value!!.token}",
            limit,
            offset
        ).execute()

        if (!response.isSuccessful)
            throw ApiException(response)

        return response.body()!!.requireResponse().map {
            Dialog(
                it.dialog.id,
                it.isPrivate,
                it.dialog.title,
                it.dialog.image,
                if (it.lastMessage == null) null else Message(
                    it.lastMessage.id,
                    it.lastMessage.sender.id,
                    it.lastMessage.timestamp,
                    it.lastMessage.content
                ),
                if (it.lastMessage == null) null else User(
                    it.lastMessage.sender.id,
                    it.lastMessage.sender.login,
                    it.lastMessage.sender.image
                )
            )
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
            @Header("Authorization") token: String, // TODO put the header via okhttp interceptor only once
            @Query("limit") limit: Int,
            @Query("offset") offset: Int?
        ): Call<StatusResponse<List<DialogResponse>>>

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

        class DialogResponse {
            @SerializedName("is_private")
            val isPrivate: Boolean = false
            lateinit var dialog: DialogDto
            @SerializedName("last_message")
            val lastMessage: MessageDto? = null

            class MessageDto {
                val id: Long = 0
                val timestamp: Long = 0
                val content: String = ""
                lateinit var sender: UserDto
            }
        }

        class DialogDto {
            val id: Long = 0L
            val title: String = ""
            val image: String? = null
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