package com.vl.messenger.domain.boundary

import com.vl.messenger.domain.entity.AccessToken
import com.vl.messenger.domain.entity.ExtendedDialog
import com.vl.messenger.domain.entity.FriendStatus
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.entity.User
import java.io.InputStream

interface MessengerRestApi {

    suspend fun downloadFile(url: String): InputStream

    /* Auth */

    suspend fun signIn(login: String, password: String): SignInResult
    suspend fun signUp(login: String, password: String): SignUpResult

    sealed interface SignUpResult {
        data object LoginIsTaken: SignUpResult
        data object Success: SignUpResult
        data class Error(val throwable: Throwable): SignUpResult
    }

    sealed interface SignInResult {
        data class Success(val token: AccessToken): SignInResult
        data object WrongCredentials: SignInResult
        data class Error(val throwable: Throwable): SignInResult
    }

    /* Profile */

    suspend fun getProfile(token: String): User
    suspend fun uploadPhoto(token: String, image: ByteArray)

    /* Friends */

    suspend fun getFriends(token: String): List<User>
    suspend fun addFriend(token: String, userId: Int)
    suspend fun removeFriend(token: String, userId: Int)

    /* Users */

    suspend fun getUserById(token: String, id: Int): Pair<User, FriendStatus>
    suspend fun searchUserByName(
        token: String,
        pattern: String,
        limit: Int,
        key: Int?
    ): List<Pair<User, FriendStatus>>

    /* Dialogs */

    suspend fun getDialogs(token: String, limit: Int, offset: Int?): List<ExtendedDialog>
    suspend fun getMessages(token: String, userId: Int, limit: Int, key: Long?): List<Message>
    suspend fun getMessages(token: String, conversationId: Long, limit: Int, key: Long?): List<Message>
    suspend fun sendMessage(token: String, message: String, userId: Int): Message
    suspend fun sendMessage(token: String, message: String, conversationId: Long): Message
}