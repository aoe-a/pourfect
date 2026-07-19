package com.pourfect.ui.grinder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pourfect.data.SettingsRepository
import com.pourfect.data.UserSettings
import com.pourfect.domain.GrindMethod
import com.pourfect.domain.GrinderCatalog
import com.pourfect.domain.GrinderInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrinderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by SettingsRepository.flow(context).collectAsState(initial = UserSettings())
    var expandedId by remember { mutableStateOf<String?>(null) }

    val grouped = GrinderCatalog.all.groupBy { it.brand }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Grinder guide") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Pick your grinder to see starting points for every brew " +
                        "method. Numbers come from maker manuals and community charts; " +
                        "trust your taste over any chart.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            for ((brand, grinders) in grouped) {
                item(key = brand) {
                    Text(
                        text = brand.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(grinders, key = { it.id }) { grinder ->
                    GrinderCard(
                        grinder = grinder,
                        isMine = settings.myGrinderId == grinder.id,
                        expanded = expandedId == grinder.id,
                        onToggle = {
                            expandedId = if (expandedId == grinder.id) null else grinder.id
                        },
                        onSetMine = {
                            scope.launch { SettingsRepository.setMyGrinder(context, grinder.id) }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun GrinderCard(
    grinder: GrinderInfo,
    isMine: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSetMine: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.large,
        color = if (isMine) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = grinder.model,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "V60: ${grinder.settings[GrindMethod.V60]}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (isMine) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Your grinder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    for (method in GrindMethod.entries.filter { it != GrindMethod.V60 }) {
                        grinder.settings[method]?.let { setting ->
                            Row(Modifier.padding(vertical = 3.dp)) {
                                Text(
                                    text = method.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(140.dp)
                                )
                                Text(
                                    text = setting,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = grinder.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isMine) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = onSetMine, modifier = Modifier.fillMaxWidth()) {
                            Text("This is my grinder")
                        }
                    }
                }
            }
        }
    }
}
