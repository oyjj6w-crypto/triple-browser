package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val TerminalDarkColorScheme = darkColorScheme(
  primary = ChartCyan,
  onPrimary = Color(0xFF00363D),
  primaryContainer = Color(0xFF004F58),
  onPrimaryContainer = Color(0xFF9EEFFF),
  secondary = ChartBlue,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF00438F),
  onSecondaryContainer = Color(0xFFD6E3FF),
  tertiary = ChartGreen,
  onTertiary = Color(0xFF003919),
  background = TerminalBackground,
  onBackground = TextPrimaryDark,
  surface = TerminalSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = TerminalSurfaceVariant,
  onSurfaceVariant = TextSecondaryDark,
  outline = TerminalBorder
)

private val TerminalLightColorScheme = lightColorScheme(
  primary = ChartBlue,
  onPrimary = Color.White,
  secondary = ChartCyan,
  onSecondary = Color.Black,
  background = LightBackground,
  onBackground = TextPrimaryLight,
  surface = LightSurface,
  onSurface = TextPrimaryLight,
  outline = LightBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek dark theme for trading
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> TerminalDarkColorScheme
    else -> TerminalLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

