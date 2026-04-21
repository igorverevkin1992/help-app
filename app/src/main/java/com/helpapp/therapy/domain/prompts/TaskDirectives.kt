package com.helpapp.therapy.domain.prompts

/**
 * Task directives for each therapeutic module. Structured output is enforced
 * via the Anthropic Tool Use API — these directives describe clinical intent
 * and allocation heuristics, while the JSON shape is pinned by the tool's
 * input_schema in [ToolDefinitions].
 */
object TaskDirectives {

    const val RESPONSIBILITY_PIE = """
The user will submit an irrational self-blaming thought and a subjective guilt
score (0–10). Deconstruct the thought via the four-segment Responsibility Pie
and call the record_responsibility_distribution tool with:

  - biology_pct  ~40 baseline: involuntary neurobiological / amygdala reflex
  - medical_pct  ~30 baseline: objective physical / medical limitation
  - social_pct   ~20 baseline: rational duty toward dependents
  - control_pct  ~10 baseline: narrow zone of subjective agency

Adjust each category within ±10 based on the thought. All four MUST sum to
exactly 100. Tooltips ≤35 words, clinical tone. The self_forgiveness_statement
≤40 words, names biology and medical fact as dominant variables, strips the
'eternal debtor' status, no mysticism.
"""

    const val DEREFLECTION = """
The user will submit a list of mundane / bureaucratic / operational tasks.
Apply Franklian dereflection: each task is reframed as an act of generative
combat against entropy, isomorphic to martial planning. Call the
record_dereflection_reframing tool with one reframing_table row per submitted
task, and one anchor_statement (≤35 words) declaring that construction
against entropy is the highest form of discipline and civic courage. No
toxic positivity, no direct advice.
"""

    const val VITALITY_COMPASS = """
The user will submit an intrusive escapist or danger-seeking fantasy (the
'cognitive hook'). Apply ACT cognitive defusion, then call the
record_vitality_split tool with:

  1. defused_thought — reword the hook as: "My traumatized mind is currently
     generating a narrative that I should ..."
  2. suffering_path — exactly 3 concrete destructive consequences of
     surrendering to the hook (self-recrimination loops, procrastination,
     secondary trauma, risk to User_Objective_Limitation, etc.)
  3. vitality_path — exactly 3 concrete micro-actions anchored in
     User_Transcendent_Goal, each executable in under 10 minutes.
"""
}
