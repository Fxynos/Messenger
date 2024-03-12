package com.vl.messenger.menu

import com.google.gson.annotations.SerializedName
import com.vl.messenger.menu.entity.User
import com.vl.messenger.util.StatusResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject

class SearchManager @Inject constructor(retrofit: Retrofit) {

    private val api = retrofit.create(Api::class.java)

    fun search(pattern: String, limit: Int, key: String?): List<User> =
        api.search(pattern, limit, key).execute().body()!!.requireResponse().map { dto ->
            User(dto.id, dto.login, dto.image)
        }

    private interface Api {
        @GET("/search/{pattern}")
        fun search(
            @Path("pattern") pattern: String,
            @Query("limit") limit: Int,
            @Query("from_id") key: String?
        ): Call<StatusResponse<List<UserDto>>>

        class UserDto {
            val id: Int = 0
            val login: String = ""
            @SerializedName("image_url")
            val image: String? = null
        }
    }
}