// Página de géneros: lista los géneros y permite añadir uno nuevo
// usando la API REST /api/generos.

const API_GENEROS = "/api/generos";

const lista = document.getElementById("lista-generos");
const formulario = document.getElementById("form-genero");
const campoNombre = document.getElementById("nombre-genero");
const mensaje = document.getElementById("mensaje-genero");

function mostrarMensaje(texto, tipo) {
    mensaje.textContent = texto;
    mensaje.className = "mensaje " + (tipo || "");
}

function pintarGeneros(generos, idNuevo) {
    lista.innerHTML = "";
    if (generos.length === 0) {
        const li = document.createElement("li");
        li.className = "vacio";
        li.textContent = "Todavía no hay géneros. Añade el primero.";
        lista.appendChild(li);
        return;
    }
    for (const genero of generos) {
        const li = document.createElement("li");
        li.textContent = genero.nombre;
        if (genero.id === idNuevo) {
            li.className = "nuevo";
        }
        lista.appendChild(li);
    }
}

async function cargarGeneros(idNuevo) {
    try {
        const respuesta = await fetch(API_GENEROS);
        if (!respuesta.ok) {
            throw new Error("HTTP " + respuesta.status);
        }
        pintarGeneros(await respuesta.json(), idNuevo);
    } catch (e) {
        lista.innerHTML = "";
        mostrarMensaje("No se han podido cargar los géneros. Comprueba que la aplicación está arrancada.", "error");
    }
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const nombre = campoNombre.value.trim();
    if (nombre === "") {
        mostrarMensaje("Escribe el nombre del género.", "error");
        campoNombre.focus();
        return;
    }
    try {
        const respuesta = await fetch(API_GENEROS, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nombre })
        });
        const datos = await respuesta.json();
        if (!respuesta.ok) {
            mostrarMensaje(datos.error || "No se ha podido añadir el género.", "error");
            return;
        }
        campoNombre.value = "";
        mostrarMensaje("Género añadido: " + datos.nombre + ".", "exito");
        cargarGeneros(datos.id);
    } catch (e) {
        mostrarMensaje("No se ha podido conectar con la aplicación.", "error");
    }
});

cargarGeneros();
