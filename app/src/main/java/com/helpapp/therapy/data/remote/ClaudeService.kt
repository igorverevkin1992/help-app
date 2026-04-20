package com.helpapp.therapy.data.remote

import com.helpapp.therapy.data.db.entities.UserContextEntity
import com.helpapp.therapy.domain.prompts.SystemPromptBuilder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around the Anthropic Messages API. Every call injects the
 * system prompt built from the user's current context variables, keeping the
 * LLM locked to the CBT / Logotherapy / ACT frame.
 */
@Singleton
class ClaudeService @Inject constructor(
    private val api: ClaudeApi,
    private val promptBuilder: SystemPromptBuilder,
) {
    suspend fun ask(
        context: UserContextEntity,
        userPrompt: String,
        taskDirective: String,
        temperature: Double = 0.3,
        maxTokens: Int = 1200,
    ): Result<String> = runCatching {
        val system = promptBuilder.build(context, taskDirective)
        val response = api.createMessage(
            MessageRequest(
                model = DEFAULT_MODEL,
                system = system,
                messages = listOf(Message(role = "user", content = userPrompt)),
                maxTokens = maxTokens,
                temperature = temperature,
            )
        )
        response.firstText()
    }

    companion object {
        const val DEFAULT_MODEL = "claude-3-5-sonnet-latest"
    }
}
