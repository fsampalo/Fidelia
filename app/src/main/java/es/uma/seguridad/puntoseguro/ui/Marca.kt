package es.uma.seguridad.puntoseguro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Logotipo de Fidelia: un círculo de marca con una estrella de puntos. */
@Composable
fun LogoFidelia(tamano: Dp = 72.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "★",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = (tamano.value * 0.5f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Círculo con la inicial del movimiento, para las tarjetas del listado. */
@Composable
fun Inicial(texto: String, tamano: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto.trim().take(1).uppercase().ifEmpty { "·" },
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
