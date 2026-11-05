package es.uma.seguridad.puntoseguro.datos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/* ==========================================================================
   ALMACÉN DE TIQUES
   Guarda las fotos de los tiques que se escanean con la cámara.

   AVISO: igual que el resto del Bloque II, se guarda SIN PROTECCIÓN, en el
   directorio privado de la aplicación (files/tiques/). Es una entrada más del
   inventario de datos sensibles; se protegerá en el Bloque III.

   Nota: el backend común no tiene ningún endpoint para enviar tiques, así que
   por ahora las fotos sólo se guardan en el dispositivo y no suman puntos.
   ========================================================================== */

/** Lo que la pantalla necesita saber de los tiques guardados. */
data class ResumenTiques(val numero: Int = 0, val ultima: Bitmap? = null)

class AlmacenTiques(contexto: Context) {

    private val carpeta = File(contexto.filesDir, CARPETA).apply { mkdirs() }

    /** Guarda la foto como JPEG y devuelve el fichero creado. */
    fun guardar(foto: Bitmap): File {
        val fichero = File(carpeta, "tique_${System.currentTimeMillis()}.jpg")
        fichero.outputStream().use { salida ->
            foto.compress(Bitmap.CompressFormat.JPEG, 90, salida)
        }
        return fichero
    }

    /** Los tiques guardados, del más reciente al más antiguo. */
    fun listar(): List<File> =
        carpeta.listFiles { f -> f.extension == "jpg" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    /** Carga la miniatura del tique más reciente, o null si no hay ninguno. */
    fun ultimaMiniatura(): Bitmap? =
        listar().firstOrNull()?.let { BitmapFactory.decodeFile(it.absolutePath) }

    /**
     * Borra todos los tiques. Lo llama el cierre de sesión: es la lección de la
     * P2.2, cada sitio donde se guardan datos del usuario es un sitio que hay
     * que limpiar al salir.
     */
    fun borrarTodo() {
        carpeta.listFiles()?.forEach { it.delete() }
    }

    companion object {
        const val CARPETA = "tiques"
    }
}
