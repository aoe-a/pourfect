package com.pourfect.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val PourfectDarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = OnAmber,
    primaryContainer = AmberDeep,
    onPrimaryContainer = Cream,
    secondary = IceBlue,
    onSecondary = OnIce,
    secondaryContainer = Surface3,
    onSecondaryContainer = IceBlue,
    background = Ink,
    onBackground = Cream,
    surface = Surface1,
    onSurface = Cream,
    surfaceVariant = Surface2,
    onSurfaceVariant = CreamDim,
    surfaceContainer = Surface2,
    surfaceContainerHigh = Surface3,
    outline = OutlineWarm,
    outlineVariant = OutlineWarm,
    error = ErrorRed
)

val PourfectShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PourfectTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PourfectDarkColors,
        typography = PourfectTypography,
        shapes = PourfectShapes,
        content = content
    )
}
