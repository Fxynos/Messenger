package com.vl.messenger

import com.google.gson.annotations.SerializedName
import com.vl.messenger.auth.SessionStore
import com.vl.messenger.menu.entity.User
import com.vl.messenger.util.StatusResponse
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

    private interface Api {
        @GET("/users/me")
        fun getProfile(@Header("Authorization") token: String): Call<StatusResponse<Profile>>

        class Profile {
            val id: Int = 0
            val login: String = ""
            @SerializedName("image_url")
            val image: String? = null
        }
    }
}