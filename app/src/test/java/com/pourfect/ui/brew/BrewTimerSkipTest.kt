package com.pourfect.ui.brew

import com.pourfect.domain.BrewParams
import com.pourfect.domain.Preset
import com.pourfect.domain.RecipeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrewTimerSkipTest {

    @Before
    fun setUp() {
        // Queue (and never run) the ticker coroutine so time is fully manual
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** 4:6 standard: pours start at 0, 45, 90, 135, 180 + drawdown at 225. */
    private fun startedVm(): BrewTimerViewModel {
        val steps = RecipeGenerator.generate(
            BrewParams(preset = Preset.FOUR_SIX, totalWater = 300, ratio = 15.0)
        )
        return BrewTimerViewModel(clock = { 0L }).also { it.start(steps) }
    }

    @Test
    fun `swipe forward jumps to the start of the next step`() {
        val vm = startedVm()
        vm.skipForward()
        assertEquals(1, vm.currentStepIndex)
        assertEquals(45, vm.elapsedSeconds)
    }

    @Test
    fun `swipe back returns to the start of the previous step`() {
        val vm = startedVm()
        vm.skipForward()
        vm.skipForward()
        vm.skipBack()
        assertEquals(1, vm.currentStepIndex)
        assertEquals(45, vm.elapsedSeconds)
    }

    @Test
    fun `swipe back on the first step stays at the beginning`() {
        val vm = startedVm()
        vm.skipBack()
        assertEquals(0, vm.currentStepIndex)
        assertEquals(0, vm.elapsedSeconds)
    }

    @Test
    fun `swiping forward past the last step finishes the brew`() {
        val vm = startedVm()
        repeat(vm.steps.size) { vm.skipForward() }
        assertTrue(vm.isFinished)
        assertFalse(vm.isRunning)
    }

    @Test
    fun `skipping while paused keeps the timer at the step start`() {
        val vm = startedVm()
        vm.pause()
        vm.skipForward()
        assertEquals(45, vm.elapsedSeconds)
        assertFalse(vm.isRunning)
    }
}
