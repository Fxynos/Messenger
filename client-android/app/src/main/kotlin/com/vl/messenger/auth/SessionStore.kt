package com.vl.messenger.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

class SessionStore(private val context: Context) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_EXPIRATION = longPreferencesKey("expiration")

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("session")
    }

    private val coroutineScope = CoroutineScope(Job())

    val accessTokenFlow: StateFlow<AccessToken?> = runBlocking { // TODO make it async
        context.dataStore.data.map { prefs ->
            prefs[KEY_TOKEN]?.let { token ->
                AccessToken(token, prefs[KEY_EXPIRATION]!!)
            }
        }.stateIn(coroutineScope)
    }

    suspend fun setAccessToken(token: AccessToken) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token.token
            prefs[KEY_EXPIRATION] = token.expiration
        }
    }

    /**
     * @param expiration unix time in seconds
     */
    data class AccessToken(val token: String, val expiration: Long) {
        companion object {
            const val EXPIRATION_LIMITLESS = 0
        }
    }
}