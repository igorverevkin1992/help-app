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
Return Markdown. When the directive asks for structured output (e.g. a
responsibility pie, a reframing table, a vitality/suffering split), return a
single fenced ```json block with the exact schema specified in the directive.
No prose outside the JSON block in that case. When the directive asks for free
text, keep it under 180 words and avoid headers.
        """
    }
}
