#!/usr/bin/env python3
"""
Backend de Fidelia (grupo C): amplía el backend común de la asignatura.

NO modifica servidor.py: lo importa y añade las rutas propias del dominio
(saldo de puntos, catálogo de recompensas y canje de códigos). Todas las rutas
del backend común siguen funcionando igual, con los mismos usuarios, datos y
debilidades intencionadas.

    python servidor_fidelia.py            # escucha en 0.0.0.0:8080
    PUERTO=9000 python servidor_fidelia.py

Admite el mismo modo TLS que el backend común (variables CERT y CLAVE), para
que sirva también en la práctica P3.2.

Rutas añadidas (todas exigen la cabecera Authorization: Bearer <token>):

    GET  /api/saldo       saldo de puntos del usuario
    GET  /api/catalogo    recompensas que se pueden canjear
    GET  /api/canjes      códigos canjeados por el usuario
    POST /api/canjes      canjea una recompensa: {"recompensa": <id>}

El servidor es quien comprueba el saldo y quien genera el código: la
aplicación nunca puede fabricarse un código ni decidir cuántos puntos tiene.

ADVERTENCIA DOCENTE: igual que el backend común, sirve por HTTP sin cifrar y
guarda todo en memoria. No debe usarse fuera del aula.
"""

import secrets
import ssl
import threading
import time
from http.server import ThreadingHTTPServer

import servidor as comun

# --- Datos del dominio (en memoria) --------------------------------------
SALDOS = {"ana": 1500, "luis": 800, "admin": 0}

CATALOGO = [
    {"id": 1, "descripcion": "Envío gratis en tu próxima compra", "puntos": 300},
    {"id": 2, "descripcion": "5 € de descuento", "puntos": 500},
    {"id": 3, "descripcion": "10 € de descuento", "puntos": 900},
]

CANJES = {usuario: [] for usuario in comun.USUARIOS}

_lock = threading.Lock()


def generar_codigo():
    """Código aleatorio de 12 caracteres, agrupado de 4 en 4 (p. ej. FID-8K2Q-M7XA-R4TD)."""
    alfabeto = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"   # sin 0/O ni 1/I para evitar confusiones
    crudo = "".join(secrets.choice(alfabeto) for _ in range(12))
    return "FID-%s-%s-%s" % (crudo[0:4], crudo[4:8], crudo[8:12])


class ManejadorFidelia(comun.Manejador):
    server_version = "Fidelia/1.0"

    def do_GET(self):
        ruta = self.path.split("?")[0]

        if ruta in ("/api/saldo", "/api/catalogo", "/api/canjes"):
            usuario = self._usuario_autenticado()
            if usuario is None:
                return self._responder(401, {"error": "Token ausente o no válido"})

            if ruta == "/api/saldo":
                return self._responder(200, {"usuario": usuario, "puntos": SALDOS.get(usuario, 0)})
            if ruta == "/api/catalogo":
                return self._responder(200, {"recompensas": CATALOGO})
            return self._responder(200, {"canjes": CANJES.get(usuario, [])})

        return super().do_GET()

    def do_POST(self):
        ruta = self.path.split("?")[0]

        if ruta == "/api/canjes":
            usuario = self._usuario_autenticado()
            if usuario is None:
                return self._responder(401, {"error": "Token ausente o no válido"})

            cuerpo = self._cuerpo()
            recompensa = next((r for r in CATALOGO if r["id"] == cuerpo.get("recompensa")), None)
            if recompensa is None:
                return self._responder(400, {"error": "Recompensa no válida"})

            with _lock:
                if SALDOS.get(usuario, 0) < recompensa["puntos"]:
                    return self._responder(409, {"error": "Saldo insuficiente"})
                SALDOS[usuario] -= recompensa["puntos"]
                canje = {
                    "codigo": generar_codigo(),
                    "descripcion": recompensa["descripcion"],
                    "puntos": recompensa["puntos"],
                    "fecha": time.strftime("%Y-%m-%d"),
                }
                CANJES.setdefault(usuario, []).insert(0, canje)
            return self._responder(201, canje)

        return super().do_POST()


if __name__ == "__main__":
    servidor = ThreadingHTTPServer(("0.0.0.0", comun.PUERTO), ManejadorFidelia)

    if comun.CERT and comun.CLAVE:
        contexto = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        contexto.load_cert_chain(certfile=comun.CERT, keyfile=comun.CLAVE)
        contexto.minimum_version = ssl.TLSVersion.TLSv1_2
        servidor.socket = contexto.wrap_socket(servidor.socket, server_side=True)
        esquema = "https"
    else:
        esquema = "http"

    print("Backend de Fidelia escuchando en %s://0.0.0.0:%d" % (esquema, comun.PUERTO))
    print("Amplía el backend común con /api/saldo, /api/catalogo y /api/canjes.")
    if esquema == "http":
        print("MODO SIN CIFRAR: es el punto de partida de la práctica P3.2.")
    else:
        print("MODO TLS: certificado %s" % comun.CERT)
    print("Desde el emulador de Android, la dirección del anfitrión es 10.0.2.2")
    print("Usuarios de prueba: ana / luis / admin  ·  contraseña: Practica2024 (admin: Administrador)")
    servidor.serve_forever()
