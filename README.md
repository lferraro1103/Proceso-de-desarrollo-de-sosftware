# Corrección de código — Clase 5: GRASP

Módulo: UADE Beats — Acceso a eventos en vivo

## 1. Bad smells corregidos

| Smell | Descripción | Solución aplicada |
|---|---|---|
| Contrato sin efecto | `Usuario.actualizarPerfil()` y `Artista.actualizarInformacionArtistica()`, en su versión sin argumentos, no realizaban ninguna operación real | Se eliminaron ambos métodos |
| Responsabilidad de entrada/salida en el modelo | `RegistroAcceso` imprimía por consola el resultado de un intento de acceso, mezclando el modelo de dominio con la interfaz | Se eliminó esa responsabilidad de `RegistroAcceso`; la comunicación del resultado queda a cargo de la clase que atiende la interfaz |

## 2. Patrones GRASP aplicados

### Experto en información

Cada clase valida aquello sobre lo que tiene la información necesaria. `RecitalEnVivo` valida su propia disponibilidad, capacidad y usuarios conectados; `AccesoEvento` valida la autorización del usuario y la compatibilidad de su plan de suscripción.

### Creador

La asociación entre `Evento` y `Artista` se establece en un único punto: el método `Evento.agregarArtista`. De esta forma la clase encargada de mantener la colección de artistas asociados es también la responsable de crear la relación, evitando que se duplique desde distintos lugares del sistema.

### Controlador

`GestorEventosEnVivo` centraliza la recepción de los eventos del sistema (iniciar, pausar, reanudar, finalizar y cancelar un evento; solicitar ingreso; expulsar un usuario) y coordina su ejecución delegando en las entidades correspondientes. La interfaz de consola y la interfaz gráfica solicitan estas operaciones al gestor en lugar de invocar directamente los métodos de las entidades del dominio.

### Alta cohesión

Cada clase mantiene una única responsabilidad bien definida. `RegistroAcceso` conserva únicamente los datos del registro de acceso; la comunicación del resultado al usuario queda a cargo de la interfaz correspondiente.

### Bajo acoplamiento

Al centralizar las operaciones en el gestor y mantener a cada entidad responsable únicamente de su propia información, las interfaces de consola y gráfica dependen exclusivamente del gestor, sin necesidad de conocer las reglas internas de las entidades del dominio.

Los diagramas de interacción correspondientes a cada patrón se encuentran en `DIAGRAMAS_GRASP.md`.
