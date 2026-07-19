package com.pourfect.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrewLogEntryTest {

    private val entry = BrewLogEntry(
        timestampMillis = 1_751_800_000_000,
        recipeName = "Japanese Iced",
        totalWater = 300,
        icePercent = 40,
        ratio = 15.0,
        coffeeGrams = 20.0,
        verdict = null
    )

    @Test
    fun `encode decode round trip without verdict`() {
        assertEquals(entry, BrewLogEntry.decode(entry.encode()))
    }

    @Test
    fun `encode decode round trip with verdict`() {
        val rated = entry.copy(verdict = TasteVerdict.SOUR)
        assertEquals(rated, BrewLogEntry.decode(rated.encode()))
    }

    @Test
    fun `pipes and newlines are stripped from recipe names`() {
        val sneaky = entry.copy(recipeName = "a|b\nc")
        assertEquals("abc", BrewLogEntry.decode(sneaky.encode())?.recipeName)
    }

    @Test
    fun `decoding garbage returns null`() {
        assertNull(BrewLogEntry.decode(""))
        assertNull(BrewLogEntry.decode("just|some|fields"))
        assertNull(BrewLogEntry.decode("abc|name|300|40|15.0|20.0|SOUR"))
    }

    @Test
    fun `unknown verdict decodes as null verdict rather than failing`() {
        val encoded = entry.copy(verdict = TasteVerdict.BITTER).encode()
            .replace("BITTER", "NONSENSE")
        assertEquals(entry.copy(verdict = null), BrewLogEntry.decode(encoded))
    }

    @Test
    fun `round trip preserves grinder name and setting`() {
        val withGrinder = entry.copy(grinderName = "Timemore C5S Pro", grindSetting = "19 clicks")
        assertEquals(withGrinder, BrewLogEntry.decode(withGrinder.encode()))
    }

    @Test
    fun `legacy seven-field entries decode with no grinder info`() {
        val legacy = "1751800000000|Japanese Iced|300|40|15.0|20.0|SOUR"
        val decoded = BrewLogEntry.decode(legacy)
        assertEquals("Japanese Iced", decoded?.recipeName)
        assertEquals(TasteVerdict.SOUR, decoded?.verdict)
        assertEquals(null, decoded?.grinderName)
        assertEquals(null, decoded?.grindSetting)
    }

    @Test
    fun `list round trip preserves order`() {
        val entries = listOf(
            entry,
            entry.copy(timestampMillis = 2, recipeName = "Rao Method", verdict = TasteVerdict.PERFECT)
        )
        assertEquals(entries, BrewLogEntry.decodeList(BrewLogEntry.encodeList(entries)))
    }
}
