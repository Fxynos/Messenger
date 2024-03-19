package com.vl.messenger.menu

import com.google.gson.annotations.SerializedName
import com.vl.messenger.ApiException
import com.vl.messenger.auth.SessionStore
import com.vl.messenger.menu.entity.User
import com.vl.messenger.util.StatusResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject

class SearchManager @Inject constructor(
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

    private interface Api {
        @GET("/users/search/{pattern}")
        fun search(
            @Header("Authorization") token: String,
            @Path("pattern") pattern: String,
            @Query("limit") limit: Int,
            @Query("from_id") key: Int?
        ): Call<StatusResponse<UsersDto>>

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