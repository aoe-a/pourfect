package com.pourfect.ui.brew

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pourfect.data.BrewJournalRepository
import com.pourfect.data.SettingsRepository
import com.pourfect.data.UserSettings
import com.pourfect.domain.BrewLogEntry
import com.pourfect.domain.DialInAdvisor
import com.pourfect.domain.GrinderCatalog
import com.pourfect.domain.GrinderInfo
import com.pourfect.domain.PourStep
import com.pourfect.domain.TasteVerdict
import com.pourfect.ui.BrewSessionViewModel
import com.pourfect.ui.common.GradientButton
import com.pourfect.ui.common.formatGrams
import com.pourfect.ui.common.formatOunces
import com.pourfect.ui.common.formatTime
import com.pourfect.ui.theme.Amber
import com.pourfect.ui.theme.AmberDeep
import com.pourfect.ui.theme.Ember
import com.pourfect.ui.theme.Gold
import kotlinx.coroutines.launch

@Composable
fun GuidedBrewScreen(
    session: BrewSessionViewModel,
    onExit: () -> Unit
) {
    val vm: BrewTimerViewModel = viewModel()
    val steps = session.steps

    if (steps.isEmpty()) {
        // e.g. process restored mid-brew; there is nothing to resume
        EmptyState(onExit)
        return
    }

    // Iced brews wait on a prep screen until the ice is actually in the server
    val needsIcePrep = (session.params?.iceGrams ?: 0) > 0
    LaunchedEffect(Unit) { if (!needsIcePrep) vm.start(steps) }

    // Keep the screen awake for the whole brew
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Vibrate, chime, and announce when the step changes (not on first composition)
    val context = LocalContext.current
    val settings by SettingsRepository.flow(context).collectAsState(initial = UserSettings())

    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { /* speaks with the default engine voice */ }
        tts.value = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    var lastSeenStep by remember { mutableIntStateOf(-1) }
    LaunchedEffect(vm.currentStepIndex, vm.isFinished) {
        if (lastSeenStep != -1 && (vm.currentStepIndex != lastSeenStep || vm.isFinished)) {
            if (settings.vibrationEnabled) context.vibrate(if (vm.isFinished) 600 else 250)
            if (settings.voiceEnabled) {
                val phrase = if (vm.isFinished) {
                    "Brew finished. Enjoy your coffee."
                } else {
                    vm.currentStep?.let { step ->
                        buildString {
                            append(step.label)
                            step.targetWeight?.let { append(". Scale to $it grams.") }
                        }
                    } ?: ""
                }
                tts.value?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "pourfect-step")
            } else if (settings.soundEnabled) {
                playChime(finish = vm.isFinished)
            }
        }
        lastSeenStep = vm.currentStepIndex
    }

    // A short nudge when the pouring phase of a step ends: stop pouring, let it drip
    var wasPouring by remember { mutableStateOf(false) }
    LaunchedEffect(vm.inPourPhase, vm.currentStepIndex) {
        if (wasPouring && !vm.inPourPhase && !vm.isFinished && vm.currentStepIndex == lastSeenStep) {
            if (settings.vibrationEnabled) context.vibrate(120)
            if (settings.voiceEnabled) {
                tts.value?.speak("Stop pouring. Let it drip.", TextToSpeech.QUEUE_ADD, null, "pourfect-drip")
            }
        }
        wasPouring = vm.inPourPhase
    }

    when {
        vm.isFinished -> FinishedContent(
            session, vm, onExit,
            myGrinder = settings.myGrinderId?.let { GrinderCatalog.byId(it) }
        )
        vm.steps.isEmpty() && needsIcePrep -> IcePrepContent(
            iceGrams = session.params?.iceGrams ?: 0,
            onStart = { vm.start(steps) },
            onExit = onExit
        )
        vm.steps.isEmpty() -> Unit // starting momentarily
        else -> BrewingContent(vm, onExit, showOunces = settings.showOunces)
    }
}

// ---------- Ice prep gate: the timer waits until the ice is in ----------

@Composable
private fun IcePrepContent(iceGrams: Int, onStart: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onExit) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Cancel brew",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            modifier = Modifier.size(96.dp)
        ) {
            Icon(
                Icons.Filled.AcUnit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(24.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Add $iceGrams g of ice",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Put it in the server or carafe under the dripper. " +
                "The hot brew will flash-chill as it drips onto it. " +
                "The timer only starts when you are ready.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.weight(1f))
        GradientButton(
            text = "Ice is in · Start brewing",
            icon = Icons.Filled.PlayArrow,
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
    }
}

// ---------- Active brew ----------

@Composable
private fun BrewingContent(vm: BrewTimerViewModel, onExit: () -> Unit, showOunces: Boolean) {
    val step = vm.currentStep ?: return

    // Swipe left = next step, swipe right = previous step
    var dragTotal by remember { mutableFloatStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                val threshold = 90.dp.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onDragEnd = {
                        when {
                            dragTotal < -threshold -> vm.skipForward()
                            dragTotal > threshold -> vm.skipBack()
                        }
                    }
                ) { _, dragAmount -> dragTotal += dragAmount }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Abort brew",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Step ${vm.currentStepIndex + 1} of ${vm.steps.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp)) // balance the close button
        }

        Spacer(Modifier.height(8.dp))
        StepDots(vm.steps, vm.currentStepIndex)
        Spacer(Modifier.weight(1f))

        TimerRing(vm, showOunces = showOunces)

        Spacer(Modifier.weight(1f))

        // Slide the step text in the direction of travel (matches the swipe)
        AnimatedContent(
            targetState = vm.currentStepIndex,
            transitionSpec = {
                val forward = targetState > initialState
                val enter = slideInHorizontally { if (forward) it / 3 else -it / 3 } + fadeIn()
                val exit = slideOutHorizontally { if (forward) -it / 3 else it / 3 } + fadeOut()
                enter togetherWith exit
            },
            label = "stepText"
        ) { index ->
            val shownStep = vm.steps.getOrNull(index) ?: step
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = shownStep.label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = shownStep.instruction,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        vm.nextStep?.let { next ->
            Text(
                text = "Next: ${next.label} at ${formatTime(next.startTime)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "‹ swipe to skip steps ›",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = if (vm.isRunning) "Pause" else "Resume",
            icon = if (vm.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            onClick = { if (vm.isRunning) vm.pause() else vm.resume() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TimerRing(vm: BrewTimerViewModel, showOunces: Boolean) {
    // Two ring phases in the same amber family: bright gold-to-amber while
    // pouring, flowing into a deep bronze while the bed drips
    val ringColor = MaterialTheme.colorScheme.primary
    val dripColor = AmberDeep
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val progress by animateFloatAsState(
        targetValue = vm.stepProgress,
        animationSpec = tween(durationMillis = 120),
        label = "ringProgress"
    )
    val pourFraction = vm.pourPhaseFraction
    val glowColor by animateColorAsState(
        targetValue = if (vm.inPourPhase) ringColor else dripColor,
        label = "glowColor"
    )
    val target = vm.currentStep?.targetWeight

    // The ring breathes while brewing: a slow pulse of warm light behind it
    val breath = rememberInfiniteTransition(label = "breath")
    val glowAlpha by breath.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(320.dp)
            .background(
                Brush.radialGradient(
                    listOf(glowColor.copy(alpha = if (vm.isRunning) glowAlpha else 0.08f), Color.Transparent)
                )
            )
    ) {
        Canvas(modifier = Modifier.size(260.dp)) {
            val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            // Amber arc while pouring; the arc continues in muted cream
            // while the bed drips (steps without a pour are all drip)
            rotate(degrees = -90f) {
                // One arc, two phases: bright gold-to-amber over the pouring
                // portion, easing into deep bronze for the drip portion.
                // The last stops return to the start color: the round stroke
                // cap samples the gradient just before 0 degrees (wrapped to
                // ~360), and without this it dips into bronze and looks muddy.
                val arcBrush = if (pourFraction > 0f) {
                    Brush.sweepGradient(
                        0f to Gold,
                        pourFraction * 0.85f to Amber,
                        (pourFraction + 0.08f).coerceAtMost(0.9f) to AmberDeep,
                        0.94f to AmberDeep,
                        1f to Gold
                    )
                } else {
                    // steps with no pouring (stir, swirl, drawdown) are all drip
                    Brush.sweepGradient(
                        0f to AmberDeep.copy(alpha = 0.55f),
                        0.94f to AmberDeep,
                        1f to AmberDeep.copy(alpha = 0.55f)
                    )
                }
                drawArc(
                    brush = arcBrush,
                    startAngle = 0f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = stroke
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(vm.elapsedSeconds),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            if (vm.currentStep?.pourSeconds != null) {
                Text(
                    text = if (vm.inPourPhase) "POUR NOW" else "LET IT DRIP",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (vm.inPourPhase) ringColor else dripColor
                )
            }
            if (target != null) {
                val ozSuffix = if (showOunces) " · ${formatOunces(target)}" else ""
                Text(
                    text = "scale → $target g$ozSuffix",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            vm.currentStep?.let { step ->
                val remaining = (step.endTime - vm.elapsedSeconds).coerceAtLeast(0)
                Text(
                    text = "ends at ${formatTime(step.endTime)} · ${formatTime(remaining)} left",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepDots(steps: List<PourStep>, currentIndex: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, _ ->
            val color = when {
                index < currentIndex -> MaterialTheme.colorScheme.primary
                index == currentIndex -> MaterialTheme.colorScheme.onBackground
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
            }
            Surface(
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(if (index == currentIndex) 10.dp else 8.dp)
            ) {}
        }
    }
}

// ---------- Finished ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinishedContent(
    session: BrewSessionViewModel,
    vm: BrewTimerViewModel,
    onExit: () -> Unit,
    myGrinder: GrinderInfo?
) {
    val params = session.params
    var verdict by remember { mutableStateOf<TasteVerdict?>(null) }
    var grindNote by remember { mutableStateOf("") }

    // Log the finished brew once; the verdict chips update the same entry
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logTimestamp = remember { System.currentTimeMillis() }
    LaunchedEffect(Unit) {
        if (params != null) {
            BrewJournalRepository.add(
                context,
                BrewLogEntry(
                    timestampMillis = logTimestamp,
                    recipeName = session.recipeName.ifBlank { "V60 brew" },
                    totalWater = params.totalWater,
                    icePercent = params.icePercent,
                    ratio = params.ratio,
                    coffeeGrams = params.coffeeGrams
                )
            )
        }
    }

    // The checkmark pops in with a springy overshoot
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                }
        ) {
            Icon(
                Icons.Filled.Done,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(24.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Enjoy your coffee!",
            style = MaterialTheme.typography.headlineMedium.copy(
                brush = Brush.horizontalGradient(listOf(Gold, Amber, Ember))
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Brewed in ${formatTime(vm.elapsedSeconds)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (params != null) {
            Spacer(Modifier.height(8.dp))
            val recap = buildString {
                append("${formatGrams(params.coffeeGrams)} g coffee · ${params.hotWaterGrams} g hot water")
                if (params.iceGrams > 0) append(" · ${params.iceGrams} g ice")
            }
            Text(
                text = recap,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(28.dp))

        // Dial-in feedback: taste verdict → concrete advice for next time
        Text(
            text = "How did it taste?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (option in TasteVerdict.entries) {
                FilterChip(
                    selected = verdict == option,
                    onClick = {
                        verdict = option
                        scope.launch {
                            BrewJournalRepository.setVerdict(context, logTimestamp, option)
                        }
                    },
                    label = { Text(verdictLabel(option)) }
                )
            }
        }

        if (verdict != null && params != null) {
            val advice = DialInAdvisor.advise(verdict!!, params)
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = advice.headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    for (action in advice.actions) {
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (myGrinder != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = grindNote,
                onValueChange = { note ->
                    grindNote = note.replace("|", "").replace("\n", "")
                    scope.launch {
                        BrewJournalRepository.setGrind(
                            context, logTimestamp,
                            grinderName = myGrinder.model,
                            grindSetting = grindNote.trim().ifEmpty { null }
                        )
                    }
                },
                label = { Text("Grind setting used on ${myGrinder.model}") },
                placeholder = { Text("e.g. 18 clicks") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(28.dp))
        GradientButton(
            text = "Brew again",
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }
}

private fun verdictLabel(verdict: TasteVerdict): String = when (verdict) {
    TasteVerdict.PERFECT -> "Perfect"
    TasteVerdict.BITTER -> "Bitter"
    TasteVerdict.SOUR -> "Sour"
    TasteVerdict.WEAK -> "Weak"
    TasteVerdict.ASTRINGENT -> "Harsh"
}

@Composable
private fun EmptyState(onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No brew in progress",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onExit) { Text("Back to recipes") }
    }
}

// ---------- Sound ----------

private suspend fun playChime(finish: Boolean) {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    try {
        if (finish) {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500)
            kotlinx.coroutines.delay(600)
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            kotlinx.coroutines.delay(250)
        }
    } finally {
        toneGenerator.release()
    }
}

// ---------- Haptics ----------

private fun Context.vibrate(milliseconds: Long) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
}
