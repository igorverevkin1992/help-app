package com.helpapp.therapy.domain.prompts

import com.helpapp.therapy.data.remote.Tool
import com.helpapp.therapy.data.remote.ToolChoice
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Anthropic Tool Use schemas for each therapeutic module. Using tool_use
 * instead of fenced-json eliminates parser fragility: Claude is forced to
 * emit a JSON object matching the declared input_schema.
 */
object ToolDefinitions {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val RESPONSIBILITY_PIE: Tool = Tool(
        name = "record_responsibility_distribution",
        description = "Allocate 100% of responsibility for an irrational self-blaming " +
            "thought across four structural categories and emit the self-forgiveness " +
            "statement that follows from that allocation.",
        inputSchema = json.parseToJsonElement(
            """
            {
              "type": "object",
              "properties": {
                "biology_pct": {
                  "type": "integer",
                  "minimum": 0,
                  "maximum": 100,
                  "description": "Weight of the involuntary neurobiological / amygdala reflex (baseline around 40, adjust ±10 based on the specific thought)"
                },
                "medical_pct": {
                  "type": "integer",
                  "minimum": 0,
                  "maximum": 100,
                  "description": "Weight of the objective physical / medical limitation (baseline around 30)"
                },
                "social_pct": {
                  "type": "integer",
                  "minimum": 0,
                  "maximum": 100,
                  "description": "Weight of the rational duty toward dependents (baseline around 20)"
                },
                "control_pct": {
                  "type": "integer",
                  "minimum": 0,
                  "maximum": 100,
                  "description": "Narrow zone of subjective agency (baseline around 10)"
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

    val DEREFLECTION: Tool = Tool(
        name = "record_dereflection_reframing",
        description = "Reframe the user's mundane operational tasks as acts of generative " +
            "combat against entropy, isomorphic to martial planning.",
        inputSchema = json.parseToJsonElement(
            """
            {
              "type": "object",
              "properties": {
                "reframing_table": {
                  "type": "array",
                  "minItems": 1,
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

    val VITALITY_COMPASS: Tool = Tool(
        name = "record_vitality_split",
        description = "Perform ACT cognitive defusion on an intrusive escapist fantasy, " +
            "then produce the Vitality vs. Suffering split with 3 concrete items each.",
        inputSchema = json.parseToJsonElement(
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
                  "minItems": 3,
                  "maxItems": 3,
                  "items": { "type": "string" }
                },
                "vitality_path": {
                  "type": "array",
                  "minItems": 3,
                  "maxItems": 3,
                  "items": {
                    "type": "string",
                    "description": "Micro-action anchored in the user's transcendent_goal, executable in under 10 minutes"
                  }
                }
              },
              "required": ["defused_thought","suffering_path","vitality_path"]
            }
            """.trimIndent()
        ) as JsonObject,
    )

    fun forceChoice(tool: Tool) = ToolChoice(type = "tool", name = tool.name)
}
