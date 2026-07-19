package com.pourfect.domain

import kotlin.math.roundToInt

enum class Preset { CLASSIC, HOFFMANN, FOUR_SIX, JAPANESE_ICED, WINTON, RAO, OSMOTIC }

/** 4:6 method: how the first 40% of water is split between the first two pours. */
enum class Taste { BRIGHTER, STANDARD, SWEETER }

/** 4:6 method: how many pours the last 60% of water is divided into. */
enum class Strength { LIGHTER, STANDARD, STRONGER }

/**
 * Everything the user configures for a brew. [totalWater] includes ice: in a
 * flash brew the ice in the server melts into the drink, so the coffee dose is
 * based on the total while only [hotWaterGrams] goes through the grounds.
 */
class BrewParams(
    val preset: Preset,
    val totalWater: Int,
    iceGrams: Int = 0,
    val ratio: Double = 15.0,
    val taste: Taste = Taste.STANDARD,
    val strength: Strength = Strength.STANDARD
) {
    /** Ice never exceeds half the drink, or the brew gets too concentrated. */
    val iceGrams: Int = iceGrams.coerceIn(0, totalWater / 2)

    /** Derived, for display and advice only. */
    val icePercent: Int
        get() = if (totalWater == 0) 0 else this.iceGrams * 100 / totalWater

    val hotWaterGrams: Int
        get() = totalWater - iceGrams

    /** Dose rounded to the nearest 0.5 g — the resolution of a kitchen scale. */
    val coffeeGrams: Double
        get() = (totalWater / ratio * 2).roundToInt() / 2.0
}
