package com.helpapp.therapy.di

import com.helpapp.therapy.data.prefs.ApiKeyStore
import com.helpapp.therapy.data.remote.ClaudeApi
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

    private const val BASE_URL = "https://api.anthropic.com/"
    private const val ANTHROPIC_VERSION = "2023-06-01"
    private const val ANTHROPIC_BETA = "tools-2024-05-16"

    // Anthropic's Let's Encrypt ISRG Root X1 chain — primary + backup pins.
    // Anyone forking this must verify current pins against the live cert
    // chain of api.anthropic.com before shipping.
    private const val PIN_PRIMARY = "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M="
    private const val PIN_BACKUP_1 = "sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E="
    private const val PIN_BACKUP_2 = "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg="

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
        .add("api.anthropic.com", PIN_PRIMARY, PIN_BACKUP_1, PIN_BACKUP_2)
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
            redactHeader("x-api-key")
            redactHeader("authorization")
            redactHeader("anthropic-version")
        }
        return OkHttpClient.Builder()
            .certificatePinner(pinner)
            .addInterceptor { chain ->
                val key = apiKeyStore.apiKey
                if (key.isBlank()) throw MissingApiKeyException()
                val request = chain.request().newBuilder()
                    .header("x-api-key", key)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header("anthropic-beta", ANTHROPIC_BETA)
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
    fun provideClaudeApi(retrofit: Retrofit): ClaudeApi = retrofit.create(ClaudeApi::class.java)
}

class MissingApiKeyException : RuntimeException("Claude API key is not configured")
