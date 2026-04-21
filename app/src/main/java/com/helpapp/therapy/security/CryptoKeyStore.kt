package com.helpapp.therapy.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces and persists the SQLCipher passphrase. The passphrase itself
 * (32 random bytes) is stored inside EncryptedSharedPreferences, which in
 * turn wraps it with a key living in the Android Keystore. This gives us
 * two-layer hardware-backed protection for the symmetric DB key.
 */
@Singleton
class CryptoKeyStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** Hex-encoded passphrase suitable for SQLCipher's SupportFactory. */
    fun getOrCreateDbPassphrase(): CharArray {
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (!existing.isNullOrBlank()) return existing.toCharArray()

        val bytes = ByteArray(PASSPHRASE_BYTES)
        SecureRandom().nextBytes(bytes)
        val hex = bytes.joinToString("") { "%02x".format(it) }
        prefs.edit().putString(KEY_DB_PASSPHRASE, hex).apply()
        return hex.toCharArray()
    }

    private companion object {
        const val FILE = "therapy_crypto_prefs"
        const val KEY_DB_PASSPHRASE = "db_passphrase_hex"
        const val PASSPHRASE_BYTES = 32
    }
}
