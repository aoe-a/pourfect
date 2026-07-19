package com.pourfect.domain

import kotlin.math.roundToInt

/**
 * Turns [BrewParams] into a guided pour schedule. All weights are cumulative
 * scale readings of hot water; ice never crosses the scale under the dripper.
 */
object RecipeGenerator {

    // Ice goes in the server before the timer starts (the brew screen gates on
    // it), so schedules are pure hot-water pours for iced and hot brews alike.

    fun generate(params: BrewParams): List<PourStep> = when (params.preset) {
        Preset.CLASSIC -> classic(params)
        Preset.HOFFMANN -> hoffmann(params)
        Preset.FOUR_SIX -> fourSix(params)
        Preset.JAPANESE_ICED -> japaneseIced(params)
        Preset.WINTON -> winton(params)
        Preset.RAO -> rao(params)
        Preset.OSMOTIC -> osmotic(params)
    }

    fun generateCustom(recipe: CustomRecipe, params: BrewParams): List<PourStep> =
        customSchedule(recipe, params)

    private fun customSchedule(recipe: CustomRecipe, p: BrewParams): List<PourStep> {
        val bloom = (p.coffeeGrams * recipe.bloomMultiplier).roundToInt()
        val remaining = p.hotWaterGrams - bloom
        val bloomStep = PourStep(
            StepType.BLOOM, "Bloom",
            "Pour $bloom g to wet all the grounds, give a gentle swirl, and wait.",
            bloom, 0, 45, pourSeconds = 10
        )
        val pours = (1..recipe.pourCount).map { i ->
            val target = if (i == recipe.pourCount) p.hotWaterGrams
            else bloom + (remaining * i / recipe.pourCount.toDouble()).roundToInt()
            val previous = if (i == 1) bloom
            else bloom + (remaining * (i - 1) / recipe.pourCount.toDouble()).roundToInt()
            val start = 45 + (i - 1) * recipe.intervalSeconds
            PourStep(
                StepType.POUR, "Pour $i of ${recipe.pourCount}",
                "Pour ${target - previous} g in slow circles (scale: $target g).",
                target, start, start + recipe.intervalSeconds,
                pourSeconds = 15.coerceAtMost(recipe.intervalSeconds - 5)
            )
        }
        val lastEnd = pours.last().endTime
        return listOf(bloomStep) + pours + PourStep(
            StepType.DRAWDOWN, "Drawdown",
            "Let it drain to a flat bed, then remove the dripper.",
            null, lastEnd, lastEnd + 45
        )
    }

    // ---------- Classic: bloom + one slow continuous pour ----------

    private fun classic(p: BrewParams): List<PourStep> {
        val bloom = (p.coffeeGrams * 2).roundToInt()
        return listOf(
            PourStep(
                StepType.BLOOM, "Bloom",
                "Pour $bloom g to wet all the grounds, then give the dripper a gentle swirl and wait.",
                bloom, 0, 45, pourSeconds = 10
            ),
            PourStep(
                StepType.POUR, "Main pour",
                "Pour slowly in small circles until the scale reads ${p.hotWaterGrams} g.",
                p.hotWaterGrams, 45, 105, pourSeconds = 45
            ),
            PourStep(
                StepType.SWIRL, "Swirl",
                "Give the dripper a gentle swirl to flatten the coffee bed.",
                null, 105, 120
            ),
            PourStep(
                StepType.DRAWDOWN, "Drawdown",
                "Let the water drain through. Aim to finish around 3:00.",
                null, 120, 180
            )
        )
    }

    // ---------- Hoffmann Ultimate V60 ----------

    private fun hoffmann(p: BrewParams): List<PourStep> {
        val bloom = (p.coffeeGrams * 2).roundToInt()
        val sixty = (p.hotWaterGrams * 0.6).roundToInt()
        return listOf(
            PourStep(
                StepType.BLOOM, "Bloom",
                "Pour $bloom g (twice the dose), swirl until evenly wet, then wait until 0:45.",
                bloom, 0, 45, pourSeconds = 10
            ),
            PourStep(
                StepType.POUR, "First pour",
                "Pour steadily up to $sixty g (60%) by 1:15.",
                sixty, 45, 75, pourSeconds = 22
            ),
            PourStep(
                StepType.POUR, "Second pour",
                "Pour slowly and gently up to ${p.hotWaterGrams} g, keeping the dripper topped up.",
                p.hotWaterGrams, 75, 105, pourSeconds = 22
            ),
            PourStep(
                StepType.STIR, "Stir",
                "Stir gently once clockwise, once counter-clockwise to knock grounds off the walls.",
                null, 105, 120
            ),
            PourStep(
                StepType.SWIRL, "Swirl",
                "Lift the dripper and give it a gentle swirl to level the bed.",
                null, 120, 135
            ),
            PourStep(
                StepType.DRAWDOWN, "Drawdown",
                "Let it drain to a flat bed. Aim to finish by 3:30.",
                null, 135, 210
            )
        )
    }

    // ---------- Tetsu Kasuya 4:6 ----------

    private fun fourSix(p: BrewParams): List<PourStep> {
        // First 40% split by taste, last 60% split by strength.
        val firstSplit = when (p.taste) {
            Taste.SWEETER -> listOf(0.4, 0.6)
            Taste.BRIGHTER -> listOf(0.6, 0.4)
            Taste.STANDARD -> listOf(0.5, 0.5)
        }
        val strengthPours = when (p.strength) {
            Strength.LIGHTER -> 2
            Strength.STANDARD -> 3
            Strength.STRONGER -> 4
        }

        val fractions = firstSplit.map { it * 0.4 } + List(strengthPours) { 0.6 / strengthPours }
        var cumulative = 0.0
        val targets = fractions.map { f ->
            cumulative += f
            (p.hotWaterGrams * cumulative).roundToInt()
        }.toMutableList()
        targets[targets.lastIndex] = p.hotWaterGrams

        val steps = targets.mapIndexed { i, target ->
            val start = i * 45
            val amount = target - (if (i == 0) 0 else targets[i - 1])
            PourStep(
                type = if (i == 0) StepType.BLOOM else StepType.POUR,
                label = if (i == 0) "Bloom pour" else "Pour ${i + 1} of ${targets.size}",
                instruction = "Pour $amount g in slow circles (scale: $target g), " +
                    "then let it drain until the next pour.",
                targetWeight = target,
                startTime = start,
                endTime = start + 45,
                pourSeconds = 15
            )
        }
        val lastEnd = steps.last().endTime
        return steps + PourStep(
            StepType.DRAWDOWN, "Drawdown",
            "Let the last pour drain to a flat bed, then remove the dripper.",
            null, lastEnd, lastEnd + 45
        )
    }

    // ---------- Matt Winton five-pour (2021 World Brewers Cup) ----------

    private fun winton(p: BrewParams): List<PourStep> {
        val pourCount = 5
        val interval = 40
        val steps = (1..pourCount).map { i ->
            val target = if (i == pourCount) p.hotWaterGrams
            else (p.hotWaterGrams * i / pourCount.toDouble()).roundToInt()
            val previous = (p.hotWaterGrams * (i - 1) / pourCount.toDouble()).roundToInt()
            val start = (i - 1) * interval
            PourStep(
                type = if (i == 1) StepType.BLOOM else StepType.POUR,
                label = "Pour $i of $pourCount",
                instruction = "Pour ${target - previous} g in steady circles (scale: $target g), " +
                    "then wait until the bed nearly stops dripping.",
                targetWeight = target,
                startTime = start,
                endTime = start + interval,
                pourSeconds = 15
            )
        }
        val lastEnd = steps.last().endTime
        return steps + PourStep(
            StepType.DRAWDOWN, "Drawdown",
            "Let the final pour drain fully to a flat bed.",
            null, lastEnd, lastEnd + 45
        )
    }

    // ---------- Scott Rao: excavated bloom, one pour, finishing spin ----------

    private fun rao(p: BrewParams): List<PourStep> {
        val bloom = (p.coffeeGrams * 3).roundToInt()
        return listOf(
            PourStep(
                StepType.BLOOM, "Excavated bloom",
                "Pour $bloom g, then stir thoroughly with a spoon, digging down so no dry clumps survive.",
                bloom, 0, 45, pourSeconds = 10
            ),
            PourStep(
                StepType.POUR, "Main pour",
                "One steady pour in slow circles up to ${p.hotWaterGrams} g.",
                p.hotWaterGrams, 45, 105, pourSeconds = 45
            ),
            PourStep(
                StepType.SWIRL, "Rao spin",
                "Give the dripper a gentle flat spin to settle the bed evenly.",
                null, 105, 120
            ),
            PourStep(
                StepType.DRAWDOWN, "Drawdown",
                "Let it drain to a flat bed. Aim to finish around 3:00.",
                null, 120, 180
            )
        )
    }

    // ---------- Osmotic flow: slow center pour ----------

    private fun osmotic(p: BrewParams): List<PourStep> {
        val bloom = (p.coffeeGrams * 2).roundToInt()
        val half = (p.hotWaterGrams * 0.5).roundToInt()
        return listOf(
            PourStep(
                StepType.BLOOM, "Bloom",
                "Pour $bloom g to wet the grounds and wait while they de-gas.",
                bloom, 0, 45, pourSeconds = 10
            ),
            PourStep(
                StepType.POUR, "Center pour",
                "Pour a thin, slow stream into the center only, up to $half g. " +
                    "Let the water migrate outward on its own.",
                half, 45, 105, pourSeconds = 50
            ),
            PourStep(
                StepType.POUR, "Center pour",
                "Keep the slow center stream going up to ${p.hotWaterGrams} g. No circles, no agitation.",
                p.hotWaterGrams, 105, 150, pourSeconds = 35
            ),
            PourStep(
                StepType.DRAWDOWN, "Drawdown",
                "Hands off. Let it drain undisturbed to a flat bed.",
                null, 150, 195
            )
        )
    }

    // ---------- Japanese iced (flash brew) ----------

    private fun japaneseIced(p: BrewParams): List<PourStep> {
        val bloom = (p.coffeeGrams * 3).roundToInt()
        val sixty = (p.hotWaterGrams * 0.6).roundToInt()
        return listOf(
            PourStep(
                StepType.BLOOM, "Bloom",
                "Pour $bloom g, three times the dose. Iced brews like a generous bloom. Swirl and wait.",
                bloom, 0, 45, pourSeconds = 10
            ),
            PourStep(
                StepType.POUR, "First pour",
                "Pour steadily up to $sixty g. The concentrated brew will chill over the ice below.",
                sixty, 45, 90, pourSeconds = 30
            ),
            PourStep(
                StepType.POUR, "Second pour",
                "Pour gently up to ${p.hotWaterGrams} g.",
                p.hotWaterGrams, 90, 135, pourSeconds = 30
            ),
            PourStep(
                StepType.SWIRL, "Swirl",
                "Gently swirl the dripper to level the coffee bed.",
                null, 135, 150
            ),
            PourStep(
                StepType.DRAWDOWN, "Drawdown",
                "While it drains, swirl the server so the ice melts evenly. Finish around 3:00.",
                null, 150, 210
            )
        )
    }
}
