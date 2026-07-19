package com.pourfect.ui.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pourfect.domain.BrewParams
import com.pourfect.domain.CustomRecipe
import com.pourfect.domain.PourStep
import com.pourfect.domain.PresetInfo
import com.pourfect.domain.RecipeGenerator
import com.pourfect.domain.Strength
import com.pourfect.domain.Taste

class BrewSetupViewModel(
    val presetInfo: PresetInfo,
    private val customRecipe: CustomRecipe? = null
) : ViewModel() {

    var totalWaterText by mutableStateOf("300")
    var iceGrams by mutableIntStateOf(300 * presetInfo.defaultIcePercent / 100)
    var ratio by mutableDoubleStateOf(presetInfo.defaultRatio)
    var taste by mutableStateOf(Taste.STANDARD)
    var strength by mutableStateOf(Strength.STANDARD)

    val totalWater: Int
        get() = totalWaterText.toIntOrNull() ?: 0

    val isValid: Boolean
        get() = totalWater in 100..1500

    val params: BrewParams
        get() = BrewParams(
            preset = presetInfo.preset,
            totalWater = totalWater,
            iceGrams = iceGrams,
            ratio = ratio,
            taste = taste,
            strength = strength
        )

    val steps: List<PourStep>
        get() = when {
            !isValid -> emptyList()
            customRecipe != null -> RecipeGenerator.generateCustom(customRecipe, params)
            else -> RecipeGenerator.generate(params)
        }

    fun adjustRatio(delta: Double) {
        ratio = (ratio + delta).coerceIn(13.0, 18.0)
    }
}
