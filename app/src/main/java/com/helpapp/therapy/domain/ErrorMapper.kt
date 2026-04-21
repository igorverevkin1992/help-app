package com.helpapp.therapy.domain

import com.helpapp.therapy.data.remote.EmptyCandidateException
import com.helpapp.therapy.data.remote.SafetyBlockedException
import com.helpapp.therapy.di.MissingApiKeyException
import com.helpapp.therapy.di.OfflineOnlyException
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
        is OfflineOnlyException ->
            "Offline-only mode is on. Disable it in Settings to run this module."
        is SafetyBlockedException ->
            "The model refused this input on safety grounds. Rephrase the entry " +
                "in clinical terms (no direct self-harm language) and try again."
        is EmptyCandidateException ->
            "The model returned no usable response. Try again in a moment."
        is SSLPeerUnverifiedException ->
            "Secure connection failed — certificate pin mismatch. Not proceeding."
        is HttpException -> when (t.code()) {
            400 -> "The request was rejected by the API. Try simplifying the input."
            401, 403 -> "Authentication rejected. Verify your API key in Settings."
            404 -> "Model endpoint not found. The default model name may need updating."
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
