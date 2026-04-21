package com.helpapp.therapy.di

import com.helpapp.therapy.data.prefs.ApiKeyStore
import com.helpapp.therapy.data.remote.GeminiApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

    // Google GTS root CA pins — primary + two backups. These MUST be verified
    // against the live cert chain of generativelanguage.googleapis.com before
    // shipping. Rotate when Google rolls roots.
    private const val PIN_PRIMARY = "sha256/hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc="
    private const val PIN_BACKUP_1 = "sha256/Vfd95BwDeSQo+NUYxVEEIlvkOlWY2SalKK1lPhzOx78="
    private const val PIN_BACKUP_2 = "sha256/cGuxAXyFXFkWm61cF4HPWX8S0srS9j0aSqN0k4AP+4A="

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
        .add("generativelanguage.googleapis.com", PIN_PRIMARY, PIN_BACKUP_1, PIN_BACKUP_2)
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyStore: ApiKeyStore,
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

class MissingApiKeyException : RuntimeException("Gemini API key is not configured")
