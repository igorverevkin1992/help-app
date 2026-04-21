package com.helpapp.therapy.domain.prompts

import com.helpapp.therapy.data.remote.FunctionDeclaration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Gemini function-calling schemas for each therapeutic module. Using function
 * declarations instead of fenced-json eliminates parser fragility: the model
 * is forced to emit args matching the declared parameters schema.
 */
object ToolDefinitions {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val RESPONSIBILITY_PIE: FunctionDeclaration = FunctionDeclaration(
        name = "record_responsibility_distribution",
        description = "Allocate 100% of responsibility for an irrational self-blaming " +
            "thought across four structural categories and emit the self-forgiveness " +
            "statement that follows from that allocation.",
        parameters = json.parseToJsonElement(
            """
            {
              "type": "object",
              "properties": {
                "biology_pct": {
                  "type": "integer",
                  "description": "Weight of the involuntary neurobiological / amygdala reflex (baseline around 40, adjust plus or minus 10 based on the specific thought). Range 0-100."
                },
                "medical_pct": {
                  "type": "integer",
                  "description": "Weight of the objective physical / medical limitation (baseline around 30). Range 0-100."
                },
                "social_pct": {
                  "type": "integer",
                  "description": "Weight of the rational duty toward dependents (baseline around 20). Range 0-100."
                },
                "control_pct": {
                  "type": "integer",
                  "description": "Narrow zone of subjective agency (baseline around 10). Range 0-100."
                },
                "tooltips": {
                  "type": "object",
                  "properties": {
                    "biology": { "type": "string", "description": "Max 35 words explaining the biology slice" },
                    "medical": { "type": "string" },
                    "social":  { "type": "string" },
                    "control": { "type": "string" }
                  },
                  "required": ["biology","medical","social","control"]
                },
                "self_forgiveness_statement": {
                  "type": "string",
                  "description": "Max 40 words. Names biology and medical fact as dominant variables. Strips 'eternal debtor' status. Clinical tone."
                }
              },
              "required": ["biology_pct","medical_pct","social_pct","control_pct","tooltips","self_forgiveness_statement"]
            }
            """.trimIndent()
        ) as JsonObject,
    )

    val DEREFLECTION: FunctionDeclaration = FunctionDeclaration(
        name = "record_dereflection_reframing",
        description = "Reframe the user's mundane operational tasks as acts of generative " +
            "combat against entropy, isomorphic to martial planning.",
        parameters = json.parseToJsonElement(
            """
            {
              "type": "object",
              "properties": {
                "reframing_table": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "mundane": { "type": "string" },
                      "generative_equivalent": { "type": "string" }
                    },
                    "required": ["mundane","generative_equivalent"]
                  }
                },
                "anchor_statement": {
                  "type": "string",
                  "description": "One sentence (max 35 words). Declares that building against entropy is the highest form of discipline and civic courage."
                }
              },
              "required": ["reframing_table","anchor_statement"]
            }
            """.trimIndent()
        ) as JsonObject,
    )

    val VITALITY_COMPASS: FunctionDeclaration = FunctionDeclaration(
        name = "record_vitality_split",
        description = "Perform ACT cognitive defusion on an intrusive escapist fantasy, " +
            "then produce the Vitality vs. Suffering split with 3 concrete items each.",
        parameters = json.parseToJsonElement(
            """
            {
              "type": "object",
              "properties": {
                "defused_thought": {
                  "type": "string",
                  "description": "Reword the hook as: 'My traumatized mind is currently generating a narrative that I should ...'"
                },
                "suffering_path": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "Exactly 3 concrete destructive consequences of surrendering to the hook."
                },
                "vitality_path": {
                  "type": "array",
                  "items": {
                    "type": "string",
                    "description": "Micro-action anchored in the user's transcendent_goal, executable in under 10 minutes"
                  },
                  "description": "Exactly 3 concrete micro-actions."
                }
              },
              "required": ["defused_thought","suffering_path","vitality_path"]
            }
            """.trimIndent()
        ) as JsonObject,
    )
}
