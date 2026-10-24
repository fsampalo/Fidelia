package es.uma.seguridad.puntoseguro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import es.uma.seguridad.puntoseguro.datos.*
import es.uma.seguridad.puntoseguro.ui.Navegacion
import es.uma.seguridad.puntoseguro.ui.theme.PuntoSeguroTheme
/**
 * Punto de entrada. ESTÁ TERMINADO: no hace falta tocarlo en P2.2.
 *
 * Las dependencias se construyen aquí a mano. En un proyecto real se usaría
 * inyección de dependencias (sesión 2.5.4); lo importante es la idea: el
 * cliente de red y el almacén se configuran UNA VEZ y toda la aplicación los
 * hereda. Cuando en la práctica P3.2 se añada anclaje de certificados, se
 * tocará sólo ClienteRed y no habrá que revisar ninguna pantalla.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repositorio = Repositorio(
            api = ClienteRed.crear(),
            sesion = SesionLocal(applicationContext),
            dao = BaseDatos.obtener(applicationContext).movimientoDao()
        )

        setContent {
            PuntoSeguroTheme {
                val vistaModelo: VistaModelo = viewModel(factory = FabricaVistaModelo(repositorio))
                Navegacion(vistaModelo)
            }
        }
    }
}

class FabricaVistaModelo(private val repositorio: Repositorio) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        VistaModelo(repositorio) as T
}
