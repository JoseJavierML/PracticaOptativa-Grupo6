# Instrucciones para Copilot

Este repositorio es una aplicación web de biblioteca personal de películas y series.
Todo el código lo genera Copilot a partir de las issues del equipo; seguid estas normas siempre.

## Stack (no cambiarlo)

- Java 21 y Spring Boot 4.1, con Maven (usar siempre el wrapper `mvnw`).
- Base de datos H2 en fichero (`./data`), acceso con Spring Data JPA.
- Front: HTML, CSS y JavaScript sin frameworks, en `src/main/resources/static`.
- No añadir dependencias nuevas salvo que la issue lo pida expresamente.
- No modificar la configuración de la base de datos de `application.properties`.

## Arquitectura

- Un paquete por funcionalidad dentro de `es.uclm.esiiab.gps.biblioteca`
  (por ejemplo `pelicula`, `serie`), siguiendo el patrón del paquete `genero`:
  entidad, repositorio, servicio y controlador REST.
- Las reglas de negocio y validaciones van en el servicio, no en el controlador.
- La API REST cuelga de `/api/...`. Los errores de validación devuelven 400 con `{"error": "mensaje"}`.
- Los datos de ejemplo se cargan solo si la tabla está vacía (ver `DatosInicialesGeneros`).

## Front

- Una página HTML por pantalla principal, con su propio fichero JS en `static/js`.
- Reutilizar `static/css/estilos.css` y sus variables; no crear hojas de estilo nuevas sin necesidad.
- Todos los textos de la interfaz en español, claros y desde el punto de vista del usuario.
- Mostrar mensajes de error comprensibles cuando la API devuelva un error.

## Tests

- Cada endpoint nuevo o modificado debe tener tests con MockMvc (ver `GeneroControllerTest`).
- Todos los tests existentes deben seguir pasando (`mvnw test`).

## Al terminar una tarea

- Comprobar que el proyecto compila y los tests pasan.
- Resumir los cambios realizados e indicar cómo probar cada criterio de aceptación de la issue.
- Indicar si ha cambiado el modelo de datos.
