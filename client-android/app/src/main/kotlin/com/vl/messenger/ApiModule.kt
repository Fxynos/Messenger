package com.vl.messenger

import android.content.Context
import com.vl.messenger.data.manager.AuthManager
import com.vl.messenger.data.manager.DialogManager
import com.vl.messenger.data.manager.SessionStore
import com.vl.messenger.data.manager.DownloadManager
import com.vl.messenger.data.manager.ProfileManager
import com.vl.messenger.data.manager.SearchManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context) = SessionStore(context)

    @Provides
    @Singleton
    fun provideAuthManager(retrofit: Retrofit) = AuthManager(retrofit)

    @Provides
    @Singleton
    fun provideSearchManager(retrofit: Retrofit, sessionStore: SessionStore) =
        SearchManager(retrofit, sessionStore)

    @Provides
    @Singleton
    fun provideDownloadManager(retrofit: Retrofit) = DownloadManager(retrofit)

    @Provides
    @Singleton
    fun provideProfileManager(retrofit: Retrofit, sessionStore: SessionStore) = ProfileManager(retrofit, sessionStore)

    @Provides
    @Singleton
    fun provideDialogsManager(retrofit: Retrofit, sessionStore: SessionStore) = DialogManager(retrofit, sessionStore)
}