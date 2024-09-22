package com.vl.messenger.data.manager

import com.google.gson.annotations.SerializedName
import com.vl.messenger.ApiException
import com.vl.messenger.data.entity.StatusResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

class AuthManager(retrofit: Retrofit) {

    private val api = retrofit.create(Api::class.java)

    fun signIn(login: String, password: String): SignInResult =
        try {
            api.signIn(Api.Credentials(login, password)).execute().run {
                if (isSuccessful)
                    body()!!.requireResponse().let {
                        SignInResult.Token(it.token, it.expirationSec, it.userId)
                    }
                else if (code() == 401) // unauthorized
                    SignInResult.WrongCredentials
                else
                    SignInResult.Error(ApiException(this@run))
            }
        } catch (e: Exception) {
            SignInResult.Error(e)
        }

    fun signUp(login: String, password: String): SignUpResult =
        try {
            api.signUp(Api.Credentials(login, password)).execute().run {
                if (isSuccessful)
                    SignUpResult.Success
                else if (code() == 409) // conflict
                    SignUpResult.LoginIsTaken
                else
                    SignUpResult.Error(ApiException(this@run))
            }
        } catch (e: Exception) {
            SignUpResult.Error(e)
        }

    sealed interface SignUpResult {
        data object LoginIsTaken: SignUpResult
        data object Success: SignUpResult
        data class Error(val throwable: Throwable): SignUpResult
    }

    sealed interface SignInResult {
        data class Token(val token: String, val expirationSec: Long, val userId: Int): SignInResult
        data object WrongCredentials: SignInResult
        data class Error(val throwable: Throwable): SignInResult
    }

    private interface Api {
        @POST("/auth/sign-in")
        fun signIn(@Body credentials: Credentials): Call<StatusResponse<SignInResponse>>
        @POST("/auth/sign-up")
        fun signUp(@Body credentials: Credentials): Call<ResponseBody>

        class Credentials(val login: String, val password: String)

        class SignInResponse {
            @SerializedName("user_id")
            val userId: Int = 0
            @SerializedName("access_token")
            val token: String = ""
            @SerializedName("expires_in")
            val expirationSec: Long = 0
        }
    }
}