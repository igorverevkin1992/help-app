# Cognitive Exoskeleton

Personal Android application that operationalizes a three-module daily
neurocognitive cycle — CBT responsibility restructuring, Franklian
dereflection, and ACT cognitive defusion + vitality action — against
non-combatant moral injury, irrational guilt, and the psychological
fallout of permanent physical limitations.

The architecture is **generalist**: no biographical fact about the user is
hard-coded. All context (biological trigger, objective limitation, social
duty, transcendent goal) lives as four abstract state variables injected
into the LLM system prompt at runtime. Swap the values and the entire
therapeutic frame retargets without touching code.

## Stack

- Kotlin · Jetpack Compose · Material 3
- Hilt DI · Room (SQLite) · DataStore · EncryptedSharedPreferences
- Retrofit + OkHttp + kotlinx.serialization
- Google Gemini generateContent API (`gemini-2.5-pro`)

## Modules

| Screen | Module | Therapeutic method |
|--------|--------|--------------------|
| `ResponsibilityPieScreen` | Morning | CBT four-segment responsibility pie |
| `DereflectionScreen`      | Midday  | Logotherapy generative reframing    |
| `VitalityCompassScreen`   | Evening | ACT defusion + split-view + timer   |
| `AssessmentScreen`        | Weekly  | MIDS + SSFS (SFFA/SFB)              |
| `AnalyticsScreen`         | —       | Trend lines vs. MIDS cut-off (27)   |
| `SettingsScreen`          | —       | Dynamic context + API key           |

## Data model

SQLite (Room). Five tables mirroring the architectural spec: `user_context_variables`,
`daily_therapy_sessions`, `module_responsibility_pie`, `module_dereflection`,
`module_vitality_compass`, `psychometric_assessments`.

## Build

1. `./gradlew wrapper` (or use Android Studio to generate Gradle wrapper).
2. Supply the Gemini API key at runtime via the onboarding flow or the
   Settings screen (stored in `EncryptedSharedPreferences`). Obtain one at
   https://aistudio.google.com/apikey.
3. `./gradlew assembleDebug`.

Minimum SDK 26, target SDK 34.

## Prompting

See `SystemPromptBuilder.kt` and `TaskDirectives.kt`. Each module builds a
strict system instruction that injects the four context variables and
forces Gemini to call a declared function whose parameters schema pins the
shape of every module's response. The structured `args` are decoded by
`ToolOutputs` and rendered by the corresponding Compose screen — no
free-form LLM text leaks into the visualizations.
