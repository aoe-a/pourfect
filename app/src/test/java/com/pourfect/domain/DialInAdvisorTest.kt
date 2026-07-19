package com.pourfect.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialInAdvisorTest {

    private fun params(iceGrams: Int = 0) = BrewParams(
        preset = Preset.CLASSIC, totalWater = 300, iceGrams = iceGrams, ratio = 15.0
    )

    private fun actionsFor(verdict: TasteVerdict, iceGrams: Int = 0): List<String> =
        DialInAdvisor.advise(verdict, params(iceGrams)).actions

    @Test
    fun `perfect brew advises repeating and no adjustments`() {
        val advice = DialInAdvisor.advise(TasteVerdict.PERFECT, params())
        assertTrue(advice.actions.isEmpty())
        assertTrue(advice.headline.isNotBlank())
    }

    @Test
    fun `bitter means over-extracted so grind coarser and cooler water`() {
        val actions = actionsFor(TasteVerdict.BITTER).joinToString(" ")
        assertTrue("should advise coarser grind: $actions", actions.contains("coarser", ignoreCase = true))
        assertTrue("should advise cooler water: $actions", actions.contains("cooler", ignoreCase = true))
    }

    @Test
    fun `sour means under-extracted so grind finer and hotter water`() {
        val actions = actionsFor(TasteVerdict.SOUR).joinToString(" ")
        assertTrue("should advise finer grind: $actions", actions.contains("finer", ignoreCase = true))
        assertTrue("should advise hotter water: $actions", actions.contains("hotter", ignoreCase = true))
    }

    @Test
    fun `weak advises a tighter ratio using the current ratio value`() {
        val actions = actionsFor(TasteVerdict.WEAK).joinToString(" ")
        // current 1:15 should point toward 1:14
        assertTrue("should mention moving to 1:14: $actions", actions.contains("1:14"))
    }

    @Test
    fun `weak iced brew also flags ice melt dilution`() {
        val actions = actionsFor(TasteVerdict.WEAK, iceGrams = 120).joinToString(" ")
        assertTrue("should mention ice: $actions", actions.contains("ice", ignoreCase = true))
    }

    @Test
    fun `hot brew advice never mentions ice`() {
        for (verdict in TasteVerdict.entries) {
            val actions = actionsFor(verdict, iceGrams = 0).joinToString(" ")
            assertTrue("$verdict advice mentions ice for a hot brew: $actions",
                !actions.contains("ice ", ignoreCase = true))
        }
    }

    @Test
    fun `astringent advises gentler pouring`() {
        val actions = actionsFor(TasteVerdict.ASTRINGENT).joinToString(" ")
        assertTrue("should advise gentler pour: $actions", actions.contains("gentl", ignoreCase = true))
    }

    @Test
    fun `every non-perfect verdict gives between one and three actions`() {
        for (verdict in TasteVerdict.entries.filter { it != TasteVerdict.PERFECT }) {
            val advice = DialInAdvisor.advise(verdict, params())
            assertTrue("$verdict should give 1..3 actions", advice.actions.size in 1..3)
            assertTrue("$verdict headline blank", advice.headline.isNotBlank())
        }
    }

    @Test
    fun `weak at the tightest ratio does not go below 1 to 13`() {
        val tight = BrewParams(preset = Preset.CLASSIC, totalWater = 300, ratio = 13.0)
        val actions = DialInAdvisor.advise(TasteVerdict.WEAK, tight).actions.joinToString(" ")
        assertEquals("should not suggest a ratio below 1:13", false, actions.contains("1:12"))
    }
}
