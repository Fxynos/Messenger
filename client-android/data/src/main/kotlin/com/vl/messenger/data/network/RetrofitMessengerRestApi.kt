package com.vl.messenger.data.network

import com.vl.messenger.data.network.NetworkMapper.toDomain
import com.vl.messenger.data.network.NetworkMapper.toDomainWithFriendStatus
import com.vl.messenger.data.network.dto.Credentials
import com.vl.messenger.data.network.dto.MessageForm
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.MessengerRestApi.SignInResult
import com.vl.messenger.domain.boundary.MessengerRestApi.SignUpResult
import com.vl.messenger.domain.entity.ConversationMember
import com.vl.messenger.domain.entity.Dialog
import com.vl.messenger.domain.entity.ExtendedDialog
import com.vl.messenger.domain.entity.Message
import com.vl.messenger.domain.entity.Profile
import com.vl.messenger.domain.entity.Role
import com.vl.messenger.domain.entity.User
import com.vl.messenger.domain.entity.VerboseUser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.InputStream

class RetrofitMessengerRestApi(retrofit: Retrofit): MessengerRestApi {
    companion object {
        // TODO move out such logic from client, cause it should not care about dialog type
        private fun String.toConversationId(): Long =
            if (startsWith('c'))
                substring(1).toLong()
            else throw IllegalArgumentException("Conversation id expected: $this")
    }

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

    override suspend fun getProfile(token: String): Profile =
        api.getProfile(token.toBearerAuthHeader())
            .requireResponse()
            .toDomain()

    override suspend fun uploadPhoto(token: String, image: ByteArray) =
        api.uploadPhoto(
            token.toBearerAuthHeader(),
            MultipartBody.Part.createFormData(
                "image",
                "profile",
                image.toRequestBody("image/png".toMediaType())
            )
        )

    override suspend fun setProfileHidden(token: String, isHidden: Boolean) =
        api.updateVisibility(
            token.toBearerAuthHeader(),
            isHidden
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

    override suspend fun getUserById(token: String, id: Int): VerboseUser =
        api.getUser(token.toBearerAuthHeader(), id)
            .requireResponse()
            .toDomainWithFriendStatus()

    override suspend fun searchUserByName(
        token: String,
        pattern: String,
        limit: Int,
        key: Int?
    ): List<VerboseUser> =
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

    override suspend fun getDialog(token: String, id: String): Dialog =
        api.getDialog(token.toBearerAuthHeader(), id)
            .requireResponse()
            .toDomain()
            .dialog

    override suspend fun getMessages(
        token: String,
        dialogId: String,
        limit: Int,
        key: Long?
    ): List<Message> = api.getMessages(token.toBearerAuthHeader(), dialogId, limit, key)
            .requireResponse()
            .toDomain(dialogId)

    override suspend fun sendMessage(token: String, message: String, dialogId: String) =
        api.sendMessage(token.toBearerAuthHeader(), dialogId, MessageForm(message))
            .requireResponse()
            .toDomain(dialogId)

    override suspend fun createConversation(token: String, conversationName: String): String =
        api.createConversation(token.toBearerAuthHeader(), conversationName)
            .requireResponse()
            .dialogId

    override suspend fun leaveConversation(token: String, dialogId: String): Unit =
        api.leaveConversation(
            token.toBearerAuthHeader(),
            dialogId.toConversationId()
        )

    override suspend fun addMemberToConversation(
        token: String,
        dialogId: String,
        userId: Int
    ): Unit = api.addConversationMember(
        token.toBearerAuthHeader(),
        dialogId.toConversationId(),
        userId
    )

    override suspend fun removeMemberFromConversation(
        token: String,
        dialogId: String,
        userId: Int
    ): Unit = api.removeConversationMember(
        token.toBearerAuthHeader(),
        dialogId.toConversationId(),
        userId
    )

    override suspend fun setConversationMemberRole(
        token: String,
        dialogId: String,
        userId: Int,
        role: Int
    ): Unit = api.setConversationMemberRole(
        token.toBearerAuthHeader(),
        dialogId.toConversationId(),
        userId,
        role
    )

    override suspend fun getConversationMembers(
        token: String,
        dialogId: String,
        limit: Int?,
        offset: Int?
    ): List<ConversationMember> = api.getConversationMembers(
        token.toBearerAuthHeader(),
        dialogId.toConversationId(),
        limit,
        offset
    ).requireResponse().toDomain()

    override suspend fun getConversationRoles(token: String, dialogId: String): List<Role> =
        api.getConversationRoles(token.toBearerAuthHeader(), dialogId.toConversationId())
            .requireResponse()
            .toDomain()

    override suspend fun getOwnConversationRole(token: String, dialogId: String): Role =
        api.getOwnConversationRole(token.toBearerAuthHeader(), dialogId.toConversationId())
            .requireResponse()
            .role.toDomain()
}