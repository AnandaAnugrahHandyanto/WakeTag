package com.savarez.waketag.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = ColorTokens.onAmber,
    primaryContainer = ColorTokens.amberContainer,
    onPrimaryContainer = AmberSoft,
    secondary = Mist,
    onSecondary = Ink,
    secondaryContainer = InkSoft,
    onSecondaryContainer = Cream,
    tertiary = Mint,
    onTertiary = ColorTokens.onMint,
    error = Coral,
    onError = ColorTokens.onCoral,
    errorContainer = ColorTokens.coralContainer,
    onErrorContainer = ColorTokens.onCoralContainer,
    background = Ink,
    onBackground = Cream,
    surface = InkRaised,
    onSurface = Cream,
    surfaceVariant = InkSoft,
    onSurfaceVariant = Mist,
    outline = ColorTokens.outlineDark,
    outlineVariant = ColorTokens.outlineVariantDark,
    inverseSurface = Cream,
    inverseOnSurface = Ink,
    inversePrimary = AmberDeep,
    scrim = Ink
)

private val LightColors = lightColorScheme(
    primary = AmberDeep,
    onPrimary = ColorTokens.onAmberDark,
    primaryContainer = AmberSoft,
    onPrimaryContainer = ColorTokens.amberContainerText,
    secondary = Slate,
    onSecondary = PaperRaised,
    secondaryContainer = PaperSoft,
    onSecondaryContainer = ColorTokens.slateText,
    tertiary = ColorTokens.mintDark,
    onTertiary = PaperRaised,
    error = ColorTokens.coralDark,
    onError = PaperRaised,
    errorContainer = ColorTokens.coralContainerLight,
    onErrorContainer = ColorTokens.coralContainerText,
    background = Paper,
    onBackground = ColorTokens.slateText,
    surface = PaperRaised,
    onSurface = ColorTokens.slateText,
    surfaceVariant = PaperSoft,
    onSurfaceVariant = Slate,
    outline = ColorTokens.outlineLight,
    outlineVariant = ColorTokens.outlineVariantLight,
    inverseSurface = ColorTokens.slateText,
    inverseOnSurface = Paper,
    inversePrimary = Amber,
    scrim = Ink
)

/**
 * Token warna yang hanya dipakai di dalam tema.
 * Dipisah agar file Color.kt tetap berisi palet utama.
 */
private object ColorTokens {
    val onAmber = androidx.compose.ui.graphics.Color(0xFF2A1D00)
    val amberContainer = androidx.compose.ui.graphics.Color(0xFF4A3200)
    val onMint = androidx.compose.ui.graphics.Color(0xFF003829)
    val onCoral = androidx.compose.ui.graphics.Color(0xFF3B0906)
    val coralContainer = androidx.compose.ui.graphics.Color(0xFF5C1510)
    val onCoralContainer = androidx.compose.ui.graphics.Color(0xFFFFD9D4)
    val onAmberDark = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val amberContainerText = androidx.compose.ui.graphics.Color(0xFF2D1C00)
    val slateText = androidx.compose.ui.graphics.Color(0xFF1D232B)
    val mintDark = androidx.compose.ui.graphics.Color(0xFF2E6B54)
    val coralDark = androidx.compose.ui.graphics.Color(0xFFBA1A1A)
    val coralContainerLight = androidx.compose.ui.graphics.Color(0xFFFFDAD6)
    val coralContainerText = androidx.compose.ui.graphics.Color(0xFF410002)
    val outlineDark = androidx.compose.ui.graphics.Color(0xFF7A8794)
    val outlineVariantDark = androidx.compose.ui.graphics.Color(0xFF333E4A)
    val outlineLight = androidx.compose.ui.graphics.Color(0xFF7F8A96)
    val outlineVariantLight = androidx.compose.ui.graphics.Color(0xFFD5CBBB)
}

private val WakeTagShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun WakeTagTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = WakeTagShapes,
        content = content
    )
}
