# Mi biblioteca de películas y series

Plantilla de la práctica de **Gestión de Proyectos Software** (ESIIAB, UCLM).

La aplicación ya arranca y tiene una funcionalidad de ejemplo completa (géneros) que recorre
todas las capas: base de datos, API REST y página web. El resto lo construiréis a partir del
backlog del proyecto, con el agente de Copilot como equipo de desarrollo.

## Requisitos

- **JDK 21 o superior** (por ejemplo, [Eclipse Temurin](https://adoptium.net/)). Comprobadlo con `java -version`.
- **Git**.
- **Visual Studio Code**. Al abrir el proyecto os sugerirá las extensiones recomendadas
  (Java, Spring Boot, GitHub Copilot y GitHub Pull Requests): instaladlas.

No hace falta instalar Maven ni ninguna base de datos: el proyecto incluye el *Maven wrapper*
(`mvnw`) y usa H2, una base de datos que se guarda en un fichero dentro del propio proyecto.

> **¿Qué es `mvnw`?** Maven es la herramienta que compila el proyecto, descarga las librerías
> indicadas en `pom.xml`, ejecuta los tests y arranca la aplicación. `mvnw` es un pequeño script
> que descarga automáticamente la versión de Maven que necesita el proyecto la primera vez que
> se ejecuta, así que no tenéis que instalarlo. `mvnw.cmd` es la versión para Windows y `mvnw`
> la de Linux/macOS. Lo que va detrás es la orden para Maven: `test`, `spring-boot:run`...

## Arrancar la aplicación

Desde la carpeta del proyecto (por ejemplo, en el terminal integrado de VS Code:
*Terminal → Nuevo terminal*):

| Sistema | Comando |
|---|---|
| Windows | `.\mvnw.cmd spring-boot:run` |
| Linux / macOS | `./mvnw spring-boot:run` |

> **Windows:** el terminal de VS Code es PowerShell, que exige el `.\` delante para ejecutar
> un programa de la carpeta actual. En el símbolo del sistema clásico (`cmd`) funciona también
> sin él.

La primera vez tarda unos minutos porque descarga Maven y las dependencias.
Cuando en la consola aparezca `Started BibliotecaApplication`, abrid:

- **Aplicación:** <http://localhost:8080>
- **Consola de la base de datos:** <http://localhost:8080/h2-console>
  (JDBC URL: `jdbc:h2:file:./data/biblioteca`, usuario `sa`, sin contraseña)

Para parar la aplicación: `Ctrl + C` en la consola.

> En Linux/macOS, si aparece "Permission denied", ejecutad una vez `chmod +x mvnw`.

## Los datos se conservan

Lo que añadáis se guarda en la carpeta `data/` y sigue ahí al volver a arrancar.
Esa carpeta no se sube a GitHub: cada miembro del equipo tiene sus propios datos locales.

### Empezar con la base de datos limpia

Parad la aplicación y borrad la carpeta `data/`. Al volver a arrancar se crea vacía
y se cargan los datos de ejemplo.

Hacedlo, sobre todo, si al probar una rama la aplicación falla al arrancar por un cambio
en el modelo de datos (por ejemplo, un campo nuevo obligatorio en una tabla que ya tenía filas).

## Ejecutar los tests

| Sistema | Comando |
|---|---|
| Windows | `.\mvnw.cmd test` |
| Linux / macOS | `./mvnw test` |

Los tests usan una base de datos en memoria, así que no tocan vuestros datos de `data/`.
Además, GitHub ejecuta los tests automáticamente en cada pull request (pestaña *Checks*).

## Revisar una pull request

1. Parad la aplicación si está arrancada.
2. Traed los cambios y cambiad a la rama de la PR:
   ```
   git fetch
   git switch nombre-de-la-rama
   ```
   (También podéis hacerlo desde VS Code con la extensión GitHub Pull Requests.)
3. Si la PR indica que cambia el modelo de datos, borrad la carpeta `data/`.
4. Arrancad la aplicación y comprobad **uno a uno los criterios de aceptación** de la issue.
5. Comprobad que los tests pasan (en local o en la pestaña *Checks* de la PR).
6. En la PR: aprobad, o pedid cambios explicando qué criterio no se cumple.

## Estructura del proyecto

```
src/main/java/es/uclm/esiiab/gps/biblioteca/
├── BibliotecaApplication.java      punto de entrada
└── genero/                         funcionalidad de ejemplo (patrón a seguir)
    ├── Genero.java                 entidad (tabla de la base de datos)
    ├── GeneroRepository.java       acceso a datos
    ├── GeneroService.java          reglas de negocio
    ├── GeneroController.java       API REST (/api/generos)
    └── DatosInicialesGeneros.java  datos de ejemplo (solo si la tabla está vacía)
└── titulo/                         catálogo de películas y series
    ├── Titulo.java                 entidad con tipo película/serie y relación con género
    ├── TituloController.java       API REST (/api/titulos)
    ├── TituloService.java          validaciones y operaciones CRUD
    └── TituloRepository.java       acceso a datos
src/main/resources/
├── application.properties          configuración
└── static/                         front: index.html, catalogo.html, css/, js/
src/test/java/...                   tests
.github/
├── copilot-instructions.md         normas que sigue el agente de Copilot
├── ISSUE_TEMPLATE/                 plantilla de historia de usuario
├── pull_request_template.md        plantilla de pull request
└── workflows/ci.yml                ejecución automática de tests
```
