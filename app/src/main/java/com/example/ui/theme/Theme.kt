package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = PureWhite,
    primaryContainer = EmeraldTint,
    onPrimaryContainer = DeepForestGreen,
    secondary = SoftGold,
    onSecondary = TextPrimaryLight,
    secondaryContainer = SoftGoldLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = DeepForestGreen,
    onTertiary = PureWhite,
    tertiaryContainer = EmeraldTint,
    onTertiaryContainer = DeepForestGreen,
    background = WarmIvory,
    onBackground = TextPrimaryLight,
    surface = PureWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = ColorTokens.SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight,
    outlineVariant = CardBorderLight,
    error = AlertRedLight,
    onError = PureWhite,
    errorContainer = AlertRedContainerLight,
    onErrorContainer = AlertRedLight
)

private val DarkColors = darkColorScheme(
    primary = EmeraldDarkPrimary,
    onPrimary = DarkForestBackground,
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = TextPrimaryDark,
    secondary = SoftGoldDark,
    onSecondary = DarkForestBackground,
    secondaryContainer = SoftGoldDarkContainer,
    onSecondaryContainer = SoftGoldLight,
    tertiary = SoftGoldBright,
    onTertiary = DarkForestBackground,
    tertiaryContainer = DarkSurfaceElevated,
    onTertiaryContainer = TextPrimaryDark,
    background = DarkForestBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorder,
    error = AlertRedDark,
    onError = DarkForestBackground,
    errorContainer = AlertRedContainerDark,
    onErrorContainer = AlertRedDark
)

private object ColorTokens {
    val SurfaceVariantLight = androidx.compose.ui.graphics.Color(0xFFEAF1EC)
}

val AmarJibonShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AmarJibonTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AmarJibonTypography,
        shapes = AmarJibonShapes,
        content = content
    )
}
