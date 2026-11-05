package es.uma.seguridad.puntoseguro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import es.uma.seguridad.puntoseguro.datos.Repositorio
import es.uma.seguridad.puntoseguro.datos.ResumenTiques
import es.uma.seguridad.puntoseguro.modelo.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modelo de vista de la aplicación.
 *
 * Sostiene el estado de las pantallas y lanza el trabajo en viewModelScope, de
 * modo que las corrutinas se cancelan solas cuando la pantalla desaparece
 * (sesión 2.4.5, concurrencia estructurada). Si el usuario sale mientras una
 * descarga está en marcha, esa descarga se cancela: no termina después y
 * escribe datos de la sesión anterior.
 *
 * Está completo. En P2.2 sólo hay que leerlo.
 */
class VistaModelo(private val repositorio: Repositorio) : ViewModel() {

    private val _estadoLogin = MutableStateFlow<EstadoUi<RespuestaLogin>>(EstadoUi.Reposo)
    val estadoLogin: StateFlow<EstadoUi<RespuestaLogin>> = _estadoLogin.asStateFlow()

    private val _estadoLista = MutableStateFlow<EstadoUi<List<Movimiento>>>(EstadoUi.Reposo)
    val estadoLista: StateFlow<EstadoUi<List<Movimiento>>> = _estadoLista.asStateFlow()

    private val _estadoDetalle = MutableStateFlow<EstadoUi<Movimiento>>(EstadoUi.Reposo)
    val estadoDetalle: StateFlow<EstadoUi<Movimiento>> = _estadoDetalle.asStateFlow()

    private val _tiques = MutableStateFlow(ResumenTiques())
    val tiques: StateFlow<ResumenTiques> = _tiques.asStateFlow()

    // Puntos y canje
    private val _estadoSaldo = MutableStateFlow<EstadoUi<Int>>(EstadoUi.Reposo)
    val estadoSaldo: StateFlow<EstadoUi<Int>> = _estadoSaldo.asStateFlow()

    private val _estadoCatalogo = MutableStateFlow<EstadoUi<List<Recompensa>>>(EstadoUi.Reposo)
    val estadoCatalogo: StateFlow<EstadoUi<List<Recompensa>>> = _estadoCatalogo.asStateFlow()

    private val _estadoCanjes = MutableStateFlow<EstadoUi<List<Canje>>>(EstadoUi.Reposo)
    val estadoCanjes: StateFlow<EstadoUi<List<Canje>>> = _estadoCanjes.asStateFlow()

    /** Último resultado de un canje, para mostrarlo una vez en pantalla. */
    private val _resultadoCanje = MutableStateFlow<ResultadoCanje?>(null)
    val resultadoCanje: StateFlow<ResultadoCanje?> = _resultadoCanje.asStateFlow()

    fun entrar(usuario: String, clave: String) {
        viewModelScope.launch {
            _estadoLogin.value = EstadoUi.Cargando
            _estadoLogin.value = repositorio.iniciarSesion(usuario, clave)
        }
    }

    fun cargarElementos() {
        viewModelScope.launch {
            _estadoLista.value = EstadoUi.Cargando
            _estadoLista.value = repositorio.obtenerElementos()
        }
    }

    fun cargarSaldo() {
        viewModelScope.launch { _estadoSaldo.value = repositorio.obtenerSaldo() }
    }

    /** Carga todo lo que necesita la pantalla de canje. */
    fun cargarCanje() {
        viewModelScope.launch {
            _estadoCatalogo.value = EstadoUi.Cargando
            _estadoSaldo.value = repositorio.obtenerSaldo()
            _estadoCatalogo.value = repositorio.obtenerCatalogo()
            _estadoCanjes.value = repositorio.obtenerCanjes()
        }
    }

    fun canjear(recompensa: Int) {
        viewModelScope.launch {
            val resultado = repositorio.canjear(recompensa)
            _resultadoCanje.value = resultado
            if (resultado is ResultadoCanje.Hecho) {
                _estadoSaldo.value = repositorio.obtenerSaldo()
                _estadoCanjes.value = repositorio.obtenerCanjes()
            }
        }
    }

    fun olvidarResultadoCanje() {
        _resultadoCanje.value = null
    }

    fun cargarTiques() {
        viewModelScope.launch { _tiques.value = repositorio.resumenTiques() }
    }

    fun guardarTique(foto: Bitmap) {
        viewModelScope.launch { _tiques.value = repositorio.guardarTique(foto) }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            _estadoDetalle.value = EstadoUi.Cargando
            _estadoDetalle.value = repositorio.obtenerMovimiento(id)
        }
    }

    /**
     * Cierre de sesión.
     *
     * Pide al repositorio que borre la sesión y devuelve las dos pantallas a
     * Reposo. Lo segundo no es decorativo: si el estado de acceso se quedara
     * en Exito, la navegación volvería a entrar sola en la lista.
     *
     * PARA EL CUADERNO: además del estado del modelo de vista y del token,
     * ¿qué otros rastros del usuario quedan en el dispositivo? Haz la lista
     * antes de seguir y compruébala en la sección 5.5.
     */
    fun salir() {
        viewModelScope.launch {
            repositorio.cerrarSesion()
            _estadoLogin.value = EstadoUi.Reposo
            _estadoLista.value = EstadoUi.Reposo
            _estadoDetalle.value = EstadoUi.Reposo
            _tiques.value = ResumenTiques()
            _estadoSaldo.value = EstadoUi.Reposo
            _estadoCatalogo.value = EstadoUi.Reposo
            _estadoCanjes.value = EstadoUi.Reposo
            _resultadoCanje.value = null
        }
    }
}
