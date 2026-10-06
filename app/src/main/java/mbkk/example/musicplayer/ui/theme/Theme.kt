package mbkk.example.musicplayer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    secondary = EmeraldAccent,
    background = ObsidianBackground,
    surface = CharcoalSurface,
    surfaceVariant = TonalSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnPrimary,
    onBackground = TextPureWhite,
    onSurface = TextPureWhite,
    onSurfaceVariant = TextSlateMuted,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = DeepEmeraldAccent,
    secondary = DeepEmeraldAccent,
    background = AlabasterBackground,
    surface = LightSurface,
    surfaceVariant = LightTonalSurface,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnPrimary,
    onBackground = TextDeepSlate,
    onSurface = TextDeepSlate,
    onSurfaceVariant = TextMutedSlate,
    outline = LightOutline
)

@Suppress("DEPRECATION")
@Composable
fun MusicPlayerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
