# Investigación y decisiones del diagrama de clases

Revisión correctiva del 21/10/2026, sobre `361a7bb`: iteración 4 más `iteracion-4` en `443c4a7`. Proceso GSD ejecutado mediante revisión del código: diagnóstico, hipótesis, investigación, plan, implementación y verificación persistidos en `.planning/`.

## Especificación y herramientas consultadas

- [OMG UML 2.5.1, documento normativo formal/17-12-05](https://www.omg.org/spec/UML/2.5.1/PDF). Se descargó y se leyó el texto de los apartados relevantes. SHA-256 del PDF: `416B57E1933780EB48BD60FE513E031DA220C28A521BDD334A366BEBC78A463E`.
- [Ficha oficial de la especificación](https://www.omg.org/spec/UML/2.5.1).
- [Notación de clases de PlantUML](https://plantuml.com/class-diagram), [motor Graphviz](https://plantuml.com/graphviz-dot) y [limitaciones de splines de Graphviz](https://graphviz.org/docs/attrs/splines/).
- Java Language Specification SE 21: [clases, herencia, enums y records](https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html) e [interfaces](https://docs.oracle.com/javase/specs/jls/se21/html/jls-9.html).

Se usa PlantUML 1.2026.9 con su Graphviz 2.44.1, ya disponible y probado con `-testdot`. No hizo falta instalar una aplicación de escritorio. Se añadió `svglib` al Python de trabajo para llevar los SVG a PDF conservando vectores. La lámina principal tiene disposición manual y rutas auditables; no usa el atlas rechazado.

## Reglas normativas empleadas

| Elemento | Decisión de representación | Apartado UML |
|---|---|---|
| Clasificador | Nombre, atributos y operaciones; visibilidad y tipos. Abstractas identificadas; interfaces y enumeraciones con palabra clave explícita | 9.2.4, 9.5.4, 10.4 |
| Generalización | Línea continua, triángulo hueco en el general | 9.2.4.2 |
| Realización | Línea discontinua, triángulo hueco en el contrato | 7.7.4, 10.4.4 |
| Dependencia | Discontinua, flecha hacia el proveedor utilizado | 7.7.3-4 |
| Asociación | Continua; punta abierta indica navegabilidad. Propiedad asociada puede suprimirse del compartimento de atributos | 9.5.4, 11.5.4 |
| Multiplicidad | Se interpreta en el extremo próximo, respecto del otro. Un extremo no rotulado no permite inferir cardinalidad | 7.5, 11.5.3-4 |
| Composición | Rombo negro en el compuesto, una parte con máximo un propietario simultáneo y responsabilidad sobre su existencia | 9.5.3, 11.5.4 |
| Agregación compartida | Rombo blanco; su semántica precisa depende del modelo. No se agrega sin justificarla | 9.5.3 |

## Diagnóstico de los intentos anteriores

La versión inicial mezclaba demasiadas dependencias en una lámina y dejaba rótulos amontonados. El reemplazo posterior ordenó fichas, pero repitió destinos por relación: servía como catálogo, no como vista conectada de clases. La ausencia de colisiones en ese catálogo no resolvía el pedido. Ambos antecedentes quedan expresamente rechazados.

El merge cambió además la base: ahora hay **91 fuentes y 97 tipos con nombre**, no 84 y 89. Se agregó transmisión, nuevos contratos, un DTO y clases auxiliares. Reutilizar las cifras o el inventario anterior habría documentado otro sistema.

## Decisiones verificadas en Java

### Identidad y roles

`Cuenta` es abstracta. `Usuario` y `Artista` extienden `Cuenta`; ninguna extiende a la otra. No existe `Administrador.java`: el administrador es una instancia de `Usuario` con `TipoUsuario.ADMINISTRADOR`. `Evento` es abstracta y `RecitalEnVivo` la especializa.

### Participación en eventos

`Evento.artistas` y `Artista.eventosCreados` son dos campos diferentes. Se muestran como dos asociaciones dirigidas, con sus nombres exactos. `Evento.agregarArtista` actualiza ambos lados; `Artista.crearEvento` sólo actualiza su lista. No se promete una asociación bidireccional siempre consistente que el código no garantiza. Los dos objetos tienen existencia independiente y se reutilizan: no hay rombo negro ni blanco justificado aquí.

### Conectados, tickets y registros

`RecitalEnVivo.usuariosConectados` referencia usuarios existentes; expulsarlos o finalizar el evento no elimina cuentas. `AccesoEvento` y `RegistroAcceso` almacenan referencias a usuario y evento: asociación. `ControlAcceso.procesarIngreso` crea un ticket como variable local; eso es una dependencia de creación, no una parte almacenada dentro del controlador.

### Propiedad compuesta

Se revisaron individualmente doce propiedades: los controles internos y la política del gestor; `ControlSesion.permisos`; las políticas privadas de `PoliticasVisibilidad`; los cuatro repositorios de `PersistenciaSqlite`; el cliente HTTP y los permisos del proxy; y las sesiones privadas del manejador HTTP. En todos esos casos se construyen las partes dentro del propietario, no se reciben de otro propietario ni se exponen o transfieren, y no existe ruta pública para reemplazarlas o compartirlas. Se modela esa propiedad lógica, independiente del instante en que actúe el recolector de Java.

Las tres instancias del mapa de políticas son dos `VisibilidadGeneral` distintas y una `VisibilidadArtista`. Por eso el extremo de partes de `porRol` vale **3**, no simplemente `0..*`.

`Persistencia`, `PublicadorCalendario`, `ProveedorTransmision` y `Notificador` son colaboradores inyectados y pueden compartirse. Conservan asociaciones sin composición. `EstadoLocal` se reemplaza al actualizar: se deja como asociación, conservadoramente. No se infiere propiedad por encontrar `new`, `final` o una colección aislados del resto del código.

### Multiplicidades

Las colecciones de referencias se representan `0..*`. Las referencias escalares recibidas sin comprobación de `null` se representan `0..1`, incluso si el arranque normal les pasa un objeto. Se evita confundir una intención del dominio con una condición que la implementación no impone. En el extremo del compuesto se usa `0..1`, cumpliendo el máximo de un propietario. Las propiedades internas inicializadas tienen una parte, salvo el mapa de tres políticas.

No se dibujan multiplicidades en herencias, realizaciones o dependencias. Los enum son tipos de atributos y se detallan como enumeraciones, sin dibujar una asociación entidad-entidad por cada aparición de un valor enum.

### Contratos y patrones

`ServicioEventos` extiende tres interfaces: generalización de interfaces. El gestor local y `GestorRemoto` implementan ese contrato: realización. Las políticas concretas realizan `PoliticaVisibilidadEvento`. Los vínculos Google y Apple realizan `VinculoCalendario`. `ClienteTransmision` realiza `ProveedorTransmision`. `PersistenciaSqlite` realiza `Persistencia`.

La revisión conserva Strategy y Facade existentes. No se agregaron Composite o Decorator para adornar el UML. Las clases anidadas con nombre se inventarían como clasificadores; anidamiento léxico no equivale a composición de instancias. Los tipos del JDK aparecen sólo como referencias necesarias de herencia o contratos; no se presentan como código propio.

## Alcance de las vistas y comprobaciones

- Diagrama principal: siete clases de dominio, diez relaciones, cada clase una sola vez y rutas sin cruces ni texto encima de conectores.
- Panorama: los 97 tipos y sus relaciones estructurales, agrupados por paquete, sin miembros para facilitar la navegación.
- Diagrama de implementación completo: los 97 tipos una sola vez, todos sus campos y operaciones inventariados, relaciones estructurales y tipos externos necesarios. Se conserva como SVG grande para ampliarlo, acompañado por PNG; no se lo comprime en una página A4.
- Detalles por módulo: cada uno de los 97 tipos tiene una vista con todos sus miembros. Los vecinos de otros módulos aparecen una vez como referencia dentro de esa vista.
- Cuadernillo A4: diez vistas acotadas y dos páginas explicativas; conserva texto vectorial, ambos tamaños de orientación A4 y los SVG principal e integral adjuntos.
- Matriz: 82 relaciones estructurales y 359 dependencias AST resueltas. La matriz conserva todas las dependencias; las láminas estructurales no se saturan con cada llamada temporal.

La verificación automática contrasta herencias e interfaces con bytecode, asociaciones con campos, composiciones con las construcciones internas y clasificadores/miembros con el inventario. Audita cajas y textos de los SVG exportados. La principal también audita sus segmentos, texto sobre conectores y cruces. Se revisan visualmente las salidas reales y cada página del PDF; esos controles no se presentan como demostración automática de calidad UML.

Las pequeñas separaciones de multiplicidades que Graphviz deja juntas en las dos vistas integrales se registran en `ajustes-rotulos.json`; no cambian valores, clases ni conectores, y el PNG se vuelve a renderizar desde el SVG final.
