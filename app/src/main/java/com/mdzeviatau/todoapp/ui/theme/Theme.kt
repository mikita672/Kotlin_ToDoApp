package com.mdzeviatau.todoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.mdzeviatau.todoapp.data.repository.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = GruvboxYellow,
    onPrimary = GruvboxBg0,
    primaryContainer = GruvboxBg1,
    onPrimaryContainer = GruvboxFg1,
    secondary = GruvboxBlue,
    onSecondary = GruvboxBg0,
    tertiary = GruvboxAqua,
    onTertiary = GruvboxBg0,
    background = GruvboxBg0,
    onBackground = GruvboxFg1,
    surface = GruvboxBg1,
    onSurface = GruvboxFg1,
    surfaceVariant = GruvboxBg2,
    onSurfaceVariant = GruvboxFg2,
    error = GruvboxRed,
    onError = GruvboxBg0
)

private val LightColorScheme = lightColorScheme(
    primary = GruvboxYellow,
    onPrimary = GruvboxLightBg0,
    primaryContainer = GruvboxLightBg1,
    onPrimaryContainer = GruvboxLightFg0,
    secondary = GruvboxBlue,
    onSecondary = GruvboxLightBg0,
    tertiary = GruvboxAqua,
    onTertiary = GruvboxLightBg0,
    background = GruvboxLightBg0,
    onBackground = GruvboxLightFg0,
    surface = GruvboxLightBg1,
    onSurface = GruvboxLightFg0,
    surfaceVariant = GruvboxLightBg1,
    onSurfaceVariant = GruvboxLightFg1,
    error = GruvboxRed,
    onError = GruvboxLightBg0
)

@Composable
fun ToDoAppTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme, shapes = Shapes, typography = Typography, content = content
    )
}
