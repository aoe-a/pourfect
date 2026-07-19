package com.pourfect.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pourfect.data.SettingsRepository
import com.pourfect.data.UserSettings
import com.pourfect.domain.GrinderCatalog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onGrinders: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by SettingsRepository.flow(context).collectAsState(initial = UserSettings())

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GrinderRow(
                currentModel = settings.myGrinderId
                    ?.let { GrinderCatalog.byId(it)?.model },
                onClick = onGrinders
            )
            SettingRow(
                icon = Icons.Filled.Vibration,
                title = "Vibration",
                subtitle = "Buzz at every pour step",
                checked = settings.vibrationEnabled,
                onToggle = { scope.launch { SettingsRepository.setVibration(context, it) } }
            )
            SettingRow(
                icon = Icons.Filled.MusicNote,
                title = "Sound",
                subtitle = "Chime at every pour step",
                checked = settings.soundEnabled,
                onToggle = { scope.launch { SettingsRepository.setSound(context, it) } }
            )
            SettingRow(
                icon = Icons.Filled.RecordVoiceOver,
                title = "Voice guidance",
                subtitle = "Read each pour step out loud",
                checked = settings.voiceEnabled,
                onToggle = { scope.launch { SettingsRepository.setVoiceEnabled(context, it) } }
            )
            SettingRow(
                icon = Icons.Filled.Scale,
                title = "Show ounces",
                subtitle = "Ounce equivalents next to gram weights",
                checked = settings.showOunces,
                onToggle = { scope.launch { SettingsRepository.setShowOunces(context, it) } }
            )
            SettingRow(
                icon = Icons.Filled.DeviceThermostat,
                title = "Fahrenheit",
                subtitle = "Water temperatures in °F instead of °C",
                checked = settings.useFahrenheit,
                onToggle = { scope.launch { SettingsRepository.setUseFahrenheit(context, it) } }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pourfect v1.0, brewed with V60 love.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GrinderRow(currentModel: String?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Coffee,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("My grinder", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = currentModel ?: "Not set. Pick yours for click recommendations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (currentModel != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
