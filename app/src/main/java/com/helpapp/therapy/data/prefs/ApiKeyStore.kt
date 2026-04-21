package com.helpapp.therapy.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the Claude API key inside EncryptedSharedPreferences (AES-GCM on
 * top of an Android Keystore master key). There is intentionally no
 * build-time fallback: a missing key must be handled by the onboarding
 * flow, never silently supplied from a compiled constant.
 */
@Singleton
class ApiKeyStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "therapy_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var apiKey: String
        get() = prefs.getString(KEY, null).orEmpty()
        set(value) = prefs.edit().putString(KEY, value.trim()).apply()

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    val hasKey: Boolean get() = apiKey.isNotBlank()

    private companion object {
        const val KEY = "claude_api_key"
    }
}
