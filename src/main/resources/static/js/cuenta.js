const API_SESION = "/api/usuarios/sesion";
const API_REGISTRO = "/api/usuarios/registro";
const estadoSesion = document.getElementById("estado-sesion");
const zonaCuenta = document.getElementById("zona-cuenta");
const accionesSesion = document.getElementById("acciones-sesion");

async function solicitar(url, opciones = {}) {
    const respuesta = await fetch(url, {
        ...opciones,
        headers: { "Content-Type": "application/json", ...(opciones.headers || {}) }
    });
    const datos = respuesta.status === 204 ? null : await respuesta.json();
    if (!respuesta.ok) {
        throw new Error(datos?.error || "No se ha podido completar la operación.");
    }
    return datos;
}

function mostrarMensaje(id, texto, tipo) {
    const elemento = document.getElementById(id);
    elemento.textContent = texto;
    elemento.className = "mensaje " + (tipo || "");
}

function mostrarCuenta(usuario) {
    estadoSesion.textContent = `Sesión iniciada como ${usuario.nombreUsuario}.`;
    zonaCuenta.hidden = true;
    accionesSesion.hidden = false;
}

async function comprobarSesion() {
    try {
        mostrarCuenta(await solicitar(API_SESION));
    } catch {
        estadoSesion.textContent = "No has iniciado sesión.";
        zonaCuenta.hidden = false;
        accionesSesion.hidden = true;
    }
}

async function enviarCredenciales(evento, formularioId, mensajeId, url) {
    evento.preventDefault();
    const registro = formularioId === "form-registro";
    const prefijo = registro ? "registro" : "acceso";
    const credenciales = {
        nombreUsuario: document.getElementById(`usuario-${prefijo}`).value.trim(),
        contrasena: document.getElementById(`contrasena-${prefijo}`).value
    };
    try {
        const usuario = await solicitar(url, {
            method: "POST",
            body: JSON.stringify(credenciales)
        });
        document.getElementById(formularioId).reset();
        mostrarCuenta(usuario);
    } catch (error) {
        mostrarMensaje(mensajeId, error.message, "error");
    }
}

document.getElementById("form-acceso").addEventListener("submit", evento =>
    enviarCredenciales(evento, "form-acceso", "mensaje-acceso", API_SESION));
document.getElementById("form-registro").addEventListener("submit", evento =>
    enviarCredenciales(evento, "form-registro", "mensaje-registro", API_REGISTRO));
document.getElementById("cerrar-sesion").addEventListener("click", async () => {
    try {
        await solicitar(API_SESION, { method: "DELETE" });
        estadoSesion.textContent = "Has cerrado sesión.";
        zonaCuenta.hidden = false;
        accionesSesion.hidden = true;
    } catch (error) {
        estadoSesion.textContent = error.message;
    }
});

comprobarSesion();
