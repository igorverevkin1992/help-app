package com.helpapp.therapy.ui.screens.assessment

/**
 * Clinical domains for the weekly assessment. Items are phrased in original
 * wording that maps to the conceptual domains of the MIDS (moral injury
 * distress) and SSFS (State Self-Forgiveness Scale), avoiding verbatim reuse
 * of the source instruments.
 *
 * Each item is an English domain stub. The UI presents them directly rather
 * than referring to the user by number alone — that is what gives the audit
 * clinical signal instead of noise.
 */
object AssessmentItems {

    // MIDS Part Two — 18 items, 0 ("not at all") .. 4 ("extremely").
    val MIDS: List<String> = listOf(
        "Over the past week I felt distressed by recalling an event I or others were involved in",
        "I felt betrayed by people, institutions, or leaders I once trusted",
        "I felt ashamed of something I did or failed to do",
        "I struggled with guilt that I still cannot discharge",
        "I felt disgusted by my own actions or inaction",
        "I felt angry at myself for an outcome I could not fully control",
        "I felt anger at others whose actions harmed people I cared about",
        "I lost trust in a moral framework I used to rely on",
        "I believed I do not deserve comfort, rest, or support",
        "I believed the world is more unjust than I had previously acknowledged",
        "I avoided people or places that bring the event back",
        "I had intrusive memories, images, or thoughts I did not invite",
        "I kept myself emotionally numb so I would not have to feel",
        "I judged myself as unforgivable",
        "I felt I am contaminating the people close to me",
        "I felt my identity changed because of what happened",
        "I found it hard to experience positive emotions even when circumstances allowed",
        "My distress interfered with work, relationships, or rest",
    )

    // SSFS · SFFA — 8 items, self-forgiving feelings & actions, 1..4.
    val SFFA: List<String> = listOf(
        "I gave myself compassion rather than contempt",
        "I acted in a way that cared for my body and functioning",
        "I spoke to myself the way I would speak to a close friend in the same situation",
        "I let myself rest without interpreting rest as moral failure",
        "I allowed myself to engage in something meaningful despite the guilt",
        "I did not punish myself with deprivation",
        "I took an action that moved me toward my transcendent goal",
        "I extended to myself the benefit of the doubt I would extend to others",
    )

    // SSFS · SFB — 9 items, self-forgiving beliefs, 1..4.
    val SFB: List<String> = listOf(
        "I hold that a human being is more than a single failure",
        "I hold that my worth is not indexed to the worst moment of my life",
        "I hold that neurobiological reflex is not moral guilt",
        "I hold that objective physical limits are not a moral verdict",
        "I hold that repair of future conduct is a valid form of atonement",
        "I hold that perpetual self-condemnation is not what my dependents need",
        "I hold that I am entitled to the same standards of justice I apply to others",
        "I hold that my capacity for generative action survives the injury",
        "I hold that I am allowed to be an agent of repair rather than only an object of blame",
    )
}
