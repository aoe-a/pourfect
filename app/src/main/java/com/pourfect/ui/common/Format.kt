package com.pourfect.ui.common

import java.util.Locale

fun formatGrams(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(Locale.US, "%.1f", value)

fun formatRatio(ratio: Double): String =
    if (ratio % 1.0 == 0.0) "1:${ratio.toInt()}"
    else String.format(Locale.US, "1:%.1f", ratio)

fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

fun formatOunces(grams: Double): String =
    String.format(Locale.US, "%.1f oz", grams / 28.3495)

fun formatOunces(grams: Int): String = formatOunces(grams.toDouble())

/** "92–96°C" or "198–205°F" style range for the temperature hint. */
fun formatTempRange(lowC: Int, highC: Int, fahrenheit: Boolean): String =
    if (fahrenheit) "${lowC * 9 / 5 + 32}–${highC * 9 / 5 + 32}°F"
    else "$lowC–$highC°C"
