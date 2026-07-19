package com.pourfect.ui.brew

import androidx.compose.runtime.snapshots.Snapshot
import org.junit.Assert.assertTrue
import org.junit.Test

class BrewTimerViewModelTest {

    /**
     * Regression test: the guided-brew screen composes before the timer is
     * started, when the step list is still empty. That first composition must
     * read snapshot state (the step list itself), otherwise Compose never
     * invalidates it when start() fills the steps in — the screen stays blank.
     */
    @Test
    fun `reading currentStep before start registers a snapshot state read`() {
        val vm = BrewTimerViewModel()
        val readStates = mutableListOf<Any>()

        Snapshot.observe(readObserver = { readStates.add(it) }) {
            vm.currentStep // what the first (empty) composition does
        }

        assertTrue(
            "currentStep read no snapshot state; composition would never recompose after start()",
            readStates.isNotEmpty()
        )
    }
}
