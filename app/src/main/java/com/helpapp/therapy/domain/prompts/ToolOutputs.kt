package com.helpapp.therapy.domain.prompts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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
 * part. Gemini's schema subset does not enforce min/max on arrays or numeric
 * ranges, so we re-validate the invariants client-side. Missing fields or
 * off-spec cardinalities surface as [SerializationException] so [ErrorMapper]
 * can show a uniform "schema mismatch" message.
 */
object ToolOutputs {
    fun parsePie(input: JsonElement): PieResponse {
        val p = ToolJson.decodeFromJsonElement(PieResponse.serializer(), input)
        listOf(p.biologyPct, p.medicalPct, p.socialPct, p.controlPct).forEach {
            if (it !in 0..100) throw SerializationException("pie percent out of range")
        }
        if (p.statement.isBlank()) throw SerializationException("missing self-forgiveness statement")
        return p
    }

    fun parseDereflection(input: JsonElement): DereflectionResponse {
        val d = ToolJson.decodeFromJsonElement(DereflectionResponse.serializer(), input)
        if (d.reframingTable.isEmpty()) {
            throw SerializationException("dereflection returned empty reframing table")
        }
        if (d.anchorStatement.isBlank()) {
            throw SerializationException("dereflection returned no anchor statement")
        }
        return d
    }

    fun parseVitality(input: JsonElement): VitalityResponse {
        val v = ToolJson.decodeFromJsonElement(VitalityResponse.serializer(), input)
        if (v.defusedThought.isBlank()) {
            throw SerializationException("vitality returned no defused thought")
        }
        val suffering = v.sufferingPath.filter { it.isNotBlank() }.take(3)
        val vitality = v.vitalityPath.filter { it.isNotBlank() }.take(3)
        if (suffering.size < 3 || vitality.size < 3) {
            throw SerializationException("vitality paths must each contain 3 items")
        }
        return v.copy(sufferingPath = suffering, vitalityPath = vitality)
    }
}
