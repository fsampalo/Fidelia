package es.uma.seguridad.puntoseguro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.uma.seguridad.puntoseguro.modelo.*

/* ==========================================================================
   PANTALLA DE DETALLE DE UN MOVIMIENTO
   Basada en la PantallaDetalle.kt del material de la P2.3, adaptada al dominio.

   Igual que la original, aquí NO hay ninguna comprobación de sesión: no se
   pregunta si hay token ni si el movimiento es tuyo. La única comprobación
   está en Repositorio.obtenerMovimiento() (paso 4 de la P2.3).
   ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    id: Int,
    estado: EstadoUi<Movimiento>,
    onCargar: (Int) -> Unit,
    onVolver: () -> Unit
) {
    // Basta con que la pantalla se abra para que se pida el movimiento
    LaunchedEffect(id) { onCargar(id) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Movimiento $id", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("Volver", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { relleno ->
        Box(
            modifier = Modifier.fillMaxSize().padding(relleno).padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Sin else: si se añade un estado nuevo, el compilador obliga a tratarlo
            when (estado) {
                is EstadoUi.Reposo -> CircularProgressIndicator(Modifier.padding(top = 48.dp))
                is EstadoUi.Cargando -> CircularProgressIndicator(Modifier.padding(top = 48.dp))

                is EstadoUi.Error -> Text(
                    text = mensajeDeError(estado.motivo),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 48.dp)
                )

                is EstadoUi.Exito -> Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Inicial(estado.datos.titulo, tamano = 52.dp)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                estado.datos.titulo,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        Text("Detalle", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(estado.datos.contenido, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        Text("Fecha", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(estado.datos.fecha, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

/**
 * Traduce el motivo del error a un texto para la persona que usa la app, sin
 * detalles internos (sesión 2.4.5).
 */
private fun mensajeDeError(motivo: MotivoError): String = when (motivo) {
    MotivoError.SinConexion -> "No se ha podido conectar. Inténtalo de nuevo."
    MotivoError.CredencialesNoValidas -> "Usuario o contraseña incorrectos."
    MotivoError.SesionCaducada -> "La sesión ha caducado. Vuelve a entrar."
    MotivoError.Desconocido -> "Ha ocurrido un error. Inténtalo de nuevo."
}
