package com.helpapp.therapy.di

import com.helpapp.therapy.data.prefs.ApiKeyStore
import com.helpapp.therapy.data.prefs.AppPreferences
import com.helpapp.therapy.data.remote.GeminiApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Public SPKI hashes for Google Trust Services roots used by
    // generativelanguage.googleapis.com. Multiple roots are pinned so that
    // expiry / cross-sign rotation does not brick the app. These SHOULD be
    // re-verified against the live chain at each release cut — see
    // https://pki.goog/repository/ for the current root catalogue.
    private val GOOGLE_ROOT_PINS = arrayOf(
        // GTS Root R1
        "sha256/hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc=",
        // GTS Root R2
        "sha256/Vfd95BwDeSQo+NUYxVEEIlvkOlWY2SalKK1lPhzOx78=",
        // GTS Root R3 (ECC)
        "sha256/QXnt2YHvdHR3tJYmQIr0Paosp6t/nggsEGD4QJZ3Q0g=",
        // GTS Root R4 (ECC)
        "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=",
        // GlobalSign Root CA - R2 (legacy cross-sign fallback)
        "sha256/iie1VXtL7HzAMF+/PVPR9xzT80kQxdZeJ+zduCB3uj0=",
    )

    @Provides
    @Singleton
    @Named("outbound")
    fun provideOutboundJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = false
        isLenient = false
    }

    @Provides
    @Singleton
    @Named("inbound")
    fun provideInboundJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner = CertificatePinner.Builder()
        .add("generativelanguage.googleapis.com", *GOOGLE_ROOT_PINS)
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyStore: ApiKeyStore,
        preferences: AppPreferences,
        pinner: CertificatePinner,
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            // Never body-log. Bodies contain PHI (self-blame, cognitive hooks)
            // and the request header carries the API key.
            level = HttpLoggingInterceptor.Level.NONE
            redactHeader("x-goog-api-key")
            redactHeader("authorization")
        }
        return OkHttpClient.Builder()
            .certificatePinner(pinner)
            .addInterceptor { chain ->
                // Fail-closed on offline-only mode before a socket is opened.
                // If the preferences read itself fails we treat it as "offline"
                // rather than silently letting PHI leave the device — the user
                // explicitly opted into offline-only and a transient DataStore
                // IO error must not regress that contract.
                val offline = runCatching {
                    runBlocking { preferences.snapshot.first().offlineOnlyMode }
                }.getOrElse { true }
                if (offline) throw OfflineOnlyException()

                val key = apiKeyStore.apiKey
                if (key.isBlank()) throw MissingApiKeyException()
                val request = chain.request().newBuilder()
                    .header("x-goog-api-key", key)
                    .header("content-type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logger)
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        @Named("outbound") outboundJson: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(outboundJson.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApi(retrofit: Retrofit): GeminiApi = retrofit.create(GeminiApi::class.java)
}

class MissingApiKeyException : java.io.IOException("Gemini API key is not configured")
class OfflineOnlyException : java.io.IOException("Offline-only mode is enabled")
