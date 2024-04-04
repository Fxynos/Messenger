package com.vl.messenger.data.manager

import com.google.gson.annotations.SerializedName
import com.vl.messenger.ApiException
import com.vl.messenger.data.entity.User
import com.vl.messenger.data.entity.StatusResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header

class ProfileManager(retrofit: Retrofit, private val sessionStore: SessionStore) {

    private val api = retrofit.create(Api::class.java)

    fun getProfile(): User = api.getProfile(
        "Bearer ${sessionStore.accessTokenFlow.value!!.token}"
    ).execute().run {
        if (!isSuccessful)
            throw ApiException(this)
        val dto = body()!!.requireResponse()
        User(dto.id, dto.login, dto.image)
    }

    fun getFriends(): List<User> {
        val response = api.getFriends(
            "Bearer ${sessionStore.accessTokenFlow.value!!.token}"
        ).execute()

        if (!response.isSuccessful)
            throw ApiException(response)

        return response.body()!!.requireResponse().users.map { dto ->
            User(dto.id, dto.login, dto.image)
        }
    }

    private interface Api {
        @GET("/users/me")
        fun getProfile(@Header("Authorization") token: String): Call<StatusResponse<UserDto>>

        @GET("/users/friends")
        fun getFriends(@Header("Authorization") token: String): Call<StatusResponse<UsersDto>>

        class UsersDto {
            val users: List<UserDto> = listOf()
        }

        class UserDto {
            val id: Int = 0
            val login: String = ""
            @SerializedName("image_url")
            val image: String? = null
        }
    }
}