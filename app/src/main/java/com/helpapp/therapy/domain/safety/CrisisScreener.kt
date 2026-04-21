package com.helpapp.therapy.domain.safety

import javax.inject.Inject
import javax.inject.Singleton

enum class CrisisLevel { NONE, WATCH, CRITICAL }

data class CrisisScreen(
    val level: CrisisLevel,
    val matchedTerms: List<String> = emptyList(),
)

/**
 * On-device keyword screener. This is NOT a clinical triage instrument — it
 * is a coarse filter that intercepts text before it is sent to the Gemini
 * API, so that suicidal, self-harm, psychotic or violent-ideation content
 * routes the user into the CrisisScreen (hotline + trusted-contact call)
 * instead of becoming an LLM prompt. False positives are acceptable — false
 * negatives are the failure mode we tune against.
 *
 * Two tiers:
 *   - CRITICAL: explicit intent / ideation language. Must block the LLM call
 *     and show the crisis sheet.
 *   - WATCH: latent distress markers. Caller decides policy (warn, still
 *     proceed, add a softening note).
 */
@Singleton
class CrisisScreener @Inject constructor() {

    fun screen(text: String): CrisisScreen {
        if (text.isBlank()) return CrisisScreen(CrisisLevel.NONE)
        val lower = text.lowercase()

        val critical = CRITICAL_PATTERNS.filter { it.containsMatchIn(lower) }
            .map { it.pattern }
        if (critical.isNotEmpty()) return CrisisScreen(CrisisLevel.CRITICAL, critical)

        val watch = WATCH_PATTERNS.filter { it.containsMatchIn(lower) }
            .map { it.pattern }
        if (watch.isNotEmpty()) return CrisisScreen(CrisisLevel.WATCH, watch)

        return CrisisScreen(CrisisLevel.NONE)
    }

    private companion object {
        // Whole-word (or stem) matches in Russian and English. Patterns use
        // \b boundaries where the language permits; Cyrillic patterns rely on
        // stems with lookaround to cut down on false positives. Intentionally
        // biased toward recall over precision.
        val CRITICAL_PATTERNS: List<Regex> = listOf(
            // RU — explicit suicidal/self-harm ideation
            Regex("(?i)\\bпокончить с собой\\b"),
            Regex("(?i)\\bсамоубий(ств|ц)"),
            Regex("(?i)\\bне хочу жить\\b"),
            Regex("(?i)\\bжить (больше )?не хочу\\b"),
            Regex("(?i)\\bубить себя\\b"),
            Regex("(?i)\\bповесит(ь|ься)\\b"),
            Regex("(?i)\\bпрыгнуть с\\b"),
            Regex("(?i)\\bвскрыть (себе )?вены\\b"),
            Regex("(?i)\\bпорезать себя\\b"),
            Regex("(?i)\\bлучше бы я (умер|сдох|не родился)\\b"),
            Regex("(?i)\\bхочу умереть\\b"),
            Regex("(?i)\\bпередозировк"),
            // EN
            Regex("(?i)\\bkill myself\\b"),
            Regex("(?i)\\bsuicid"),
            Regex("(?i)\\bend my life\\b"),
            Regex("(?i)\\btake my (own )?life\\b"),
            Regex("(?i)\\bhang myself\\b"),
            Regex("(?i)\\bi want to die\\b"),
            Regex("(?i)\\bi'?m going to die\\b"),
            Regex("(?i)\\bcut myself\\b"),
            Regex("(?i)\\boverdose\\b"),
            Regex("(?i)\\bshoot myself\\b"),
        )

        val WATCH_PATTERNS: List<Regex> = listOf(
            // RU — latent ideation / psychotic markers
            Regex("(?i)\\bбессмысленн(о|ость)\\b"),
            Regex("(?i)\\bникому не нужен\\b"),
            Regex("(?i)\\bголос(а)? в голове\\b"),
            Regex("(?i)\\bза мной следят\\b"),
            Regex("(?i)\\bхочу исчезнуть\\b"),
            // EN
            Regex("(?i)\\bhopeless\\b"),
            Regex("(?i)\\bno way out\\b"),
            Regex("(?i)\\bvoices in my head\\b"),
            Regex("(?i)\\bbeing watched\\b"),
            Regex("(?i)\\bdisappear forever\\b"),
        )
    }
}
