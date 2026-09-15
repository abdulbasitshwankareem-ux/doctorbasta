package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

object ApiKeyDataStore {
    private val GEMINI_API_KEY_PREF = stringPreferencesKey("gemini_api_key")

    fun getApiKeyFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[GEMINI_API_KEY_PREF] ?: ""
        }
    }

    suspend fun getApiKey(context: Context): String {
        return try {
            val preferences = context.dataStore.data.first()
            preferences[GEMINI_API_KEY_PREF] ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun saveApiKey(context: Context, key: String) {
        context.dataStore.edit { preferences ->
            preferences[GEMINI_API_KEY_PREF] = key.trim()
        }
    }

    suspend fun clearApiKey(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(GEMINI_API_KEY_PREF)
        }
    }
}
