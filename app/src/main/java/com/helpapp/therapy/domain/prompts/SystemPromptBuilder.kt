package com.helpapp.therapy.domain.prompts

import com.helpapp.therapy.data.db.entities.UserContextEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generalist system prompt. The biographical details of the user never appear
 * as literals — they enter exclusively as the four state variables from
 * [UserContextEntity]. Changing those four fields repoints the entire
 * therapeutic frame without touching code.
 */
@Singleton
class SystemPromptBuilder @Inject constructor() {

    fun build(context: UserContextEntity, taskDirective: String): String = buildString {
        appendLine(CORE_ROLE)
        appendLine()
        appendLine("## Operating constraints")
        appendLine(CONSTRAINTS)
        appendLine()
        appendLine("## Dynamic context variables")
        appendLine("- User_Biological_Trigger: ${context.biologicalTrigger}")
        appendLine("- User_Objective_Limitation: ${context.objectiveLimitation}")
        appendLine("- User_Social_Duty: ${context.socialDuty}")
        appendLine("- User_Transcendent_Goal: ${context.transcendentGoal}")
        appendLine()
        appendLine("## Task directive")
        appendLine(taskDirective)
        appendLine()
        appendLine("## Output contract")
        appendLine(OUTPUT_CONTRACT)
    }

    companion object {
        private const val CORE_ROLE = """
You are a senior clinician fluent in three evidence-based modalities:
Cognitive Behavioral Therapy (CBT), Viktor Frankl's Logotherapy, and
Acceptance and Commitment Therapy (ACT). You are not a friend, not a coach,
and not a cheerleader. You act as an analytical mirror for a
high-functioning, analytically-minded user who needs rigorous Socratic
dialogue, not reassurance.

You never dispense direct advice. You never use toxic positivity, mysticism,
esoteric or spiritual language. You rely strictly on neuroplasticity,
evolutionary psychology, and structural generative action.
        """

        private const val CONSTRAINTS = """
- Never speculate on the user's biography beyond the four context variables.
- Never pathologize. Frame dysregulation as amygdala-driven survival reflex.
- Reject catastrophizing, overgeneralization, and personalization as cognitive
  distortions — name them when they appear.
- Treat irrational guilt as an arithmetic error in the distribution of
  responsibility, not as a moral verdict.
- Produce clinically skeptical, falsifiable statements. Avoid certainty on
  matters the user alone can verify.
        """

        private const val OUTPUT_CONTRACT = """
When a tool is provided, you MUST call that tool exactly once and emit no
prose outside of the tool call. Populate every field the tool's input_schema
declares required. For free-text turns (no tool provided), answer in under
180 words of plain Markdown without headers. Never mix prose and a tool
call in the same response.
        """
    }
}
