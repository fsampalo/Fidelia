# Fidelia

## Equipo

**Grupo C** · Dominio: **Cartera** (cartera de fidelización)

| Miembro | Responsable de |
|---|---|
| Fernando Sampalo Gómez | Autenticación |
| Pablo Anguita Pérez | Almacenamiento local |
| Eric García Pedrosa | API remota |
| Pablo Rodríguez Gómez | Permiso de cámara |

## Descripción

Fidelia permite al usuario acumular puntos de fidelización fotografiando sus tiques de compra, consultar su saldo e historial y canjear los puntos por códigos de descuento.

El dato más sensible son los **códigos canjeables**, porque equivalen a dinero: quien los obtenga puede gastarlos sin ser el titular.

## Requisitos técnicos mínimos

- **Autenticación**: inicio de sesión con usuario y contraseña.
- **Almacenamiento local de datos sensibles**: saldo, historial de compras y códigos canjeables guardados en el dispositivo.
- **API remota**: consulta del saldo y validación del canje de códigos contra un servidor.
- **Permiso peligroso**: cámara, para fotografiar el tique de compra.

## Convenciones

| Decisión | Acuerdo |
|---|---|
| Nombre de paquete | `es.uma.seguridad.puntoseguro` |
| minSdk | 26 (Android 8.0) |
| targetSdk | 37 |
| Ramas | `main` siempre compila. Una rama por tarea y revisión de un compañero antes de fusionar. |

**Justificación del targetSdk**: se declara la versión estable más reciente para que el sistema aplique a la aplicación las restricciones de seguridad y privacidad más actuales.

## Cómo compilar y ejecutar

1. Clonar el repositorio y abrir la carpeta raíz en Android Studio (**File → Open**). Esperar a que termine la sincronización de Gradle.
2. Arrancar el backend de Fidelia, que está en la carpeta `backend/` (sólo necesita Python 3, sin dependencias):
   ```
   cd backend
   python servidor_fidelia.py
   ```
   Debe mostrar `Backend de Fidelia escuchando en http://0.0.0.0:8080`. Dejar esa terminal abierta y comprobar que responde con `curl http://localhost:8080/api/salud` (en Windows, `curl.exe`).
3. Ejecutar la aplicación en un emulador con el botón ▶ de Android Studio.
4. Entrar con `ana` / `Practica2024`. Otros usuarios de prueba: `luis` / `Practica2024` y `admin` / `Administrador`.

La aplicación se conecta a `http://10.0.2.2:8080/`, que es el ordenador visto desde el emulador. Si no conecta, se puede usar el canal de adb:

```
adb reverse tcp:8080 tcp:8080
```

y cambiar `BASE_URL` en `datos/ApiServicio.kt` a `http://localhost:8080/`. **Este cambio no se sube al repositorio.**

Versiones usadas: Kotlin 2.2.10, AGP 9.4.1, KSP 2.2.10-2.0.2.

### Sobre el backend

En `backend/` hay dos ficheros:

- `servidor.py`: el backend común de la asignatura, **sin modificar**. Sirve por HTTP sin cifrar y tiene debilidades intencionadas (contraseñas en claro, tokens predecibles, sin comprobación de propietario), que son material de estudio para los Bloques III y IV.
- `servidor_fidelia.py`: **amplía** el común sin tocarlo. Lo importa y añade las rutas del dominio: `GET /api/saldo`, `GET /api/catalogo`, `GET /api/canjes` y `POST /api/canjes`. Todas las rutas del común siguen igual, y admite el mismo modo TLS (`CERT` y `CLAVE`) para la P3.2.

La app funciona con cualquiera de los dos; con `servidor.py` el saldo y el canje aparecen como no disponibles. Los usuarios y contraseñas son de prueba, no credenciales reales. No debe usarse fuera del aula.

## Estado actual de la aplicación

Versión base del Bloque II (etiqueta `v1.0-bloque2`):

- **Acceso** con usuario y contraseña contra `/api/login`.
- **Saldo de puntos** (`/api/saldo`) e **historial de movimientos** (`/api/elementos`), con copia local de los movimientos en Room para verlos sin conexión.
- **Canje de puntos** por códigos de descuento (`/api/catalogo` y `/api/canjes`): el servidor comprueba el saldo y genera el código; la app guarda una copia de los códigos en Room.
- **Detalle** de un movimiento (`/api/elementos/{id}`).
- **Enlace profundo** `fidelia://detalle/{id}`. Para probarlo con la app cerrada:
  ```
  adb shell am start -a android.intent.action.VIEW -d "fidelia://detalle/2" es.uma.seguridad.puntoseguro
  ```
- **Escaneo de tiques** con la cámara: el permiso se pide al pulsar el botón y, si se deniega, el resto de la aplicación sigue funcionando. Las fotos se guardan en el dispositivo; todavía no suman puntos porque el backend no tiene dónde recibirlas.
- **`FLAG_SECURE`** en la ventana principal.

La aplicación es **insegura a propósito**, como punto de partida del Bloque III: guarda la sesión, los datos, los códigos y las fotos sin cifrar, registra información sensible en logcat y se comunica por HTTP sin cifrar. El inventario siguiente recoge qué queda expuesto, y el [documento de alcance](docs/documento_alcance.md) añade la superficie expuesta y el modelo de amenazas.

## Inventario de datos sensibles

| Dato | Dónde se guarda hoy | Quién puede leerlo hoy | Cómo se protegerá |
|---|---|---|---|
| Token de sesión | `shared_prefs/sesion.xml`, en claro | Cualquiera con adb y depuración activada | A completar en el Bloque III |
| Token de sesión | logcat (`Log.d` del repositorio e interceptor de red) | Cualquiera con adb; informes de errores | A completar en el Bloque III |
| Usuario y contraseña | logcat (interceptor en nivel `BODY`) y tráfico HTTP sin cifrar | Cualquiera con adb; cualquiera en la misma red | A completar en el Bloque III |
| Nombre de usuario | `sesion.xml` y columna `propietario` de la base de datos | Cualquiera con adb y depuración activada | A completar en el Bloque III |
| Historial de movimientos | `databases/appbase.db` (y `-wal`), sin cifrar | Cualquiera con adb; copias de seguridad (`allowBackup="true"`) | A completar en el Bloque III |
| Historial de movimientos | Tráfico HTTP sin cifrar | Cualquiera en la misma red | A completar en el Bloque III |
| Saldo de puntos | Tráfico HTTP sin cifrar (`/api/saldo`) | Cualquiera en la misma red | A completar en el Bloque III |
| Códigos canjeables | Tabla `canjes` de `appbase.db`, sin cifrar | Cualquiera con adb; copias de seguridad. Quien los lea puede gastarlos | A completar en el Bloque III |
| Códigos canjeables | Tráfico HTTP sin cifrar (`/api/canjes`) | Cualquiera en la misma red | A completar en el Bloque III |
| Fotos de los tiques | `files/tiques/*.jpg`, sin cifrar | Cualquiera con adb; copias de seguridad | A completar en el Bloque III |
| Datos personales (DNI, correo) | Tráfico HTTP de `/api/perfil` y logcat | Cualquiera en la misma red; cualquiera con adb | A completar en el Bloque III |

## Reconocimiento de una aplicación real

Aplicación analizada: **Google Fotos** (`com.google.android.apps.photos`, versión 7.99.0).
El emulador no permitía iniciar sesión en Google Play, así que no pudimos instalar una aplicación de fidelización. Como permite el enunciado, analizamos una aplicación del sistema ya instalada. Elegimos esta porque, como Fidelia, usa la cámara y maneja imágenes del usuario.

| Qué mirar | Con qué | Observación |
|---|---|---|
| Permisos declarados | `aapt2 dump permissions` | Declara 57 permisos. Muchos son peligrosos y se piden en ejecución: cámara, micrófono, ubicación precisa y aproximada, contactos, calendario (lectura y escritura), estado del teléfono, cuentas y multimedia (`READ_MEDIA_*`, `ACCESS_MEDIA_LOCATION`). Otros, como `MANAGE_MEDIA`, `WRITE_MEDIA_STORAGE` o `WRITE_SETTINGS`, solo se conceden porque es una aplicación preinstalada del sistema. Además define dos permisos propios: `C2D_MESSAGE` y `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` |
| Componentes exportados | `apktool` + lectura del manifiesto | **120** componentes con `exported="true"`, entre Activities, *activity-alias*, *receivers* y *services*. Entre ellos hay Activities de enlace profundo (`ArchivedPhotosDeepLinkActivity`), diálogos como `PermanentDeleteConfirmationActivity` y varios servicios de copia de seguridad expuestos por gRPC (`PhotosBackupGrpcService`, `PhotosBackupApiService`) |
| Esquemas de firma | `apksigner verify --print-certs` | Se verifican v3 y v3.1; v1, v2 y v4 no. Hay **dos firmantes**: un certificado antiguo (`CN=Unknown, O=Google, Inc`) para Android 7.0–12L (SDK 24–32) y uno nuevo (`CN=Android, O=Google Inc.`) para Android 13 en adelante (SDK 33+). Es una **rotación de clave** mediante v3.1. Incluye también un *Source Stamp*, que identifica la tienda de origen |
| minSdk y targetSdk | `aapt2 dump badging` | minSdk 32 (Android 12L) y targetSdk 37, compilada contra el SDK 37 |
| Número de piezas instaladas | `adb shell pm path` | 4 piezas: `base.apk` y tres *splits* de configuración, uno de idioma (`en`), otro de arquitectura (`x86_64`) y otro de densidad de pantalla (`xxhdpi`) |

**Notas**
- **¿Algún permiso difícil de justificar?** Para una galería de fotos cuesta justificar la **lectura y escritura del calendario**, el **estado del teléfono** (`READ_PHONE_STATE`) y la **superposición sobre otras aplicaciones** (`SYSTEM_ALERT_WINDOW`). La ubicación y los contactos se explican por funciones concretas, como ver las fotos en el mapa o compartirlas con otras personas, pero amplían mucho lo que la aplicación puede saber del usuario. Fidelia hace lo contrario: un solo permiso peligroso (la cámara), pedido en contexto y con `required="false"`.
- **¿Cuántos componentes exportados?** **120**, frente a la única Activity exportada de Fidelia. Cada uno es una puerta de entrada que otra aplicación puede invocar. Que un componente esté exportado no significa que esté desprotegido, porque muchos estarán protegidos con `android:permission`, pero cada uno hay que revisarlo. Es justo lo que hará el grupo auditor en el Bloque IV con nuestra aplicación.
- **Algo que aplicar a nuestro proyecto:** la rotación de clave con v3.1 permite cambiar la clave de firma sin que las actualizaciones dejen de instalarse. Lo tendremos en cuenta en el Bloque III.
