package com.vl.messenger.di

import android.content.Context
import com.vl.messenger.BuildConfig
import com.vl.messenger.data.FileStorageAccessorImpl
import com.vl.messenger.data.SessionPreferencesStore
import com.vl.messenger.data.network.MessengerStompApiImpl
import com.vl.messenger.data.network.RetrofitMessengerRestApi
import com.vl.messenger.data.paging.dialog.DialogDataSourceImpl
import com.vl.messenger.data.paging.message.MessageDataSourceImpl
import com.vl.messenger.data.paging.user.UserDataSourceImpl
import com.vl.messenger.domain.boundary.DialogDataSource
import com.vl.messenger.domain.boundary.FileStorageAccessor
import com.vl.messenger.domain.boundary.MessageDataSource
import com.vl.messenger.domain.boundary.MessengerRestApi
import com.vl.messenger.domain.boundary.MessengerStompApi
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.boundary.UserDataSource
import com.vl.messenger.domain.usecase.AddConversationMemberUseCase
import com.vl.messenger.domain.usecase.AddFriendUseCase
import com.vl.messenger.domain.usecase.CreateConversationUseCase
import com.vl.messenger.domain.usecase.DownloadFileUseCase
import com.vl.messenger.domain.usecase.GetAvailableRolesUseCase
import com.vl.messenger.domain.usecase.GetDialogByIdUseCase
import com.vl.messenger.domain.usecase.GetFriendsUseCase
import com.vl.messenger.domain.usecase.GetIsLoggedInUseCase
import com.vl.messenger.domain.usecase.GetLoggedUserProfileUseCase
import com.vl.messenger.domain.usecase.GetOwnConversationRoleUseCase
import com.vl.messenger.domain.usecase.GetPagedConversationMembersUseCase
import com.vl.messenger.domain.usecase.GetPagedDialogsUseCase
import com.vl.messenger.domain.usecase.GetPagedMessagesUseCase
import com.vl.messenger.domain.usecase.GetPagedUsersByNameUseCase
import com.vl.messenger.domain.usecase.GetUserByIdUseCase
import com.vl.messenger.domain.usecase.LeaveConversationUseCase
import com.vl.messenger.domain.usecase.LogOutUseCase
import com.vl.messenger.domain.usecase.ObserveAllIncomingMessagesUseCase
import com.vl.messenger.domain.usecase.RemoveConversationMemberUseCase
import com.vl.messenger.domain.usecase.RemoveFriendUseCase
import com.vl.messenger.domain.usecase.SendMessageUseCase
import com.vl.messenger.domain.usecase.SetConversationMemberRole
import com.vl.messenger.domain.usecase.SignInUseCase
import com.vl.messenger.domain.usecase.SignUpUseCase
import com.vl.messenger.domain.usecase.UpdatePhotoUseCase
import com.vl.messenger.domain.usecase.UpdateProfileHiddenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DependencyHolder {

    /* Environment */

    @Provides
    @MessengerRestAddress
    fun provideMessengerRestApiAddress(): String = BuildConfig.ADDRESS

    @Provides
    @MessengerStompAddress
    fun provideMessengerStompApiAddress(): String = BuildConfig.ADDRESS

    /* Use case */

    @Provides
    @Singleton
    fun provideAddFriendUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        AddFriendUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideDownloadFileUseCase(api: MessengerRestApi) = DownloadFileUseCase(api)

    @Provides
    @Singleton
    fun provideGetDialogByIdUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        GetDialogByIdUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideGetFriendsUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        GetFriendsUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideUpdateProfileHiddenUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        UpdateProfileHiddenUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideGetIsLoggedInUseCase(sessionStore: SessionStore) = GetIsLoggedInUseCase(sessionStore)

    @Provides
    @Singleton
    fun provideLogOutUseCase(sessionStore: SessionStore) = LogOutUseCase(sessionStore)

    @Provides
    @Singleton
    fun provideGetLoggedUserProfileUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        GetLoggedUserProfileUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideUpdatePhotoUseCase(
        sessionStore: SessionStore,
        api: MessengerRestApi,
        fileStorageAccessor: FileStorageAccessor
    ) = UpdatePhotoUseCase(sessionStore, api, fileStorageAccessor)

    @Provides
    @Singleton
    fun provideGetPagedDialogsUseCase(sessionStore: SessionStore, dialogDataSource: DialogDataSource) =
        GetPagedDialogsUseCase(sessionStore, dialogDataSource)

    @Provides
    @Singleton
    fun provideGetPagedMessagesUseCase(sessionStore: SessionStore, messageDataSource: MessageDataSource) =
        GetPagedMessagesUseCase(sessionStore, messageDataSource)

    @Provides
    @Singleton
    fun provideGetPagedUsersByNameUseCase(sessionStore: SessionStore, userDataSource: UserDataSource) =
        GetPagedUsersByNameUseCase(sessionStore, userDataSource)

    @Provides
    @Singleton
    fun provideGetUserByIdUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        GetUserByIdUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideObserveAllIncomingMessagesUseCase(sessionStore: SessionStore, api: MessengerStompApi) =
        ObserveAllIncomingMessagesUseCase(api, sessionStore)

    @Provides
    @Singleton
    fun provideRemoveFriendUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        RemoveFriendUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideSendMessageUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        SendMessageUseCase(api, sessionStore)

    @Provides
    @Singleton
    fun provideSignInUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        SignInUseCase(api, sessionStore)

    @Provides
    @Singleton
    fun provideSignUpUseCase(api: MessengerRestApi, signInUseCase: SignInUseCase) =
        SignUpUseCase(api, signInUseCase)

    @Provides
    @Singleton
    fun provideCreateConversationUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        CreateConversationUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideLeaveConversationUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        LeaveConversationUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideAddConversationMemberUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        AddConversationMemberUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideRemoveConversationMemberUseCase(sessionStore: SessionStore, api: MessengerRestApi) =
        RemoveConversationMemberUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideSetConversationMemberRole(sessionStore: SessionStore, api: MessengerRestApi) =
        SetConversationMemberRole(sessionStore, api)

    @Provides
    @Singleton
    fun provideGetPagedConversationMembersUseCase(
        sessionStore: SessionStore,
        dialogDataSource: DialogDataSource
    ) = GetPagedConversationMembersUseCase(sessionStore, dialogDataSource)

    @Provides
    @Singleton
    fun provideGetOwnConversationRoleUseCase(
        sessionStore: SessionStore,
        api: MessengerRestApi
    ) = GetOwnConversationRoleUseCase(sessionStore, api)

    @Provides
    @Singleton
    fun provideGetAvailableRolesUseCase(
        sessionStore: SessionStore,
        api: MessengerRestApi
    ) = GetAvailableRolesUseCase(sessionStore, api)

    /* Boundary */

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore =
        SessionPreferencesStore(context)

    @Provides
    @Singleton
    fun provideMessengerRestApi(retrofit: Retrofit): MessengerRestApi =
        RetrofitMessengerRestApi(retrofit)

    @Provides
    @Singleton
    fun provideStompApi(@MessengerStompAddress address: String): MessengerStompApi =
        MessengerStompApiImpl(address)

    @Provides
    @Singleton
    fun provideDialogDataSource(api: MessengerRestApi): DialogDataSource = DialogDataSourceImpl(api)

    @Provides
    @Singleton
    fun provideMessageDataSource(api: MessengerRestApi): MessageDataSource = MessageDataSourceImpl(api)

    @Provides
    @Singleton
    fun provideUserDataSource(api: MessengerRestApi): UserDataSource = UserDataSourceImpl(api)

    @Provides
    fun provideFileStorageAccessor(@ApplicationContext context: Context): FileStorageAccessor =
        FileStorageAccessorImpl(context)

    /* Data layer */

    @Provides
    fun provideRetrofit(@MessengerRestAddress address: String): Retrofit = Retrofit.Builder()
        .baseUrl("http://$address")
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("Accept-Language", Locale.getDefault().language)
                            .build()
                    )
                }
                .addInterceptor(
                    HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
                .build()
        ).build()
}