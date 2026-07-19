package com.pourfect.ui.builder

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pourfect.data.CustomRecipeRepository
import com.pourfect.domain.BrewParams
import com.pourfect.domain.CustomRecipe
import com.pourfect.domain.Preset
import com.pourfect.domain.RecipeGenerator
import com.pourfect.ui.common.GradientButton
import com.pourfect.ui.common.formatRatio
import com.pourfect.ui.common.formatTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBuilderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var ratio by remember { mutableDoubleStateOf(15.0) }
    var bloomMultiplier by remember { mutableIntStateOf(2) }
    var pourCount by remember { mutableIntStateOf(3) }
    var intervalSeconds by remember { mutableIntStateOf(45) }

    val recipe = CustomRecipe(name.trim(), ratio, bloomMultiplier, pourCount, intervalSeconds)
    // Preview the schedule for a nominal 300 g brew
    val previewTime = RecipeGenerator.generateCustom(
        recipe.copy(name = "preview"),
        BrewParams(preset = Preset.CLASSIC, totalWater = 300, ratio = ratio)
    ).last().endTime

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Create your recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Column(Modifier.padding(20.dp)) {
                GradientButton(
                    text = "Save recipe · ${pourCount + 1} pours · ~${formatTime(previewTime)}",
                    enabled = name.isNotBlank(),
                    onClick = {
                        scope.launch {
                            CustomRecipeRepository.save(context, recipe)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
            BuilderCard {
                BuilderLabel("NAME")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { new ->
                        if (new.length <= 24) name = new.replace("|", "").replace("\n", "")
                    },
                    placeholder = { Text("e.g. Sunday Slow Brew") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BuilderCard {
                BuilderLabel("RATIO (COFFEE : WATER)")
                Spacer(Modifier.height(8.dp))
                Stepper(
                    value = formatRatio(ratio),
                    onMinus = { ratio = (ratio - 0.5).coerceAtLeast(13.0) },
                    onPlus = { ratio = (ratio + 0.5).coerceAtMost(18.0) }
                )
            }

            BuilderCard {
                BuilderLabel("BLOOM SIZE")
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(2, 3).forEachIndexed { index, mult ->
                        SegmentedButton(
                            selected = bloomMultiplier == mult,
                            onClick = { bloomMultiplier = mult },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) {
                            Text("$mult× the dose")
                        }
                    }
                }
            }

            BuilderCard {
                BuilderLabel("POURS AFTER THE BLOOM")
                Spacer(Modifier.height(8.dp))
                Stepper(
                    value = "$pourCount",
                    onMinus = { pourCount = (pourCount - 1).coerceAtLeast(1) },
                    onPlus = { pourCount = (pourCount + 1).coerceAtMost(6) }
                )
            }

            BuilderCard {
                BuilderLabel("SECONDS BETWEEN POURS")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (seconds in listOf(30, 40, 45, 60)) {
                        FilterChip(
                            selected = intervalSeconds == seconds,
                            onClick = { intervalSeconds = seconds },
                            label = { Text("$seconds s") }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BuilderCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun BuilderLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Stepper(value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledIconButton(
            onClick = onMinus,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        FilledIconButton(
            onClick = onPlus,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase")
        }
        Spacer(Modifier.width(4.dp))
    }
}
