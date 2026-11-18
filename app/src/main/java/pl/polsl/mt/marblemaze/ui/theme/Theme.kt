package pl.polsl.mt.marblemaze.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = darkColorScheme(
    primary = Color(0xFF2E9E5B),
    onPrimary = Color.White,
    secondaryContainer = Color(0xFF4A3828),
    onSecondaryContainer = Color(0xFFF3E7D3),
    background = Color(0xFF2B211A),
    surface = Color(0xFF2B211A),
)

@Composable
fun MarbleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
