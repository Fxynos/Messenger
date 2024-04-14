package com.vl.messenger.data.manager

import com.google.gson.annotations.SerializedName
import com.vl.messenger.ApiException
import com.vl.messenger.data.entity.FriendStatus
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.entity.StatusResponse
import com.vl.messenger.data.entity.UserProfile
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

class SearchManager(
    retrofit: Retrofit,
    private val sessionStore: SessionStore
) {
    private val api = retrofit.create(Api::class.java)

    fun search(pattern: String, limit: Int, key: Int?): List<User> {
        val response = api.search(
            "Bearer ${sessionStore.accessTokenFlow.value!!.token}",
            pattern,
            limit,
            key
        ).execute()

        if (!response.isSuccessful)
            throw ApiException(response)

        return response.body()!!.requireResponse().users.map { dto ->
            User(dto.id, dto.login, dto.image)
        }
    }

    fun getUser(id: Int): UserProfile {
        val response = api.getUser(
            "Bearer ${sessionStore.accessTokenFlow.value!!.token}",
            id
        ).execute()

        if (!response.isSuccessful)
            throw ApiException(response)

        return response.body()!!.requireResponse().run {
            UserProfile(id, login, image, friendStatus!!)
        }
    }

    private interface Api {
        @GET("/users/search/{pattern}")
        fun search(
            @Header("Authorization") token: String,
            @Path("pattern") pattern: String,
            @Query("limit") limit: Int,
            @Query("from_id") key: Int?
        ): Call<StatusResponse<UsersDto>>

        @GET("/users/{id}")
        fun getUser(
            @Header("Authorization") token: String,
            @Path("id") userId: Int
        ): Call<StatusResponse<UserDto>>

        class UsersDto {
            val users: List<UserDto> = listOf()
        }

        class UserDto {
            val id: Int = 0
            val login: String = ""
            @SerializedName("image_url")
            val image: String? = null
            @SerializedName("friend_status")
            val friendStatus: FriendStatus? = null
        }
    }
}