package com.pourfect.domain

data class PresetInfo(
    val preset: Preset,
    val id: String,
    val title: String,
    val tagline: String,
    val defaultRatio: Double,
    val defaultIcePercent: Int,
    val supportsTasteStrength: Boolean,
    /** Suggested ice share of total water when making this recipe iced. */
    val recommendedIcePercent: Int = 35
)

object PresetCatalog {
    val all = listOf(
        PresetInfo(
            preset = Preset.CLASSIC,
            id = "classic",
            title = "Classic V60",
            tagline = "Bloom + one slow pour. The perfect starting point.",
            defaultRatio = 15.0,
            defaultIcePercent = 0,
            supportsTasteStrength = false
        ),
        PresetInfo(
            preset = Preset.HOFFMANN,
            id = "hoffmann",
            title = "Hoffmann Ultimate",
            tagline = "The famous ultimate technique: bloom, two pours, stir & swirl.",
            defaultRatio = 16.67,
            defaultIcePercent = 0,
            supportsTasteStrength = false
        ),
        PresetInfo(
            preset = Preset.FOUR_SIX,
            id = "foursix",
            title = "4:6 Method",
            tagline = "Tetsu Kasuya's champion recipe. Tune sweetness and strength.",
            defaultRatio = 15.0,
            defaultIcePercent = 0,
            supportsTasteStrength = true
        ),
        PresetInfo(
            preset = Preset.WINTON,
            id = "winton",
            title = "Winton Five-Pour",
            tagline = "2021 World Brewers Cup winner. Five equal pours, each as the bed drains.",
            defaultRatio = 15.0,
            defaultIcePercent = 0,
            supportsTasteStrength = false
        ),
        PresetInfo(
            preset = Preset.RAO,
            id = "rao",
            title = "Rao Method",
            tagline = "Scott Rao's classic: stirred bloom, one steady pour, finishing spin.",
            defaultRatio = 16.7,
            defaultIcePercent = 0,
            supportsTasteStrength = false
        ),
        PresetInfo(
            preset = Preset.OSMOTIC,
            id = "osmotic",
            title = "Osmotic Flow",
            tagline = "Slow center pour lets water migrate outward. Syrupy and sweet.",
            defaultRatio = 15.7,
            defaultIcePercent = 0,
            supportsTasteStrength = false
        ),
        PresetInfo(
            preset = Preset.JAPANESE_ICED,
            id = "iced",
            title = "Japanese Iced",
            tagline = "Flash brew over ice. Bright, aromatic iced coffee.",
            defaultRatio = 15.0,
            defaultIcePercent = 40,
            supportsTasteStrength = false,
            recommendedIcePercent = 40
        )
    )

    fun byId(id: String): PresetInfo = all.firstOrNull { it.id == id } ?: all.first()
}
