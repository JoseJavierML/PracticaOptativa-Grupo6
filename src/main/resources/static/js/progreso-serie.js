window.crearProgresoSerie = function (serieId, alError) {
    const acordeon = document.createElement("details");
    acordeon.className = "progreso-serie";
    const resumen = document.createElement("summary");
    resumen.textContent = "Ver temporadas y capítulos";
    const contenido = document.createElement("div");
    acordeon.append(resumen, contenido);

    async function solicitar(url, opciones = {}) {
        const respuesta = await fetch(url, {
            ...opciones,
            headers: { "Content-Type": "application/json", ...(opciones.headers || {}) }
        });
        const datos = await respuesta.json();
        if (!respuesta.ok) {
            throw new Error(datos?.error || "No se ha podido completar la operación.");
        }
        return datos;
    }

    function representar(datos) {
        const estabaAbierto = acordeon.open;
        contenido.replaceChildren();
        for (const temporada of datos.detalleTemporadas) {
            const seccion = document.createElement("details");
            seccion.className = "temporada-progreso";
            const tituloTemporada = document.createElement("summary");
            tituloTemporada.textContent = `Temporada ${temporada.numero} · ${temporada.capitulosVistos}/${temporada.totalCapitulos} capítulos${temporada.vista ? " · Vista" : ""}`;
            const listaCapitulos = document.createElement("div");
            listaCapitulos.className = "lista-capitulos";
            for (const capitulo of temporada.capitulos) {
                const etiqueta = document.createElement("label");
                etiqueta.className = "capitulo-checkbox";
                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.checked = capitulo.visto;
                checkbox.setAttribute("aria-label", `Temporada ${temporada.numero}, capítulo ${capitulo.numero} visto`);
                checkbox.addEventListener("change", async () => {
                    checkbox.disabled = true;
                    try {
                        const actualizado = await solicitar(
                            `/api/biblioteca/series/${serieId}/capitulos`, {
                                method: "POST",
                                body: JSON.stringify({
                                    temporada: temporada.numero,
                                    capitulo: capitulo.numero,
                                    visto: checkbox.checked
                                })
                            });
                        representar(actualizado);
                    } catch (error) {
                        checkbox.checked = !checkbox.checked;
                        checkbox.disabled = false;
                        alError(error);
                    }
                });
                etiqueta.append(checkbox, `Capítulo ${capitulo.numero}`);
                listaCapitulos.appendChild(etiqueta);
            }
            seccion.append(tituloTemporada, listaCapitulos);
            contenido.appendChild(seccion);
        }
        acordeon.open = estabaAbierto;
    }

    async function cargar() {
        try {
            representar(await solicitar(`/api/biblioteca/series/${serieId}/capitulos`));
        } catch (error) {
            contenido.textContent = error.message;
            contenido.className = "mensaje error";
            alError(error);
        }
    }

    return { element: acordeon, cargar };
};