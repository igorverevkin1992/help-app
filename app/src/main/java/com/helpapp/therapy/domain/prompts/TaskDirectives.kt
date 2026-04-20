package com.helpapp.therapy.domain.prompts

/**
 * Task directives for each therapeutic module. They are appended to the
 * system prompt and tell Claude exactly what structured output to produce.
 */
object TaskDirectives {

    const val RESPONSIBILITY_PIE = """
The user will submit an irrational self-blaming thought. Your job is to
deconstruct it via the four-segment Responsibility Pie.

Allocate 100% of responsibility across exactly these four categories,
anchored to the user's context variables:
  1. biology_pct  — the involuntary neurobiological trigger
                    (baseline ~40)
  2. medical_pct  — the objective physical/medical limitation
                    (baseline ~30)
  3. social_pct   — the rational duty toward dependents
                    (baseline ~20)
  4. control_pct  — the narrow zone of subjective agency
                    (baseline ~10)

Adjust within ±10 per category when the specific thought warrants it. All
four must sum to exactly 100.

For each category produce a tooltip (max 35 words, clinical tone, cites
either evolutionary reflex, medical objectivity, dependent welfare, or
agency). Conclude with a single self-forgiveness statement (max 40 words)
that explicitly names biology and medical fact as the dominant variables
and strips the user of 'eternal debtor' status.

Return exactly this JSON schema, nothing else:

```json
{
  "biology_pct": <int>,
  "medical_pct": <int>,
  "social_pct": <int>,
  "control_pct": <int>,
  "tooltips": {
    "biology": "<string>",
    "medical": "<string>",
    "social":  "<string>",
    "control": "<string>"
  },
  "self_forgiveness_statement": "<string>"
}
```
"""

    const val DEREFLECTION = """
The user will submit a list of current mundane / bureaucratic / operational
tasks. Your job is Franklian dereflection: recalibrate their perception so
each task is reframed as an act of 'generative combat' against entropy —
structurally isomorphic to martial planning.

Return exactly this JSON schema, nothing else:

```json
{
  "reframing_table": [
    { "mundane": "<user task>", "generative_equivalent": "<reframe>" }
  ],
  "anchor_statement": "<one sentence, max 35 words, declaring the user's
    current mission is creation and that building against entropy is the
    highest form of discipline and civic courage>"
}
```

No direct advice. No toxic positivity. Keep language austere and structural.
"""

    const val VITALITY_COMPASS = """
The user will submit an intrusive escapist or danger-seeking fantasy (the
'cognitive hook'). Apply ACT cognitive defusion, then produce the
Vitality vs. Suffering split.

Steps:
  1. Reword the hook in the defused form:
       "My traumatized mind is currently generating a narrative that I
        should ..."
  2. On the suffering vector, name 3 concrete destructive consequences of
     surrendering to the hook (self-recrimination loops, procrastination,
     secondary trauma, risk to the user's objective_limitation, etc.).
  3. On the vitality vector, produce 3 concrete micro-actions anchored in
     the user's transcendent_goal that can be executed in under 10 minutes.

Return exactly this JSON schema, nothing else:

```json
{
  "defused_thought": "<string>",
  "suffering_path": ["<string>", "<string>", "<string>"],
  "vitality_path": ["<string>", "<string>", "<string>"]
}
```
"""
}
