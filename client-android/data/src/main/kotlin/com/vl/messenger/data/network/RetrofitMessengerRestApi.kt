package com.vl.messenger.data.network

import com.vl.messenger.data.network.NetworkMapper.toDomain
import com.vl.messenger.data.network.NetworkMapper.toDomainWithFriendStatus
import com.vl.messenger.data.network.dto.Credentials
import com.vl.messenger.data.network.dto.MessageForm
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.MessengerRestApi.SignInResult
import com.vl.messenger.domain.boundary.MessengerRestApi.SignUpResult
import com.vl.messenger.domain.entity.ExtendedDialog
import com.vl.messenger.domain.entity.FriendStatus
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.entity.User
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import java.io.InputStream

class RetrofitMessengerRestApi(retrofit: Retrofit): MessengerRestApi {

    private val api = retrofit.create(ApiScheme::class.java)

    override suspend fun downloadFile(url: String): InputStream = api.download(url).byteStream()

    /* Auth */

    override suspend fun signIn(login: String, password: String): SignInResult =
        try {
            api.signIn(Credentials(login, password)).execute().run {
                if (isSuccessful)
                    SignInResult.Success(
                        body()!!
                            .requireResponse()
                            .toDomain()
                    )
                else if (code() == 401) // unauthorized
                    SignInResult.WrongCredentials
                else
                    SignInResult.Error(ApiException(this@run))
            }
        } catch (e: Exception) {
            SignInResult.Error(e)
        }

    override suspend fun signUp(login: String, password: String): SignUpResult =
        try {
            api.signUp(Credentials(login, password)).execute().run {
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

    /* Profile */

    override suspend fun getProfile(token: String): User =
        api.getProfile(token.toBearerAuthHeader())
            .requireResponse()
            .toDomain()

    override suspend fun uploadPhoto(token: String, image: ByteArray) =
        api.uploadPhoto(
            token.toBearerAuthHeader(),
            MultipartBody.Part.createFormData("image", "profile", RequestBody.create(
                MediaType.get("image/png"),
                image
            ))
        )

    /* Friends */

    override suspend fun getFriends(token: String): List<User> =
        api.getFriends(token.toBearerAuthHeader())
            .requireResponse()
            .toDomain()

    override suspend fun addFriend(token: String, userId: Int) =
        api.addFriend(token.toBearerAuthHeader(), userId)

    override suspend fun removeFriend(token: String, userId: Int) =
        api.removeFriend(token.toBearerAuthHeader(), userId)

    /* Users */

    override suspend fun getUserById(token: String, id: Int): Pair<User, FriendStatus> =
        api.getUser(token.toBearerAuthHeader(), id)
            .requireResponse()
            .toDomainWithFriendStatus()

    override suspend fun searchUserByName(
        token: String,
        pattern: String,
        limit: Int,
        key: Int?
    ): List<Pair<User, FriendStatus>> =
        api.search(token.toBearerAuthHeader(), pattern, limit, key)
            .requireResponse()
            .toDomainWithFriendStatus()

    /* Dialogs */

    override suspend fun getDialogs(
        token: String,
        limit: Int,
        offset: Int?
    ): List<ExtendedDialog> =
        api.getDialogs(token.toBearerAuthHeader(), limit, offset)
            .requireResponse()
            .map { it.toDomain() }

    override suspend fun getMessages(
        token: String,
        userId: Int,
        limit: Int,
        key: Long?
    ): List<Message> = api.getMessages(token.toBearerAuthHeader(), userId, limit, key)
            .requireResponse()
            .toDomain(null)

    override suspend fun getMessages(
        token: String,
        conversationId: Long,
        limit: Int,
        key: Long?
    ): List<Message> = TODO("Not yet implemented")

    override suspend fun sendMessage(token: String, message: String, userId: Int) =
        api.sendMessage(token.toBearerAuthHeader(), MessageForm(userId, message))
            .requireResponse()
            .toDomain(null)

    override suspend fun sendMessage(
        token: String,
        message: String,
        conversationId: Long
    ): Message = TODO("Not yet implemented")
}