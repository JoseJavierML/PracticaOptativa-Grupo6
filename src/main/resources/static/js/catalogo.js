const API_TITULOS = "/api/titulos";
const API_GENEROS = "/api/generos";
const lista = document.getElementById("lista-titulos");
const seccionFormulario = document.getElementById("formulario-seccion");
const formulario = document.getElementById("form-titulo");
const mensaje = document.getElementById("mensaje-catalogo");
const mensajeFormulario = document.getElementById("mensaje-formulario");
const tipo = document.getElementById("tipo");

function mostrarMensaje(elemento, texto, clase) {
    elemento.textContent = texto;
    elemento.className = "mensaje " + (clase || "");
}

function pintar(titulos) {
    lista.innerHTML = "";
    if (titulos.length === 0) {
        lista.innerHTML = "<p class=\"vacio\">Todavía no hay títulos en el catálogo.</p>";
        return;
    }
    for (const titulo of titulos) {
        const tarjeta = document.createElement("article");
        tarjeta.className = "tarjeta";
        const detalle = titulo.tipo === "PELICULA"
            ? `${titulo.duracion} minutos`
            : `${titulo.temporadas} temporadas`;
        tarjeta.innerHTML = `<h3></h3><p>${titulo.tipo === "PELICULA" ? "Película" : "Serie"} · ${titulo.anio} · ${detalle}</p><p>Género: <span></span></p>`;
        tarjeta.querySelector("h3").textContent = titulo.titulo;
        tarjeta.querySelector("span").textContent = titulo.genero.nombre;
        const editar = document.createElement("button");
        editar.textContent = "Editar";
        editar.addEventListener("click", () => abrirFormulario(titulo));
        const borrar = document.createElement("button");
        borrar.textContent = "Borrar";
        borrar.className = "peligro";
        borrar.addEventListener("click", () => borrarTitulo(titulo));
        tarjeta.append(editar, borrar);
        lista.appendChild(tarjeta);
    }
}

async function cargar(url, opciones) {
    const respuesta = await fetch(url, opciones);
    const datos = respuesta.status === 204 ? null : await respuesta.json();
    if (!respuesta.ok) throw new Error(datos?.error || "No se ha podido completar la operación.");
    return datos;
}

async function cargarDatos() {
    try {
        const [titulos, generos] = await Promise.all([cargar(API_TITULOS), cargar(API_GENEROS)]);
        pintar(titulos);
        const selector = document.getElementById("genero");
        selector.innerHTML = '<option value="">Selecciona un género</option>';
        generos.forEach(genero => selector.add(new Option(genero.nombre, genero.id)));
    } catch (error) {
        mostrarMensaje(mensaje, error.message, "error");
    }
}

function abrirFormulario(titulo) {
    formulario.reset();
    document.getElementById("id-titulo").value = titulo?.id || "";
    document.getElementById("titulo-formulario").textContent = titulo ? "Editar título" : "Añadir título";
    if (titulo) {
        document.getElementById("titulo").value = titulo.titulo;
        document.getElementById("tipo").value = titulo.tipo;
        document.getElementById("anio").value = titulo.anio;
        document.getElementById("genero").value = titulo.genero.id;
        document.getElementById("duracion").value = titulo.duracion || "";
        document.getElementById("temporadas").value = titulo.temporadas || "";
    }
    actualizarCampos();
    seccionFormulario.hidden = false;
    seccionFormulario.scrollIntoView({ behavior: "smooth" });
}

function actualizarCampos() {
    const pelicula = tipo.value === "PELICULA";
    document.getElementById("duracion").hidden = !pelicula;
    document.getElementById("etiqueta-duracion").hidden = !pelicula;
    document.getElementById("temporadas").hidden = pelicula || tipo.value === "";
    document.getElementById("etiqueta-temporadas").hidden = pelicula || tipo.value === "";
    document.getElementById("duracion").required = pelicula;
    document.getElementById("temporadas").required = !pelicula && tipo.value !== "";
}

async function borrarTitulo(titulo) {
    if (!confirm(`¿Quieres borrar "${titulo.titulo}" del catálogo?`)) return;
    try {
        await cargar(`${API_TITULOS}/${titulo.id}`, { method: "DELETE" });
        mostrarMensaje(mensaje, "Título eliminado.", "exito");
        await cargarDatos();
    } catch (error) {
        mostrarMensaje(mensaje, error.message, "error");
    }
}

tipo.addEventListener("change", actualizarCampos);
document.getElementById("nuevo-titulo").addEventListener("click", () => abrirFormulario());
document.getElementById("cancelar-titulo").addEventListener("click", () => { seccionFormulario.hidden = true; });
formulario.addEventListener("submit", async evento => {
    evento.preventDefault();
    const datos = {
        titulo: document.getElementById("titulo").value.trim(),
        tipo: tipo.value,
        anio: Number(document.getElementById("anio").value),
        generoId: Number(document.getElementById("genero").value),
        duracion: tipo.value === "PELICULA" ? Number(document.getElementById("duracion").value) : null,
        temporadas: tipo.value === "SERIE" ? Number(document.getElementById("temporadas").value) : null
    };
    try {
        const id = document.getElementById("id-titulo").value;
        await cargar(id ? `${API_TITULOS}/${id}` : API_TITULOS, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });
        seccionFormulario.hidden = true;
        mostrarMensaje(mensaje, id ? "Título actualizado." : "Título añadido.", "exito");
        await cargarDatos();
    } catch (error) {
        mostrarMensaje(mensajeFormulario, error.message, "error");
    }
});

actualizarCampos();
cargarDatos();
