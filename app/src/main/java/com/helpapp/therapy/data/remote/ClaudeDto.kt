package com.helpapp.therapy.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageRequest(
    val model: String,
    val system: String,
    val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    val temperature: Double = 0.3,
)

@Serializable
data class Message(
    val role: String,
    val content: String,
)

@Serializable
data class MessageResponse(
    val id: String? = null,
    val model: String? = null,
    val role: String? = null,
    val content: List<ContentBlock> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
)

@Serializable
data class ContentBlock(
    val type: String,
    val text: String? = null,
)

@Serializable
data class ErrorResponse(
    val type: String? = null,
    val error: ErrorDetail? = null,
)

@Serializable
data class ErrorDetail(
    val type: String? = null,
    val message: String? = null,
)

fun MessageResponse.firstText(): String =
    content.firstOrNull { it.type == "text" }?.text.orEmpty()
