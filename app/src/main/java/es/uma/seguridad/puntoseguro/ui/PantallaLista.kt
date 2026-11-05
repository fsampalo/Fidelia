package es.uma.seguridad.puntoseguro.ui

import android.Manifest
import android.graphics.Bitmap
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import es.uma.seguridad.puntoseguro.datos.ResumenTiques
import es.uma.seguridad.puntoseguro.modelo.*

/**
 * Pantalla principal: saldo, acceso a la cámara para escanear tiques e
 * historial de movimientos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    estado: EstadoUi<List<Movimiento>>,
    saldo: EstadoUi<Int>,
    tiques: ResumenTiques,
    onCanjes: () -> Unit,
    onFotoTomada: (Bitmap) -> Unit,
    onRecargar: () -> Unit,
    onSalir: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    // P2.3, paso 6: permiso de cámara pedido en el momento en que se usa
    val contexto = LocalContext.current
    var concedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    // La cámara del sistema devuelve una miniatura del tique
    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) onFotoTomada(foto)
    }
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { resultado ->
        concedido = resultado
        if (resultado) camara.launch(null)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Fidelia", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    TextButton(onClick = onRecargar) {
                        Text("Recargar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    TextButton(onClick = onSalir) {
                        Text("Salir", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ---------- Saldo (lo decide el servidor) ----------
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Saldo de puntos", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = (saldo as? EstadoUi.Exito)?.datos?.toString() ?: "—",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "pts",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        when (saldo) {
                            is EstadoUi.Exito -> "Actualizado desde el servidor"
                            is EstadoUi.Cargando -> "Consultando el saldo..."
                            is EstadoUi.Error -> "Saldo no disponible (¿está arrancado servidor_fidelia.py?)"
                            is EstadoUi.Reposo -> "Pulsa Recargar para consultar tu saldo"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onCanjes,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Canjear puntos")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- Escanear tique (cámara) ----------
            Button(
                onClick = {
                    if (concedido) camara.launch(null)
                    else lanzador.launch(Manifest.permission.CAMERA)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Escanear tique de compra")
            }
            if (tiques.numero > 0) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    tiques.ultima?.let { miniatura ->
                        Image(
                            bitmap = miniatura.asImageBitmap(),
                            contentDescription = "Último tique escaneado",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        "Tiques escaneados: ${tiques.numero} (guardados en el dispositivo)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!concedido) {
                Text(
                    "Sin el permiso de cámara no se pueden añadir tiques. " +
                        "El resto de la aplicación sigue funcionando.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Historial de movimientos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            // ---------- Historial ----------
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (estado) {
                    is EstadoUi.Reposo -> Text(
                        "Pulsa Recargar para ver tus movimientos",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is EstadoUi.Cargando -> CircularProgressIndicator()
                    is EstadoUi.Error -> Text(
                        "No se han podido cargar los movimientos",
                        color = MaterialTheme.colorScheme.error
                    )
                    is EstadoUi.Exito -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(estado.datos) { movimiento ->
                            TarjetaMovimiento(movimiento, onClick = { onAbrir(movimiento.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaMovimiento(movimiento: Movimiento, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Inicial(movimiento.titulo)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    movimiento.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    movimiento.contenido,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    movimiento.fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
