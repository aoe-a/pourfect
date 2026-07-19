package com.pourfect.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pourfect.data.CustomRecipeRepository
import com.pourfect.domain.CustomRecipe
import com.pourfect.domain.PresetCatalog
import com.pourfect.domain.PresetInfo
import com.pourfect.ui.common.formatRatio
import com.pourfect.ui.theme.Amber
import com.pourfect.ui.theme.Cream
import com.pourfect.ui.theme.Gold
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onPresetSelected: (String) -> Unit,
    onSettings: () -> Unit,
    onJournal: () -> Unit,
    onCreateRecipe: () -> Unit,
    onCustomSelected: (CustomRecipe) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val customRecipes by CustomRecipeRepository.flow(context)
        .collectAsState(initial = emptyList())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Pourfect",
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.horizontalGradient(listOf(Cream, Gold, Amber)),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Your V60 pour-over companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onJournal) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = "Brew journal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (info in PresetCatalog.all) {
                PresetCard(info = info, onClick = { onPresetSelected(info.id) })
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "YOUR RECIPES",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (recipe in customRecipes) {
                CustomRecipeCard(
                    recipe = recipe,
                    onClick = { onCustomSelected(recipe) },
                    onDelete = {
                        scope.launch { CustomRecipeRepository.delete(context, recipe.name) }
                    }
                )
            }
            OutlinedButton(
                onClick = onCreateRecipe,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create your own recipe")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CustomRecipeCard(
    recipe: CustomRecipe,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${formatRatio(recipe.ratio)} · bloom ${recipe.bloomMultiplier}× · " +
                        "${recipe.pourCount} pours every ${recipe.intervalSeconds} s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete ${recipe.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun presetIcon(info: PresetInfo): ImageVector = when (info.id) {
    "classic" -> Icons.Filled.LocalCafe
    "hoffmann" -> Icons.Filled.Coffee
    "foursix" -> Icons.Filled.EmojiEvents
    "winton" -> Icons.Filled.WaterDrop
    "rao" -> Icons.Filled.Waves
    "osmotic" -> Icons.Filled.Science
    "iced" -> Icons.Filled.AcUnit
    else -> Icons.Filled.Science
}

/** Moody diagonal wash per preset; iced is the only cold one, by design. */
private fun presetGradient(info: PresetInfo): List<Color> = when (info.id) {
    "classic" -> listOf(Color(0xFF2B1F12), Color(0xFF191510))
    "hoffmann" -> listOf(Color(0xFF33240F), Color(0xFF1A1610))
    "foursix" -> listOf(Color(0xFF2E2410), Color(0xFF191510))
    "winton" -> listOf(Color(0xFF30200E), Color(0xFF181410))
    "rao" -> listOf(Color(0xFF2E1C12), Color(0xFF191411))
    "osmotic" -> listOf(Color(0xFF292110), Color(0xFF181510))
    "iced" -> listOf(Color(0xFF2B2012), Color(0xFF181510))
    else -> listOf(Color(0xFF1A1815), Color(0xFF1A1815))
}

@Composable
private fun PresetCard(info: PresetInfo, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    // Tactile press feedback: the card gives slightly under the finger
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "cardPress")
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
    ) {
        Box(Modifier.background(Brush.linearGradient(presetGradient(info)))) {
            // Oversized watermark glyph bleeding off the card edge
            Icon(
                imageVector = presetIcon(info),
                contentDescription = null,
                tint = accent.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 36.dp)
            )
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = presetIcon(info),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = info.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = info.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
