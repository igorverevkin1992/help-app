package com.helpapp.therapy.domain.prompts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val ToolJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = false
}

@Serializable
data class PieTooltips(
    val biology: String = "",
    val medical: String = "",
    val social: String = "",
    val control: String = "",
)

@Serializable
data class PieResponse(
    @SerialName("biology_pct") val biologyPct: Int,
    @SerialName("medical_pct") val medicalPct: Int,
    @SerialName("social_pct") val socialPct: Int,
    @SerialName("control_pct") val controlPct: Int,
    val tooltips: PieTooltips = PieTooltips(),
    @SerialName("self_forgiveness_statement") val statement: String = "",
)

@Serializable
data class ReframingRow(
    val mundane: String = "",
    @SerialName("generative_equivalent") val generativeEquivalent: String = "",
)

@Serializable
data class DereflectionResponse(
    @SerialName("reframing_table") val reframingTable: List<ReframingRow> = emptyList(),
    @SerialName("anchor_statement") val anchorStatement: String = "",
)

@Serializable
data class VitalityResponse(
    @SerialName("defused_thought") val defusedThought: String = "",
    @SerialName("suffering_path") val sufferingPath: List<String> = emptyList(),
    @SerialName("vitality_path") val vitalityPath: List<String> = emptyList(),
)

/**
 * Decodes the JsonElement that Gemini returns as the `args` of a functionCall
 * part. The schema is already validated on the API side, so we only need
 * type-safe deserialization here.
 */
object ToolOutputs {
    fun parsePie(input: JsonElement): PieResponse = ToolJson.decodeFromJsonElement(PieResponse.serializer(), input)
    fun parseDereflection(input: JsonElement): DereflectionResponse =
        ToolJson.decodeFromJsonElement(DereflectionResponse.serializer(), input)
    fun parseVitality(input: JsonElement): VitalityResponse =
        ToolJson.decodeFromJsonElement(VitalityResponse.serializer(), input)
}
