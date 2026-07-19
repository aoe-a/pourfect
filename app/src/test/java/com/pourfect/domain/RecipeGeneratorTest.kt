package com.pourfect.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeGeneratorTest {

    // ---------- BrewParams math ----------

    @Test
    fun `ice and hot water split from total water and ice grams`() {
        val params = BrewParams(preset = Preset.JAPANESE_ICED, totalWater = 300, iceGrams = 130)
        assertEquals(130, params.iceGrams)
        assertEquals(170, params.hotWaterGrams)
        assertEquals(43, params.icePercent) // derived, for display only
    }

    @Test
    fun `zero ice means all water is hot`() {
        val params = BrewParams(preset = Preset.CLASSIC, totalWater = 300, iceGrams = 0)
        assertEquals(0, params.iceGrams)
        assertEquals(300, params.hotWaterGrams)
    }

    @Test
    fun `coffee dose is total water divided by ratio rounded to half gram`() {
        val params = BrewParams(preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0)
        assertEquals(20.0, params.coffeeGrams, 0.001)

        val hoffmann = BrewParams(preset = Preset.HOFFMANN, totalWater = 500, ratio = 16.67)
        assertEquals(30.0, hoffmann.coffeeGrams, 0.001)
    }

    @Test
    fun `ice grams are clamped between zero and half the total water`() {
        assertEquals(150, BrewParams(preset = Preset.CLASSIC, totalWater = 300, iceGrams = 260).iceGrams)
        assertEquals(0, BrewParams(preset = Preset.CLASSIC, totalWater = 300, iceGrams = -10).iceGrams)
    }

    // ---------- 4:6 method ----------

    @Test
    fun `four six standard is five equal pours every 45 seconds`() {
        val params = BrewParams(preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0)
        val steps = RecipeGenerator.generate(params)
        val pours = steps.filter { it.type == StepType.BLOOM || it.type == StepType.POUR }

        assertEquals(5, pours.size)
        assertEquals(listOf(60, 120, 180, 240, 300), pours.map { it.targetWeight })
        assertEquals(listOf(0, 45, 90, 135, 180), pours.map { it.startTime })
    }

    @Test
    fun `four six sweeter makes first pour smaller than second`() {
        val params = BrewParams(
            preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0, taste = Taste.SWEETER
        )
        val pours = RecipeGenerator.generate(params)
            .filter { it.type == StepType.BLOOM || it.type == StepType.POUR }
        val first = pours[0].targetWeight!!
        val second = pours[1].targetWeight!! - first

        assertTrue("first pour ($first) should be smaller than second ($second)", first < second)
        // The first 40% of water is still poured across the first two pours
        assertEquals(120, pours[1].targetWeight)
    }

    @Test
    fun `four six brighter makes first pour larger than second`() {
        val params = BrewParams(
            preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0, taste = Taste.BRIGHTER
        )
        val pours = RecipeGenerator.generate(params)
            .filter { it.type == StepType.BLOOM || it.type == StepType.POUR }
        val first = pours[0].targetWeight!!
        val second = pours[1].targetWeight!! - first

        assertTrue("first pour ($first) should be larger than second ($second)", first > second)
        assertEquals(120, pours[1].targetWeight)
    }

    @Test
    fun `four six stronger splits last 60 percent into four pours`() {
        val params = BrewParams(
            preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0, strength = Strength.STRONGER
        )
        val pours = RecipeGenerator.generate(params)
            .filter { it.type == StepType.BLOOM || it.type == StepType.POUR }

        assertEquals(6, pours.size) // 2 taste pours + 4 strength pours
        assertEquals(300, pours.last().targetWeight)
    }

    @Test
    fun `four six lighter splits last 60 percent into two pours`() {
        val params = BrewParams(
            preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0, strength = Strength.LIGHTER
        )
        val pours = RecipeGenerator.generate(params)
            .filter { it.type == StepType.BLOOM || it.type == StepType.POUR }

        assertEquals(4, pours.size) // 2 taste pours + 2 strength pours
        assertEquals(300, pours.last().targetWeight)
    }

    // ---------- Hoffmann ----------

    @Test
    fun `hoffmann blooms with twice the dose then pours to 60 and 100 percent`() {
        val params = BrewParams(preset = Preset.HOFFMANN, totalWater = 500, ratio = 16.67)
        val steps = RecipeGenerator.generate(params)

        val bloom = steps.first { it.type == StepType.BLOOM }
        assertEquals(60, bloom.targetWeight) // 2 x 30g dose

        val pours = steps.filter { it.type == StepType.POUR }
        assertEquals(2, pours.size)
        assertEquals(300, pours[0].targetWeight) // 60% of 500
        assertEquals(500, pours[1].targetWeight)
        // Hoffmann timing: first pour ends at 1:15, second at 1:45
        assertEquals(75, pours[0].endTime)
        assertEquals(105, pours[1].endTime)
    }

    @Test
    fun `hoffmann includes stir and swirl steps after pouring`() {
        val params = BrewParams(preset = Preset.HOFFMANN, totalWater = 500, ratio = 16.67)
        val types = RecipeGenerator.generate(params).map { it.type }
        assertTrue(types.contains(StepType.STIR))
        assertTrue(types.contains(StepType.SWIRL))
    }

    // ---------- Japanese iced ----------

    @Test
    fun `ice changes pour targets but adds no step and shifts no times`() {
        val params = BrewParams(
            preset = Preset.JAPANESE_ICED, totalWater = 300, iceGrams = 120, ratio = 15.0
        )
        val steps = RecipeGenerator.generate(params)

        // ice is handled before the timer starts, not as a timed step
        assertEquals(StepType.BLOOM, steps.first().type)
        assertEquals(0, steps.first().startTime)

        val waterSteps = steps.filter { it.type == StepType.BLOOM || it.type == StepType.POUR }
        assertEquals(180, waterSteps.last().targetWeight) // pours only reach hot-water total
    }

    @Test
    fun `iced and hot schedules of the same preset share identical timings`() {
        val hot = RecipeGenerator.generate(
            BrewParams(preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0)
        )
        val iced = RecipeGenerator.generate(
            BrewParams(preset = Preset.FOUR_SIX, totalWater = 300, iceGrams = 90, ratio = 15.0)
        )
        assertEquals(hot.map { it.startTime to it.endTime }, iced.map { it.startTime to it.endTime })
        assertEquals(210, iced.filter { it.targetWeight != null }.last().targetWeight)
    }

    // ---------- Invariants across all presets ----------

    @Test
    fun `pour targets are strictly increasing and end at hot water total for every preset`() {
        for (preset in Preset.entries) {
            for (ice in listOf(0, 90)) {
                val params = BrewParams(preset = preset, totalWater = 300, iceGrams = ice, ratio = 15.0)
                val steps = RecipeGenerator.generate(params)
                val targets = steps
                    .filter { it.type == StepType.BLOOM || it.type == StepType.POUR }
                    .map { it.targetWeight!! }

                assertEquals(
                    "$preset ice=$ice should end at hot water total",
                    params.hotWaterGrams, targets.last()
                )
                assertEquals(
                    "$preset ice=$ice targets should be strictly increasing",
                    targets, targets.sorted().distinct()
                )
            }
        }
    }

    @Test
    fun `step times are sequential and non-overlapping for every preset`() {
        for (preset in Preset.entries) {
            val params = BrewParams(preset = preset, totalWater = 300, ratio = 15.0)
            val steps = RecipeGenerator.generate(params)

            var previousEnd = 0
            for (step in steps) {
                assertTrue(
                    "$preset step '${step.label}' starts (${step.startTime}) before previous ended ($previousEnd)",
                    step.startTime >= previousEnd
                )
                assertTrue(
                    "$preset step '${step.label}' has non-positive duration",
                    step.endTime > step.startTime
                )
                previousEnd = step.endTime
            }
        }
    }

    @Test
    fun `every step has a non-blank label and instruction`() {
        for (preset in Preset.entries) {
            val params = BrewParams(preset = preset, totalWater = 300, iceGrams = 60, ratio = 15.0)
            for (step in RecipeGenerator.generate(params)) {
                assertTrue("$preset has blank label", step.label.isNotBlank())
                assertTrue("$preset has blank instruction", step.instruction.isNotBlank())
            }
        }
    }
}
