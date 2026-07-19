package com.pourfect.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The three champion-recipe presets. The cross-preset invariants in
 * RecipeGeneratorTest cover them automatically via Preset.entries; these
 * tests pin down what makes each method itself.
 */
class ChampionPresetsTest {

    // ---------- Matt Winton five-pour ----------

    @Test
    fun `winton is five equal pours with no separate bloom wait`() {
        val params = BrewParams(preset = Preset.WINTON, totalWater = 300, ratio = 15.0)
        val pours = RecipeGenerator.generate(params)
            .filter { it.type == StepType.BLOOM || it.type == StepType.POUR }

        assertEquals(5, pours.size)
        assertEquals(listOf(60, 120, 180, 240, 300), pours.map { it.targetWeight })
        // pours land on a steady cadence, faster than the 4:6's 45s
        val starts = pours.map { it.startTime }
        val gaps = starts.zipWithNext { a, b -> b - a }
        assertTrue("gaps should be uniform: $gaps", gaps.distinct().size == 1)
    }

    @Test
    fun `winton instructions tell the user to wait for the bed to drain`() {
        val params = BrewParams(preset = Preset.WINTON, totalWater = 300, ratio = 15.0)
        val texts = RecipeGenerator.generate(params).joinToString(" ") { it.instruction }
        assertTrue(texts.contains("drain", ignoreCase = true) || texts.contains("dripping", ignoreCase = true))
    }

    // ---------- Scott Rao ----------

    @Test
    fun `rao blooms with three times the dose and stirs it`() {
        val params = BrewParams(preset = Preset.RAO, totalWater = 300, ratio = 15.0)
        val steps = RecipeGenerator.generate(params)

        val bloom = steps.first { it.type == StepType.BLOOM }
        assertEquals(60, bloom.targetWeight) // 3 x 20g dose
        assertTrue("bloom should mention stirring", bloom.instruction.contains("stir", ignoreCase = true))
    }

    @Test
    fun `rao has a single main pour to full weight then a spin`() {
        val params = BrewParams(preset = Preset.RAO, totalWater = 300, ratio = 15.0)
        val steps = RecipeGenerator.generate(params)

        val pours = steps.filter { it.type == StepType.POUR }
        assertEquals(1, pours.size)
        assertEquals(300, pours.single().targetWeight)
        assertTrue(steps.any { it.type == StepType.SWIRL })
    }

    // ---------- Osmotic flow ----------

    @Test
    fun `osmotic blooms with twice the dose then pours center only in two stages`() {
        val params = BrewParams(preset = Preset.OSMOTIC, totalWater = 300, ratio = 15.0)
        val steps = RecipeGenerator.generate(params)

        val bloom = steps.first { it.type == StepType.BLOOM }
        assertEquals(40, bloom.targetWeight) // 2 x 20g dose

        val pours = steps.filter { it.type == StepType.POUR }
        assertEquals(2, pours.size)
        assertEquals(300, pours.last().targetWeight)
        assertTrue(
            "pours should say center: ${pours.map { it.instruction }}",
            pours.all { it.instruction.contains("center", ignoreCase = true) }
        )
    }

    // ---------- Catalog ----------

    @Test
    fun `every preset enum value has a catalog entry`() {
        val cataloged = PresetCatalog.all.map { it.preset }.toSet()
        assertEquals(Preset.entries.toSet(), cataloged)
    }
}
