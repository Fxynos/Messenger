package com.vl.messenger.auth

import com.google.gson.annotations.SerializedName
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.net.SocketTimeoutException

class AuthModel(retrofit: Retrofit) {

    private val api = retrofit.create(Api::class.java)

    fun signIn(login: String, password: String): SignInResult? =
        try {
            api.signIn(Api.Credentials(login, password)).execute().run {
                if (isSuccessful)
                    body()!!.let { SignInResult.Token(it.token, it.expirationSec) }
                else if (code() == 401) // unauthorized
                    SignInResult.WrongCredentials
                else null // unexpected error
            }
        } catch (e: SocketTimeoutException) {
            null
        }

    fun signUp(login: String, password: String): SignUpResult? =
        try {
            api.signUp(Api.Credentials(login, password)).execute().run {
                if (isSuccessful)
                    SignUpResult.Success
                else if (code() == 409) // conflict
                    SignUpResult.LoginIsTaken
                else null
            }
        } catch (e: SocketTimeoutException) {
            null
        }

    sealed interface SignUpResult {
        object LoginIsTaken: SignUpResult
        object Success: SignUpResult
    }

    sealed interface SignInResult {
        class Token(val token: String, val expirationSec: Long): SignInResult
        object WrongCredentials: SignInResult
    }

    private interface Api {
        @POST("/auth/sign-in")
        fun signIn(@Body credentials: Credentials): Call<SignInResponse>
        @POST("/auth/sign-up")
        fun signUp(@Body credentials: Credentials): Call<ResponseBody>

        class Credentials(val login: String, val password: String)

        class SignInResponse(
            @SerializedName("user_id") val userId: Int,
            @SerializedName("access_token") val token: String,
            @SerializedName("expires_in") val expirationSec: Long
        )
    }
}