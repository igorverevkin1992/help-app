package com.helpapp.therapy.data.remote

import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.domain.prompts.SystemPromptBuilder
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around the Anthropic Messages API. Every call injects the
 * system prompt built from the user's current context variables, keeping the
 * LLM locked to the CBT / Logotherapy / ACT frame.
 *
 * Structured output goes through Anthropic's Tool Use API: the caller passes a
 * [Tool] and Claude is forced (via tool_choice) to emit a JSON object matching
 * the declared input_schema. That removes the need to parse fenced JSON out of
 * prose.
 */
@Singleton
class ClaudeService @Inject constructor(
    private val api: ClaudeApi,
    private val promptBuilder: SystemPromptBuilder,
) {
    /**
     * Run a tool-use round and return the JsonElement the model bound to the
     * tool's input_schema. Fails if the response contained no tool_use block
     * for the requested tool.
     */
    suspend fun runTool(
        context: UserContextEntity,
        userPrompt: String,
        taskDirective: String,
        tool: Tool,
        temperature: Double = 0.2,
        maxTokens: Int = 1500,
    ): Result<JsonElement> = runCatching {
        val system = promptBuilder.build(context, taskDirective)
        val response = api.createMessage(
            MessageRequest(
                model = DEFAULT_MODEL,
                system = system,
                messages = listOf(Message(role = "user", content = userPrompt)),
                maxTokens = maxTokens,
                temperature = temperature,
                tools = listOf(tool),
                toolChoice = ToolChoice(type = "tool", name = tool.name),
            )
        )
        response.toolInput(tool.name)
            ?: error("Model returned no tool_use block for ${tool.name}")
    }

    companion object {
        const val DEFAULT_MODEL = "claude-3-5-sonnet-latest"
    }
}
