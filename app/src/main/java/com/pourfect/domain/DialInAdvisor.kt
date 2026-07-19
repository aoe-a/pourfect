package com.pourfect.domain

/** How the finished cup tasted, in the user's words. */
enum class TasteVerdict { PERFECT, BITTER, SOUR, WEAK, ASTRINGENT }

data class DialInAdvice(
    val headline: String,
    val actions: List<String>
)

/**
 * Turns a taste verdict into concrete adjustments for the next brew.
 * Rules follow standard V60 dial-in practice: bitterness signals
 * over-extraction, sourness under-extraction, astringency channeling
 * from an aggressive pour.
 */
object DialInAdvisor {

    fun advise(verdict: TasteVerdict, params: BrewParams): DialInAdvice = when (verdict) {
        TasteVerdict.PERFECT -> DialInAdvice(
            headline = "Lock it in! Keep this grind, dose, and pour exactly as they are.",
            actions = emptyList()
        )

        TasteVerdict.BITTER -> DialInAdvice(
            headline = "Bitter usually means over-extracted.",
            actions = listOf(
                "Grind 2 clicks coarser so water flows through faster.",
                "Try cooler water, about 3°C down from what you used."
            )
        )

        TasteVerdict.SOUR -> DialInAdvice(
            headline = "Sour usually means under-extracted.",
            actions = listOf(
                "Grind 2 clicks finer to slow the water down.",
                "Use hotter water, just off the boil for light roasts."
            )
        )

        TasteVerdict.WEAK -> DialInAdvice(
            headline = "Weak means not enough coffee made it into the cup.",
            actions = buildList {
                tighterRatio(params.ratio)?.let { tighter ->
                    add("Use more coffee: move the ratio from ${formatRatio(params.ratio)} to ${formatRatio(tighter)}.")
                }
                add("Grind 1 click finer for a little more extraction.")
                if (params.iceGrams > 0) {
                    add("Melting ice dilutes the cup. Try about ${(params.iceGrams - 30).coerceAtLeast(0)} g of ice instead of ${params.iceGrams} g.")
                }
            }
        )

        TasteVerdict.ASTRINGENT -> DialInAdvice(
            headline = "That dry, harsh feel points to channeling in the bed.",
            actions = listOf(
                "Pour more gently and lower to the bed, in slow circles.",
                "Grind 1 click coarser and swirl after the last pour to flatten the bed."
            )
        )
    }

    /** One step tighter (stronger), never below 1:13. */
    private fun tighterRatio(ratio: Double): Double? =
        (ratio - 1.0).takeIf { it >= 13.0 }

    private fun formatRatio(ratio: Double): String =
        if (ratio % 1.0 == 0.0) "1:${ratio.toInt()}" else "1:$ratio"
}
