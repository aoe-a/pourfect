package com.pourfect.ui.brew

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pourfect.domain.PourStep
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drives the guided brew. Time is measured with [SystemClock.elapsedRealtime]
 * so the timer stays accurate no matter how often we tick or recompose.
 * The clock is injectable so tests can control time.
 */
class BrewTimerViewModel @JvmOverloads constructor(
    private val clock: () -> Long = SystemClock::elapsedRealtime
) : ViewModel() {

    var steps: List<PourStep> by mutableStateOf(emptyList())
        private set

    var elapsedMs by mutableLongStateOf(0L)
        private set
    var isRunning by mutableStateOf(false)
        private set
    var isFinished by mutableStateOf(false)
        private set

    private var accumulatedMs = 0L
    private var runningSinceRealtime = 0L
    private var ticker: Job? = null

    val elapsedSeconds: Int get() = (elapsedMs / 1000).toInt()

    val totalSeconds: Int get() = steps.lastOrNull()?.endTime ?: 0

    val currentStepIndex: Int
        get() = steps.indexOfFirst { elapsedSeconds < it.endTime }
            .let { if (it == -1) steps.lastIndex else it }

    val currentStep: PourStep? get() = steps.getOrNull(currentStepIndex)

    val nextStep: PourStep? get() = steps.getOrNull(currentStepIndex + 1)

    /** 0..1 progress through the current step, for the ring. */
    val stepProgress: Float
        get() {
            val step = currentStep ?: return 0f
            val durationMs = (step.endTime - step.startTime) * 1000f
            val intoStepMs = elapsedMs - step.startTime * 1000f
            return (intoStepMs / durationMs).coerceIn(0f, 1f)
        }

    /** True while the current step is in its active pouring phase. */
    val inPourPhase: Boolean
        get() {
            val step = currentStep ?: return false
            val pour = step.pourSeconds ?: return false
            return elapsedSeconds - step.startTime < pour
        }

    /** Fraction of the current step occupied by pouring; 0 when none. */
    val pourPhaseFraction: Float
        get() {
            val step = currentStep ?: return 0f
            val pour = step.pourSeconds ?: return 0f
            return pour.toFloat() / (step.endTime - step.startTime)
        }

    fun start(steps: List<PourStep>) {
        if (this.steps.isNotEmpty()) return // already started for this brew
        this.steps = steps
        resume()
    }

    fun pause() {
        if (!isRunning) return
        accumulatedMs += clock() - runningSinceRealtime
        isRunning = false
        ticker?.cancel()
    }

    fun resume() {
        if (isRunning || isFinished || steps.isEmpty()) return
        runningSinceRealtime = clock()
        isRunning = true
        ticker = viewModelScope.launch {
            while (isActive) {
                elapsedMs = accumulatedMs + (clock() - runningSinceRealtime)
                if (elapsedSeconds >= totalSeconds) {
                    isRunning = false
                    isFinished = true
                    break
                }
                delay(100)
            }
        }
    }

    /** Swipe left: jump to the start of the next step (or finish the brew). */
    fun skipForward() = seekToStep(currentStepIndex + 1)

    /** Swipe right: jump back to the start of the previous step. */
    fun skipBack() = seekToStep(currentStepIndex - 1)

    private fun seekToStep(index: Int) {
        if (isFinished || steps.isEmpty()) return
        if (index > steps.lastIndex) {
            ticker?.cancel()
            elapsedMs = totalSeconds * 1000L
            isRunning = false
            isFinished = true
            return
        }
        val targetMs = steps[index.coerceAtLeast(0)].startTime * 1000L
        accumulatedMs = targetMs
        runningSinceRealtime = clock()
        elapsedMs = targetMs
    }

    override fun onCleared() {
        ticker?.cancel()
    }
}
