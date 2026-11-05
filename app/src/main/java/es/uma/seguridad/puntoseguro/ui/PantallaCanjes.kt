package es.uma.seguridad.puntoseguro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.uma.seguridad.puntoseguro.modelo.*

/**
 * Canje de puntos por códigos de descuento.
 *
 * La pantalla sólo muestra y pide: el saldo, el catálogo y los códigos llegan
 * del servidor, y es el servidor quien decide si hay puntos suficientes. El
 * botón deshabilitado cuando no llega el saldo es una ayuda para el usuario,
 * no un control de seguridad (sesión 2.5).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCanjes(
    saldo: EstadoUi<Int>,
    catalogo: EstadoUi<List<Recompensa>>,
    canjes: EstadoUi<List<Canje>>,
    resultado: ResultadoCanje?,
    onCargar: () -> Unit,
    onCanjear: (Int) -> Unit,
    onOlvidarResultado: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) { onCargar() }

    val puntos = (saldo as? EstadoUi.Exito)?.datos

    // Aviso con el resultado del último canje
    resultado?.let { r ->
        AlertDialog(
            onDismissRequest = onOlvidarResultado,
            confirmButton = { TextButton(onClick = onOlvidarResultado) { Text("Aceptar") } },
            title = {
                Text(
                    when (r) {
                        is ResultadoCanje.Hecho -> "Código canjeado"
                        ResultadoCanje.SaldoInsuficiente -> "Saldo insuficiente"
                        is ResultadoCanje.Fallo -> "No se ha podido canjear"
                    }
                )
            },
            text = {
                when (r) {
                    is ResultadoCanje.Hecho -> Column {
                        Text(r.canje.descripcion)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            r.canje.codigo,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    ResultadoCanje.SaldoInsuficiente -> Text("No tienes puntos suficientes para esta recompensa.")
                    is ResultadoCanje.Fallo -> Text(textoMotivo(r.motivo))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Canjear puntos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("Volver", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(relleno),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Tienes", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            puntos?.let { "$it pts" } ?: "— pts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Titulo("Recompensas") }
            when (catalogo) {
                is EstadoUi.Reposo, is EstadoUi.Cargando -> item { CircularProgressIndicator() }
                is EstadoUi.Error -> item {
                    Text(
                        "No se ha podido cargar el catálogo. El canje necesita el backend " +
                            "de Fidelia (backend/servidor_fidelia.py).",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is EstadoUi.Exito -> items(catalogo.datos) { recompensa ->
                    TarjetaRecompensa(
                        recompensa = recompensa,
                        alcanza = puntos != null && puntos >= recompensa.puntos,
                        onCanjear = { onCanjear(recompensa.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)); Titulo("Mis códigos") }
            when (canjes) {
                is EstadoUi.Reposo, is EstadoUi.Cargando -> item { CircularProgressIndicator() }
                is EstadoUi.Error -> item {
                    Text("Todavía no tienes códigos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is EstadoUi.Exito ->
                    if (canjes.datos.isEmpty()) item {
                        Text("Todavía no tienes códigos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else items(canjes.datos) { canje -> TarjetaCodigo(canje) }
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun TarjetaRecompensa(recompensa: Recompensa, alcanza: Boolean, onCanjear: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(recompensa.descripcion, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${recompensa.puntos} pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onCanjear,
                enabled = alcanza,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) { Text("Canjear") }
        }
    }
}

@Composable
private fun TarjetaCodigo(canje: Canje) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                canje.codigo,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "${canje.descripcion} · ${canje.puntos} pts · ${canje.fecha}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun textoMotivo(motivo: MotivoError): String = when (motivo) {
    MotivoError.SinConexion -> "No se ha podido conectar. Inténtalo de nuevo."
    MotivoError.CredencialesNoValidas -> "Usuario o contraseña incorrectos."
    MotivoError.SesionCaducada -> "La sesión ha caducado. Vuelve a entrar."
    MotivoError.Desconocido -> "Ha ocurrido un error. Inténtalo de nuevo."
}
