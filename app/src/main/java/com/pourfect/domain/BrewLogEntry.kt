package com.pourfect.domain

/** One finished brew in the journal. */
data class BrewLogEntry(
    val timestampMillis: Long,
    val recipeName: String,
    val totalWater: Int,
    val icePercent: Int,
    val ratio: Double,
    val coffeeGrams: Double,
    val verdict: TasteVerdict? = null,
    val grinderName: String? = null,
    val grindSetting: String? = null
) {
    fun encode(): String = listOf(
        timestampMillis.toString(),
        recipeName.replace("|", "").replace("\n", ""),
        totalWater.toString(),
        icePercent.toString(),
        ratio.toString(),
        coffeeGrams.toString(),
        verdict?.name ?: "-",
        grinderName?.replace("|", "")?.replace("\n", "") ?: "-",
        grindSetting?.replace("|", "")?.replace("\n", "") ?: "-"
    ).joinToString("|")

    companion object {
        // 7 fields = entries written before grinder tracking existed
        fun decode(encoded: String): BrewLogEntry? {
            val parts = encoded.split("|")
            if (parts.size != 7 && parts.size != 9) return null
            return BrewLogEntry(
                timestampMillis = parts[0].toLongOrNull() ?: return null,
                recipeName = parts[1].trim().ifEmpty { return null },
                totalWater = parts[2].toIntOrNull() ?: return null,
                icePercent = parts[3].toIntOrNull() ?: return null,
                ratio = parts[4].toDoubleOrNull() ?: return null,
                coffeeGrams = parts[5].toDoubleOrNull() ?: return null,
                verdict = TasteVerdict.entries.firstOrNull { it.name == parts[6] },
                grinderName = parts.getOrNull(7)?.takeIf { it != "-" && it.isNotBlank() },
                grindSetting = parts.getOrNull(8)?.takeIf { it != "-" && it.isNotBlank() }
            )
        }

        fun encodeList(entries: List<BrewLogEntry>): String =
            entries.joinToString("\n") { it.encode() }

        fun decodeList(encoded: String): List<BrewLogEntry> =
            encoded.lines().mapNotNull { line ->
                line.takeIf { it.isNotBlank() }?.let(::decode)
            }
    }
}
