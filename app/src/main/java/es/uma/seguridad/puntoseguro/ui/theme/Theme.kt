package es.uma.seguridad.puntoseguro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeOscuro,
    secondary = Ambar,
    onSecondary = Color.Black,
    secondaryContainer = AmbarClaro,
    onSecondaryContainer = AmbarOscuro,
    background = Fondo,
    onBackground = Texto,
    surface = Superficie,
    onSurface = Texto,
    surfaceVariant = Fondo,
    onSurfaceVariant = TextoSuave
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeNoche,
    onPrimary = VerdeOscuro,
    primaryContainer = VerdeOscuro,
    onPrimaryContainer = VerdeClaro,
    secondary = Ambar,
    onSecondary = Color.Black,
    secondaryContainer = AmbarOscuro,
    onSecondaryContainer = AmbarClaro,
    background = FondoNoche,
    onBackground = TextoNoche,
    surface = SuperficieNoche,
    onSurface = TextoNoche,
    surfaceVariant = SuperficieNoche,
    onSurfaceVariant = VerdeClaro
)

/**
 * Tema de Fidelia. Se desactiva el color dinámico de Android 12+ para que
 * la aplicación use siempre los colores de la marca.
 */
@Composable
fun PuntoSeguroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
