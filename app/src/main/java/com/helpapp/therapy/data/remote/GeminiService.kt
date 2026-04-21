package com.helpapp.therapy.data.remote

import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.domain.prompts.SystemPromptBuilder
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Google's Gemini generateContent API. Every call injects
 * the system prompt built from the user's current context variables, keeping
 * the LLM locked to the CBT / Logotherapy / ACT frame.
 *
 * Structured output goes through Gemini function calling: the caller passes a
 * [FunctionDeclaration] and the model is forced (via toolConfig ANY mode with
 * a single allowedFunctionName) to emit args matching the declared schema.
 */
@Singleton
class GeminiService @Inject constructor(
    private val api: GeminiApi,
    private val promptBuilder: SystemPromptBuilder,
) {
    /**
     * Run a function-calling round and return the JsonElement the model bound
     * to the declaration's parameters schema. Fails if the response contained
     * no functionCall for the requested name.
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
            ),
        )
        response.functionCallArgs(tool.name)
            ?: error("Model returned no functionCall for ${tool.name}")
    }

    companion object {
        const val DEFAULT_MODEL = "gemini-2.5-pro"
    }
}
