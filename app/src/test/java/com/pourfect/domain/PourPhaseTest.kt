package com.pourfect.domain

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pour steps are split into an active pouring phase followed by a drip/wait
 * phase; [PourStep.pourSeconds] is the length of the active phase.
 */
class PourPhaseTest {

    private fun steps(preset: Preset) = RecipeGenerator.generate(
        BrewParams(preset = preset, totalWater = 300, ratio = 15.0)
    )

    @Test
    fun `every bloom and pour step pours first then leaves drip time`() {
        for (preset in Preset.entries) {
            for (step in steps(preset)) {
                val duration = step.endTime - step.startTime
                if (step.type == StepType.BLOOM || step.type == StepType.POUR) {
                    val pour = step.pourSeconds
                    assertTrue(
                        "$preset '${step.label}' pourSeconds=$pour should be in 1..${duration - 1} " +
                            "so the ring always shows both phases",
                        pour != null && pour in 1 until duration
                    )
                } else {
                    assertNull(
                        "$preset '${step.label}' is not a pour, pourSeconds must be null",
                        step.pourSeconds
                    )
                }
            }
        }
    }

    @Test
    fun `four six pulse pours pour briefly then wait for the drip`() {
        val pours = steps(Preset.FOUR_SIX).filter { it.type == StepType.POUR }
        for (pour in pours) {
            val duration = pour.endTime - pour.startTime
            assertTrue(
                "pulse pour should leave waiting time: ${pour.pourSeconds} of $duration",
                pour.pourSeconds!! < duration
            )
        }
    }

    @Test
    fun `custom recipe pours pour briefly then wait`() {
        val recipe = CustomRecipe("Mine", 15.0, 2, 3, 45)
        val params = BrewParams(preset = Preset.CLASSIC, totalWater = 300, ratio = 15.0)
        val pours = RecipeGenerator.generateCustom(recipe, params)
            .filter { it.type == StepType.POUR || it.type == StepType.BLOOM }
        for (pour in pours) {
            val duration = pour.endTime - pour.startTime
            assertTrue(pour.pourSeconds != null && pour.pourSeconds!! < duration)
        }
    }
}
