package com.vl.messenger.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vl.messenger.domain.boundary.SessionStore
import com.vl.messenger.domain.entity.AccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionPreferencesStore(private val context: Context): SessionStore() {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_EXPIRATION = longPreferencesKey("expiration")
        private val KEY_USER_ID = intPreferencesKey("userId")

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("session")
    }

    private val accessTokenFlow: Flow<AccessToken?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]?.let { token ->
            AccessToken(token, prefs[KEY_EXPIRATION]!!, prefs[KEY_USER_ID]!!)
        }
    }

    override suspend fun setToken(token: AccessToken) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token.token
            prefs[KEY_EXPIRATION] = token.expiration
            prefs[KEY_USER_ID] = token.userId
        }
    }

    override suspend fun removeToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_EXPIRATION)
            prefs.remove(KEY_USER_ID)
        }
    }

    override fun observeToken(): Flow<AccessToken?> = accessTokenFlow
}