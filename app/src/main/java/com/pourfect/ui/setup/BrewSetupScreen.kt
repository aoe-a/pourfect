package com.pourfect.ui.setup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pourfect.domain.CustomRecipe
import com.pourfect.domain.GrindMethod
import com.pourfect.domain.GrinderCatalog
import com.pourfect.domain.GrinderInfo
import com.pourfect.domain.Preset
import com.pourfect.domain.PresetCatalog
import com.pourfect.domain.PresetInfo
import com.pourfect.domain.Strength
import com.pourfect.domain.Taste
import com.pourfect.data.SettingsRepository
import com.pourfect.data.UserSettings
import com.pourfect.ui.BrewSessionViewModel
import com.pourfect.ui.common.GradientButton
import com.pourfect.ui.common.formatGrams
import com.pourfect.ui.common.formatOunces
import com.pourfect.ui.common.formatRatio
import com.pourfect.ui.common.formatTempRange
import com.pourfect.ui.common.formatTime
import com.pourfect.ui.theme.Amber
import com.pourfect.ui.theme.Ember
import com.pourfect.ui.theme.Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrewSetupScreen(
    presetId: String,
    session: BrewSessionViewModel,
    onStartBrew: () -> Unit,
    onBack: () -> Unit,
    customRecipe: CustomRecipe? = null
) {
    val vm: BrewSetupViewModel = viewModel(key = customRecipe?.name ?: presetId) {
        if (customRecipe != null) {
            BrewSetupViewModel(
                presetInfo = PresetInfo(
                    preset = Preset.CLASSIC,
                    id = "custom",
                    title = customRecipe.name,
                    tagline = "Your recipe",
                    defaultRatio = customRecipe.ratio,
                    defaultIcePercent = 0,
                    supportsTasteStrength = false
                ),
                customRecipe = customRecipe
            )
        } else {
            BrewSetupViewModel(PresetCatalog.byId(presetId))
        }
    }
    val info = vm.presetInfo
    val settings by SettingsRepository.flow(LocalContext.current)
        .collectAsState(initial = UserSettings())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(info.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            StartBrewBar(vm = vm, session = session, onStartBrew = onStartBrew)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WaterCard(vm, showOunces = settings.showOunces)
            IceCard(vm)
            DoseCard(vm, showOunces = settings.showOunces)
            if (info.supportsTasteStrength) TasteStrengthCard(vm)
            HintsCard(
                vm,
                fahrenheit = settings.useFahrenheit,
                grinder = settings.myGrinderId?.let { GrinderCatalog.byId(it) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SetupCard(
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = color,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun CardLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
    )
}

// ---------- Total water ----------

@Composable
private fun WaterCard(vm: BrewSetupViewModel, showOunces: Boolean) {
    SetupCard {
        CardLabel("TOTAL WATER (DRINK SIZE, INCLUDES ICE)")
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = vm.totalWaterText,
                onValueChange = { new ->
                    if (new.length <= 4 && new.all { it.isDigit() }) vm.totalWaterText = new
                },
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = !vm.isValid,
                supportingText = when {
                    !vm.isValid -> {
                        { Text("Enter 100–1500 g") }
                    }
                    showOunces -> {
                        { Text("≈ ${formatOunces(vm.totalWater)}") }
                    }
                    else -> null
                },
                suffix = { Text("g", style = MaterialTheme.typography.titleLarge) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(180.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val quickAmounts = listOf(
                    250 to "250 g · 1 cup",
                    375 to "375 g · 1½ cups",
                    500 to "500 g · 2 cups"
                )
                for ((amount, label) in quickAmounts) {
                    SuggestionChip(
                        onClick = { vm.totalWaterText = amount.toString() },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

// ---------- Ice / hot water slider ----------

@Composable
private fun IceCard(vm: BrewSetupViewModel) {
    SetupCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardLabel("ICE ↔ HOT WATER")
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.AcUnit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${vm.params.iceGrams} g · ${vm.params.icePercent}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(Modifier.height(12.dp))

        // Two-color split bar: amber = hot water, icy blue = ice
        val params = vm.params
        val iceFraction by animateFloatAsState(
            targetValue = params.iceGrams.toFloat() / params.totalWater.coerceAtLeast(1),
            label = "iceFraction"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            if (iceFraction < 1f) {
                Box(
                    modifier = Modifier
                        .weight(1f - iceFraction)
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(
                                topStart = 8.dp, bottomStart = 8.dp,
                                topEnd = if (iceFraction <= 0.001f) 8.dp else 0.dp,
                                bottomEnd = if (iceFraction <= 0.001f) 8.dp else 0.dp
                            )
                        )
                )
            }
            if (iceFraction > 0.001f) {
                Box(
                    modifier = Modifier
                        .weight(iceFraction)
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row {
            Text(
                text = "${params.hotWaterGrams} g hot water",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${params.iceGrams} g ice",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Ice moves in 5 g steps up to half the drink
        val maxIce = (vm.totalWater / 2).coerceAtLeast(5)
        Slider(
            value = vm.params.iceGrams.toFloat(),
            onValueChange = { vm.iceGrams = ((it / 5).toInt() * 5).coerceIn(0, maxIce) },
            valueRange = 0f..maxIce.toFloat()
        )
        Text(
            text = if (vm.params.iceGrams == 0) {
                "All hot. A classic warm brew."
            } else {
                "Flash brew: the ice goes in the server and chills the coffee instantly as it drips."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Recommended split for an iced version of this recipe
        val recommendedIce = (vm.totalWater * vm.presetInfo.recommendedIcePercent / 100 / 5) * 5
        if (vm.isValid && recommendedIce > 0 && vm.params.iceGrams != recommendedIce) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Iced? Recommended: $recommendedIce g ice + " +
                        "${vm.totalWater - recommendedIce} g hot water",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { vm.iceGrams = recommendedIce }) {
                    Text("Apply")
                }
            }
        }
    }
}

// ---------- Ratio + dose ----------

@Composable
private fun DoseCard(vm: BrewSetupViewModel, showOunces: Boolean) {
    // The dose is the screen's hero output: it gets the only tinted card
    SetupCard(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
        CardLabel("COFFEE DOSE")
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${formatGrams(vm.params.coffeeGrams)} g",
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.linearGradient(listOf(Gold, Amber, Ember)),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = if (showOunces) {
                        "of freshly ground beans · ≈ ${formatOunces(vm.params.coffeeGrams)}"
                    } else {
                        "of freshly ground beans"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { vm.adjustRatio(-0.5) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Stronger ratio")
                    }
                    Text(
                        text = formatRatio(vm.ratio),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    FilledIconButton(
                        onClick = { vm.adjustRatio(0.5) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Weaker ratio")
                    }
                }
                Text(
                    text = "coffee : water",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ---------- 4:6 taste & strength ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasteStrengthCard(vm: BrewSetupViewModel) {
    SetupCard {
        CardLabel("TASTE — FIRST 40% OF THE WATER")
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val options = listOf(Taste.BRIGHTER, Taste.STANDARD, Taste.SWEETER)
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = vm.taste == option,
                    onClick = { vm.taste = option },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                ) {
                    Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        CardLabel("STRENGTH — LAST 60% OF THE WATER")
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val options = listOf(Strength.LIGHTER, Strength.STANDARD, Strength.STRONGER)
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = vm.strength == option,
                    onClick = { vm.strength = option },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                ) {
                    Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

// ---------- Grind & temperature hints ----------

@Composable
private fun HintsCard(vm: BrewSetupViewModel, fahrenheit: Boolean, grinder: GrinderInfo?) {
    // Hints are reference info, not controls: grouped by whitespace and a
    // hairline instead of yet another card.
    val iced = vm.params.iceGrams > 0
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        val baseGrind = if (iced) "Medium-fine, a touch finer than your hot brew"
        else "Medium-fine, like table salt"
        val grinderLine = grinder?.settings?.get(GrindMethod.V60)
            ?.let { "\n${grinder.model}: $it" } ?: ""
        HintRow(
            icon = Icons.Filled.Grain,
            title = "Grind",
            body = baseGrind + grinderLine
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outline
        )
        HintRow(
            icon = Icons.Filled.Thermostat,
            title = "Water temperature",
            body = if (iced) {
                "${formatTempRange(95, 100, fahrenheit)}, just off the boil. The ice does the cooling."
            } else {
                "${formatTempRange(92, 96, fahrenheit)}. Hotter for light roasts, cooler for dark."
            }
        )
    }
}

@Composable
private fun HintRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------- Start button ----------

@Composable
private fun StartBrewBar(
    vm: BrewSetupViewModel,
    session: BrewSessionViewModel,
    onStartBrew: () -> Unit
) {
    val steps = vm.steps
    Box(Modifier.padding(20.dp)) {
        val summary = if (steps.isEmpty()) "Start brewing"
        else "Start brewing · ${steps.size} steps · ~${formatTime(steps.last().endTime)}"
        GradientButton(
            text = summary,
            onClick = {
                session.startSession(vm.params, steps, vm.presetInfo.title)
                onStartBrew()
            },
            enabled = vm.isValid && steps.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
