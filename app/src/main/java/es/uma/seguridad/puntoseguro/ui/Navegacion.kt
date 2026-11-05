package es.uma.seguridad.puntoseguro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.uma.seguridad.puntoseguro.VistaModelo
import es.uma.seguridad.puntoseguro.modelo.EstadoUi

/**
 * Navegación entre pantallas. ESTÁ TERMINADA: no hace falta tocarla en P2.2.
 *
 * En la práctica P2.3 se ampliará con argumentos de ruta y enlaces profundos,
 * y se comprobará que la navegación no es un control de seguridad (sesión 2.5.3).
 */
@Composable
fun Navegacion(vistaModelo: VistaModelo) {
    val navController = rememberNavController()
    val estadoLogin by vistaModelo.estadoLogin.collectAsState()
    val estadoLista by vistaModelo.estadoLista.collectAsState()
    val estadoDetalle by vistaModelo.estadoDetalle.collectAsState()
    val tiques by vistaModelo.tiques.collectAsState()
    val estadoSaldo by vistaModelo.estadoSaldo.collectAsState()
    val estadoCatalogo by vistaModelo.estadoCatalogo.collectAsState()
    val estadoCanjes by vistaModelo.estadoCanjes.collectAsState()
    val resultadoCanje by vistaModelo.resultadoCanje.collectAsState()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            PantallaLogin(
                estado = estadoLogin,
                onEntrar = { usuario, clave -> vistaModelo.entrar(usuario, clave) }
            )
            if (estadoLogin is EstadoUi.Exito) {
                navController.navigate("lista") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("lista") {
            LaunchedEffect(Unit) {
                vistaModelo.cargarTiques()
                vistaModelo.cargarSaldo()
            }
            PantallaLista(
                estado = estadoLista,
                saldo = estadoSaldo,
                tiques = tiques,
                onCanjes = { navController.navigate("canjes") },
                onFotoTomada = { vistaModelo.guardarTique(it) },
                onRecargar = {
                    vistaModelo.cargarElementos()
                    vistaModelo.cargarSaldo()
                },
                onSalir = {
                    vistaModelo.salir()
                    navController.navigate("login") {
                        popUpTo("lista") { inclusive = true }
                    }
                },
                onAbrir = { identificador -> navController.navigate("detalle/$identificador") }
            )
        }

        composable("canjes") {
            PantallaCanjes(
                saldo = estadoSaldo,
                catalogo = estadoCatalogo,
                canjes = estadoCanjes,
                resultado = resultadoCanje,
                onCargar = { vistaModelo.cargarCanje() },
                onCanjear = { vistaModelo.canjear(it) },
                onOlvidarResultado = { vistaModelo.olvidarResultadoCanje() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = "detalle/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "fidelia://detalle/{id}" })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            PantallaDetalle(
                id = id,
                estado = estadoDetalle,
                onCargar = { vistaModelo.cargarDetalle(it) },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
