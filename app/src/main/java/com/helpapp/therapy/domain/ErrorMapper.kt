package com.helpapp.therapy.domain

import com.helpapp.therapy.di.MissingApiKeyException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Translates low-level exceptions into human-readable error text without
 * leaking any PHI, stack traces, API keys, or internal identifiers.
 */
@Singleton
class ErrorMapper @Inject constructor() {
    fun map(t: Throwable): String = when (t) {
        is MissingApiKeyException ->
            "API key not configured. Set it in Settings."
        is SSLPeerUnverifiedException ->
            "Secure connection failed — certificate pin mismatch. Not proceeding."
        is HttpException -> when (t.code()) {
            401, 403 -> "Authentication rejected. Verify your API key in Settings."
            429 -> "Rate-limited by the API. Retry in a minute."
            in 500..599 -> "Service is temporarily unavailable. Try again shortly."
            else -> "Request failed (${t.code()})."
        }
        is IOException ->
            "Network unreachable. Check connectivity and retry."
        is SerializationException ->
            "Model response did not match the expected schema."
        else -> "Unexpected error. Try again."
    }
}
