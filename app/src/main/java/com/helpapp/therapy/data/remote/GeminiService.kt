package com.helpapp.therapy.data.remote

import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.domain.prompts.SystemPromptBuilder
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

class SafetyBlockedException(val reason: String) :
    RuntimeException("Gemini refused the request for safety reasons: $reason")

class EmptyCandidateException :
    RuntimeException("Gemini returned no candidate content")

/**
 * Thin wrapper around Google's Gemini generateContent API. Every call injects
 * the system prompt built from the user's current context variables, keeping
 * the LLM locked to the CBT / Logotherapy / ACT frame.
 *
 * Structured output goes through Gemini function calling: the caller passes a
 * [FunctionDeclaration] and the model is forced (via toolConfig ANY mode with
 * a single allowedFunctionName) to emit args matching the declared schema.
 *
 * Safety filters are set to BLOCK_ONLY_HIGH — the default thresholds falsely
 * block clinical vocabulary (grief, suicidality, self-blame) that this
 * application is specifically designed to process. The on-device
 * CrisisScreener already intercepts explicit ideation before anything leaves
 * the device, so the remaining risk is the model, not the user, producing
 * harmful text.
 */
@Singleton
class GeminiService @Inject constructor(
    private val api: GeminiApi,
    private val promptBuilder: SystemPromptBuilder,
) {
    /**
     * Run a function-calling round and return the JsonElement the model bound
     * to the declaration's parameters schema. Throws [SafetyBlockedException]
     * if the model blocked the response, [EmptyCandidateException] if the
     * response was structurally empty.
     */
    suspend fun runTool(
        context: UserContextEntity,
        userPrompt: String,
        taskDirective: String,
        tool: FunctionDeclaration,
        temperature: Double = 0.2,
        maxTokens: Int = 1500,
    ): Result<JsonElement> = runCatching {
        val system = promptBuilder.build(context, taskDirective)
        val response = api.generateContent(
            model = DEFAULT_MODEL,
            request = GenerateContentRequest(
                systemInstruction = Content(parts = listOf(Part(text = system))),
                contents = listOf(Content(role = "user", parts = listOf(Part(text = userPrompt)))),
                tools = listOf(ToolWrapper(functionDeclarations = listOf(tool))),
                toolConfig = ToolConfig(
                    functionCallingConfig = FunctionCallingConfig(
                        mode = "ANY",
                        allowedFunctionNames = listOf(tool.name),
                    ),
                ),
                generationConfig = GenerationConfig(
                    temperature = temperature,
                    maxOutputTokens = maxTokens,
                ),
                safetySettings = SAFETY_SETTINGS,
            ),
        )

        response.promptFeedback?.blockReason?.let { throw SafetyBlockedException(it) }
        val candidate = response.candidates.firstOrNull() ?: throw EmptyCandidateException()
        val finish = candidate.finishReason
        if (finish != null && finish !in ACCEPTABLE_FINISH_REASONS) {
            throw SafetyBlockedException(finish)
        }
        response.functionCallArgs(tool.name)
            ?: throw EmptyCandidateException()
    }

    companion object {
        const val DEFAULT_MODEL = "gemini-2.5-pro"

        private val ACCEPTABLE_FINISH_REASONS = setOf("STOP", "MAX_TOKENS", "OTHER")

        // Threshold BLOCK_ONLY_HIGH — maximally permissive inside Gemini's
        // policy envelope. Clinical discussion of self-harm is the raison
        // d'être of this app; CrisisScreener handles explicit ideation
        // upstream of the API call.
        private val SAFETY_SETTINGS = listOf(
            SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_ONLY_HIGH"),
            SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_ONLY_HIGH"),
            SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_ONLY_HIGH"),
            SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_ONLY_HIGH"),
        )
    }
}
