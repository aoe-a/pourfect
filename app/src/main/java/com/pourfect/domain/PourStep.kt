package com.pourfect.domain

enum class StepType { BLOOM, POUR, STIR, SWIRL, DRAWDOWN }

/**
 * One step of a guided brew.
 *
 * @param targetWeight cumulative grams the user's scale should read at the end
 *   of this step (hot water only), or null for steps that add no weight
 *   (stir, swirl, drawdown).
 * @param startTime seconds from the start of the brew when this step begins.
 * @param endTime seconds from the start of the brew when this step ends.
 * @param pourSeconds for BLOOM/POUR steps, how long the active pouring lasts
 *   from the start of the step; the rest of the window is drip/wait time.
 *   Null for steps with no pouring (stir, swirl, drawdown).
 */
data class PourStep(
    val type: StepType,
    val label: String,
    val instruction: String,
    val targetWeight: Int?,
    val startTime: Int,
    val endTime: Int,
    val pourSeconds: Int? = null
)
