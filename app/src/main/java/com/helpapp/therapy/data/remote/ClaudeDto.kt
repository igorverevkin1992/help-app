package com.helpapp.therapy.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class MessageRequest(
    val model: String,
    val system: String,
    val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    val temperature: Double = 0.3,
    val tools: List<Tool>? = null,
    @SerialName("tool_choice") val toolChoice: ToolChoice? = null,
)

@Serializable
data class Message(
    val role: String,
    val content: String,
)

@Serializable
data class Tool(
    val name: String,
    val description: String,
    @SerialName("input_schema") val inputSchema: JsonObject,
)

@Serializable
data class ToolChoice(
    val type: String,
    val name: String? = null,
)

@Serializable
data class MessageResponse(
    val id: String? = null,
    val model: String? = null,
    val role: String? = null,
    val content: List<ContentBlock> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
    val usage: Usage? = null,
)

@Serializable
data class ContentBlock(
    val type: String,
    val text: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: JsonElement? = null,
)

@Serializable
data class Usage(
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int = 0,
)

fun MessageResponse.firstText(): String =
    content.firstOrNull { it.type == "text" }?.text.orEmpty()

fun MessageResponse.toolInput(toolName: String): JsonElement? =
    content.firstOrNull { it.type == "tool_use" && it.name == toolName }?.input
