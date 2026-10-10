const API_BIBLIOTECA = "/api/biblioteca";
const API_GENEROS = "/api/generos";
const estados = ["PENDIENTE", "VISTO", "ABANDONADO"];
const nombresEstado = {
    PENDIENTE: "Pendiente",
    VISTO: "Visto",
    ABANDONADO: "Abandonado"
};
const lista = document.getElementById("lista-biblioteca");
const mensaje = document.getElementById("mensaje-biblioteca");

function mostrarMensaje(texto, tipo) {
    mensaje.textContent = texto;
    mensaje.className = "mensaje " + (tipo || "");
}

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

function crearFila(item) {
    const fila = document.createElement("li");
    fila.className = "elemento-coleccion";

    const informacion = document.createElement("div");
    informacion.className = "informacion-titulo";
    const titulo = document.createElement("strong");
    titulo.textContent = item.titulo;
    const tipo = document.createElement("span");
    tipo.textContent = `${item.tipoTitulo === "PELICULA" ? "Película" : "Serie"} · Catálogo #${item.tituloId}`;
    informacion.append(titulo, tipo);

    const acciones = document.createElement("div");
    acciones.className = "acciones-coleccion";
    const selector = document.createElement("select");
    selector.setAttribute("aria-label", `Estado de ${item.titulo}`);
    for (const estado of estados) {
        const opcion = document.createElement("option");
        opcion.value = estado;
        opcion.textContent = nombresEstado[estado];
        opcion.selected = estado === item.estado;
        selector.appendChild(opcion);
    }

    const actualizar = document.createElement("button");
    actualizar.type = "button";
    actualizar.textContent = "Guardar estado";
    actualizar.addEventListener("click", async () => {
        try {
            await solicitar(`${API_BIBLIOTECA}/${item.id}`, {
                method: "PUT",
                body: JSON.stringify({ estado: selector.value })
            });
            mostrarMensaje("Estado actualizado.", "exito");
            await cargarBiblioteca();
        } catch (error) {
            mostrarMensaje(error.message, "error");
        }
    });

    const eliminar = document.createElement("button");
    eliminar.type = "button";
    eliminar.className = "boton-secundario";
    eliminar.textContent = "Quitar";
    eliminar.setAttribute("aria-label", `Quitar ${item.titulo} de mi biblioteca`);
    eliminar.addEventListener("click", async () => {
        try {
            await solicitar(`${API_BIBLIOTECA}/${item.id}`, { method: "DELETE" });
            mostrarMensaje("Título eliminado de tu biblioteca.", "exito");
            await cargarBiblioteca();
        } catch (error) {
            mostrarMensaje(error.message, "error");
        }
    });

    acciones.append(selector, actualizar, eliminar);
    fila.append(informacion, acciones);
    return fila;
}

async function cargarBiblioteca() {
    try {
        const parametros = new URLSearchParams();
        const genero = document.getElementById("filtro-genero-biblioteca").value;
        const anio = document.getElementById("filtro-anio-biblioteca").value;
        const estado = document.getElementById("filtro-estado-biblioteca").value;
        if (genero) parametros.set("generoId", genero);
        if (anio) parametros.set("anio", anio);
        if (estado) parametros.set("estado", estado);
        const items = await solicitar(`${API_BIBLIOTECA}?${parametros}`);
        lista.replaceChildren();
        if (items.length === 0) {
            const vacio = document.createElement("li");
            vacio.className = "vacio";
            vacio.textContent = "No se han encontrado películas ni series con los filtros seleccionados";
            lista.appendChild(vacio);
            return;
        }
        for (const item of items) {
            lista.appendChild(crearFila(item));
        }
    } catch (error) {
        lista.replaceChildren();
        mostrarMensaje(error.message, "error");
    }
}

async function cargarGeneros() {
    const generos = await solicitar(API_GENEROS);
    const selector = document.getElementById("filtro-genero-biblioteca");
    generos.forEach(genero => selector.add(new Option(genero.nombre, genero.id)));
}

document.getElementById("filtros-biblioteca").addEventListener("submit", evento => {
    evento.preventDefault();
    cargarBiblioteca();
});
document.getElementById("limpiar-filtros-biblioteca").addEventListener("click", () => {
    document.getElementById("filtros-biblioteca").reset();
    cargarBiblioteca();
});

Promise.all([cargarGeneros(), cargarBiblioteca()]).catch(error => mostrarMensaje(error.message, "error"));
