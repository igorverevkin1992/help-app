package com.helpapp.therapy.domain.prompts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Lenient = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
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

object LlmJson {
    private val fenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)

    fun extract(raw: String): String {
        val match = fenceRegex.find(raw) ?: return raw.trim()
        return match.groupValues[1].trim()
    }

    inline fun <reified T> parse(raw: String): T =
        Lenient.decodeFromString(extract(raw))

    fun parsePie(raw: String): PieResponse = parse(raw)
    fun parseDereflection(raw: String): DereflectionResponse = parse(raw)
    fun parseVitality(raw: String): VitalityResponse = parse(raw)
}
