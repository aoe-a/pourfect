package com.pourfect.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomRecipeTest {

    private val recipe = CustomRecipe(
        name = "My Morning V60",
        ratio = 15.0,
        bloomMultiplier = 2,
        pourCount = 3,
        intervalSeconds = 45
    )

    private val params = BrewParams(preset = Preset.CLASSIC, totalWater = 300, ratio = 15.0)

    @Test
    fun `custom schedule blooms with the chosen multiplier then equal pours to total`() {
        val steps = RecipeGenerator.generateCustom(recipe, params)

        val bloom = steps.first { it.type == StepType.BLOOM }
        assertEquals(40, bloom.targetWeight) // 2 x 20g dose

        val pours = steps.filter { it.type == StepType.POUR }
        assertEquals(3, pours.size)
        assertEquals(300, pours.last().targetWeight)
        // equal pours over the remaining 260g: cumulative ~127, ~213, 300
        val amounts = pours.mapIndexed { i, p ->
            p.targetWeight!! - (if (i == 0) 40 else pours[i - 1].targetWeight!!)
        }
        assertTrue("pour amounts should be near-equal: $amounts",
            amounts.max() - amounts.min() <= 1)
    }

    @Test
    fun `custom pours follow the chosen interval`() {
        val steps = RecipeGenerator.generateCustom(recipe, params)
        val pours = steps.filter { it.type == StepType.POUR }
        val gaps = pours.map { it.startTime }.zipWithNext { a, b -> b - a }
        assertTrue("gaps should all be 45s: $gaps", gaps.all { it == 45 })
    }

    @Test
    fun `custom recipe with ice pours to the hot water total with unshifted times`() {
        val iced = BrewParams(preset = Preset.CLASSIC, totalWater = 300, iceGrams = 90, ratio = 15.0)
        val steps = RecipeGenerator.generateCustom(recipe, iced)

        assertEquals(StepType.BLOOM, steps.first().type)
        assertEquals(0, steps.first().startTime)
        val pours = steps.filter { it.type == StepType.POUR }
        assertEquals(210, pours.last().targetWeight)
    }

    @Test
    fun `encode decode round trip preserves the recipe`() {
        val decoded = CustomRecipe.decode(recipe.encode())
        assertEquals(recipe, decoded)
    }

    @Test
    fun `pipe and newline are stripped from names when encoding`() {
        val sneaky = recipe.copy(name = "a|b\nc")
        val decoded = CustomRecipe.decode(sneaky.encode())
        assertEquals("abc", decoded?.name)
    }

    @Test
    fun `decoding garbage returns null`() {
        assertNull(CustomRecipe.decode("not|a|recipe"))
        assertNull(CustomRecipe.decode(""))
        assertNull(CustomRecipe.decode("name|abc|2|3|45"))
    }

    @Test
    fun `list encode decode round trip`() {
        val recipes = listOf(recipe, recipe.copy(name = "Evening", pourCount = 1))
        val decoded = CustomRecipe.decodeList(CustomRecipe.encodeList(recipes))
        assertEquals(recipes, decoded)
    }
}
