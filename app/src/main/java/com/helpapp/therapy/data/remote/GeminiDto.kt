package com.helpapp.therapy.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    @SerialName("systemInstruction") val systemInstruction: Content? = null,
    val tools: List<ToolWrapper>? = null,
    @SerialName("toolConfig") val toolConfig: ToolConfig? = null,
    @SerialName("generationConfig") val generationConfig: GenerationConfig? = null,
    @SerialName("safetySettings") val safetySettings: List<SafetySetting>? = null,
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>,
)

@Serializable
data class Part(
    val text: String? = null,
    @SerialName("functionCall") val functionCall: FunctionCall? = null,
)

@Serializable
data class FunctionCall(
    val name: String,
    val args: JsonElement? = null,
)

@Serializable
data class ToolWrapper(
    @SerialName("functionDeclarations") val functionDeclarations: List<FunctionDeclaration>,
)

@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

@Serializable
data class ToolConfig(
    @SerialName("functionCallingConfig") val functionCallingConfig: FunctionCallingConfig,
)

@Serializable
data class FunctionCallingConfig(
    val mode: String,
    @SerialName("allowedFunctionNames") val allowedFunctionNames: List<String>? = null,
)

@Serializable
data class GenerationConfig(
    val temperature: Double? = null,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int? = null,
    @SerialName("topP") val topP: Double? = null,
    @SerialName("candidateCount") val candidateCount: Int? = null,
)

@Serializable
data class SafetySetting(
    val category: String,
    val threshold: String,
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList(),
    @SerialName("promptFeedback") val promptFeedback: PromptFeedback? = null,
    @SerialName("usageMetadata") val usageMetadata: UsageMetadata? = null,
)

@Serializable
data class Candidate(
    val content: Content? = null,
    @SerialName("finishReason") val finishReason: String? = null,
    val index: Int? = null,
)

@Serializable
data class PromptFeedback(
    @SerialName("blockReason") val blockReason: String? = null,
    @SerialName("safetyRatings") val safetyRatings: List<SafetyRating> = emptyList(),
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String,
)

@Serializable
data class UsageMetadata(
    @SerialName("promptTokenCount") val promptTokenCount: Int = 0,
    @SerialName("candidatesTokenCount") val candidatesTokenCount: Int = 0,
    @SerialName("totalTokenCount") val totalTokenCount: Int = 0,
)

/**
 * Pull the args object from the first functionCall part whose name matches the
 * requested tool. Returns null if the model emitted no function call for it
 * (e.g. responded with prose despite ANY-mode tool forcing).
 */
fun GenerateContentResponse.functionCallArgs(name: String): JsonElement? =
    candidates.firstOrNull()
        ?.content
        ?.parts
        ?.firstOrNull { it.functionCall?.name == name }
        ?.functionCall
        ?.args

fun GenerateContentResponse.firstText(): String =
    candidates.firstOrNull()
        ?.content
        ?.parts
        ?.firstOrNull { it.text != null }
        ?.text
        .orEmpty()
