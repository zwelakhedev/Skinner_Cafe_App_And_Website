package za.co.skinnercafe.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CafeColors(
    val green: Color,
    val darkGreen: Color,
    val cream: Color,
    val white: Color,
    val text: Color,
    val muted: Color,
    val sage: Color,
    val gold: Color
)

private val LightCafeColors = CafeColors(
    green = Color(0xFF315744),
    darkGreen = Color(0xFF183D2C),
    cream = Color(0xFFF7F4EC),
    white = Color(0xFFFFFEFA),
    text = Color(0xFF292D27),
    muted = Color(0xFF77796F),
    sage = Color(0xFFE4E9DF),
    gold = Color(0xFFD8BB83)
)

private val DarkCafeColors = CafeColors(
    green = Color(0xFF9ABFA6),
    darkGreen = Color(0xFFB8D0BD),
    cream = Color(0xFF171E19),
    white = Color(0xFF222C25),
    text = Color(0xFFE8E9E1),
    muted = Color(0xFFBBC6BC),
    sage = Color(0xFF35443A),
    gold = Color(0xFFD8BB83)
)

val LocalCafeColors = staticCompositionLocalOf {
    LightCafeColors
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF315744),
    onPrimary = Color.White,
    secondary = Color(0xFFD8BB83),
    onSecondary = Color(0xFF292D27),
    tertiary = Color(0xFF183D2C),
    background = Color(0xFFF7F4EC),
    onBackground = Color(0xFF292D27),
    surface = Color(0xFFFFFEFA),
    onSurface = Color(0xFF292D27),
    surfaceVariant = Color(0xFFE4E9DF),
    onSurfaceVariant = Color(0xFF77796F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ABFA6),
    onPrimary = Color(0xFF183D2C),
    secondary = Color(0xFFD8BB83),
    onSecondary = Color(0xFF292D27),
    tertiary = Color(0xFFB8D0BD),
    background = Color(0xFF171E19),
    onBackground = Color(0xFFE8E9E1),
    surface = Color(0xFF222C25),
    onSurface = Color(0xFFE8E9E1),
    surfaceVariant = Color(0xFF35443A),
    onSurfaceVariant = Color(0xFFBBC6BC)
)

@Composable
fun SkinnerCafeTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val cafeColors = if (darkTheme) {
        DarkCafeColors
    } else {
        LightCafeColors
    }

    CompositionLocalProvider(
        LocalCafeColors provides cafeColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
