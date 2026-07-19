package com.pourfect.ui

import androidx.lifecycle.ViewModel
import com.pourfect.domain.BrewParams
import com.pourfect.domain.PourStep

/**
 * Shared between the setup and guided-brew screens: holds the parameters the
 * user configured and the generated pour schedule for the brew in progress.
 */
class BrewSessionViewModel : ViewModel() {
    var params: BrewParams? = null
        private set
    var steps: List<PourStep> = emptyList()
        private set
    var recipeName: String = ""
        private set

    fun startSession(params: BrewParams, steps: List<PourStep>, recipeName: String) {
        this.params = params
        this.steps = steps
        this.recipeName = recipeName
    }
}
