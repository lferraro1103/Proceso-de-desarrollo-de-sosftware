# Revisión de patrones — Iteración 4

Fecha: 20/10/2026. Base: `519714c`; rama de trabajo: `iteracion-4`.

## Resultado y decisiones

| Patrón | Situación encontrada | Decisión de iteración 4 |
|---|---|---|
| Strategy | Ya existe en `VinculoCalendario`, con `VinculoGoogle` y `VinculoApple` | Conservar y documentar; probar su invocación desde servicio local y proxy remoto |
| Strategy | El alcance de consulta por rol estaba dentro de `GestorEventosEnVivo.esVisiblePara` | Extraer dos políticas concretas y un contexto selector; conservar los resultados existentes |
| Facade | `GestorEventosEnVivo` ofrece casos de uso sobre sesión, acceso, persistencia y calendarios mediante `ServicioEventos` | Reconocer y conservar; evitar una fachada adicional que solo reenvíe llamadas |
| Composite | La aplicación construye árboles de componentes Swing; no hay una jerarquía recursiva de eventos en el dominio | Documentar el uso del Composite de la biblioteca gráfica; no introducirlo en el dominio sin requisito |
| Decorator | No se encontró una familia propia de envoltorios que conserve un contrato y agregue comportamiento apilable | No implementarlo ahora; documentar alternativas y condiciones concretas de adopción |

La elección se basa en la intención y las colaboraciones del patrón, no solo en la presencia de una interfaz, una lista o una clase que contiene otra.

## 1. Strategy existente: calendarios

Según el [catálogo Strategy](https://refactoring.guru/design-patterns/strategy), el patrón separa algoritmos intercambiables detrás de un contrato. En este proyecto, la tarea común es abrir una suscripción al feed de un evento; cambia el mecanismo de cada proveedor.

| Participante | Código real | Responsabilidad |
|---|---|---|
| Estrategia | `src/gestor/VinculoCalendario.java` | `getNombre()` y `vincular(urlFeed)` |
| Estrategia concreta | `src/calendario/VinculoGoogle.java` | Construir el enlace de Google y abrir el navegador |
| Estrategia concreta | `src/calendario/VinculoApple.java` | Abrir Calendar en macOS o delegar el protocolo webcal al equipo |
| Contexto local | `GestorEventosEnVivo.vincularCalendario` | Obtener una URL autorizada e invocar el algoritmo recibido |
| Contexto remoto | `GestorRemoto.vincularCalendario` | Pedir la URL al servidor e invocar la estrategia en el equipo cliente |
| Cliente que selecciona | `ui/VentanaPrincipal` y `app/MenuAcciones` | Elegir la implementación Google o Apple |

Es Strategy aunque el contexto reciba el objeto como parámetro y no lo guarde en un atributo. La selección sigue siendo intercambiable y el gestor no condiciona la apertura por marca.

Pruebas: una estrategia de prueba captura el feed obtenido por ambos gestores; se verifica que un evento ajeno no llegue a ejecutar la estrategia y que un error `IOException` del proveedor se propague. No se abre un navegador real ni se confirma una suscripción externa durante las pruebas.

`Persistencia`, `Notificador` y `PublicadorCalendario` también son puntos de sustitución por inyección. En esta rama cada uno tiene una sola implementación productiva: es más preciso describirlos como puertos de infraestructura que afirmar que existe una familia activa de algoritmos Strategy para todos ellos.

## 2. Strategy aplicado: visibilidad de eventos

### Problema concreto

El gestor contenía el algoritmo que decide si una cuenta puede consultar un evento. Usuarios y administradores ven el catálogo; un artista ve eventos donde participa. Este comportamiento afecta listados, búsquedas individuales, la instantánea remota y la autorización del feed de calendario.

La separación permite probar las dos variantes sin iniciar servidor ni ventana y localizar futuros cambios de alcance fuera de una clase gestora de gran tamaño. El beneficio actual es principalmente separación y verificabilidad; no se afirma una mejora de rendimiento ni una necesidad de reemplazo dinámico por el usuario final.

### Implementación

- `PoliticaVisibilidadEvento`: contrato `esVisible(Cuenta, Evento)`.
- `VisibilidadGeneral`: catálogo completo cuando existen cuenta y evento.
- `VisibilidadArtista`: busca participación comparando el ID persistente del artista.
- `PoliticasVisibilidad`: contexto sin estado de sesión, con una tabla de tres roles y dos algoritmos.
- `GestorEventosEnVivo.esVisiblePara`: delega en el contexto. Las llamadas existentes siguen pasando por el mismo punto central.

Se eligieron dos algoritmos, no tres clases idénticas por cada rol. La estrategia del artista usa los artistas asociados al evento y no solamente `Artista.eventosCreados`, preservando la semántica de participación y los objetos reconstruidos desde SQLite.

### Invariantes preservadas

1. Sin cuenta o sin evento no hay visibilidad.
2. Usuario y administrador conservan el catálogo completo.
3. Artista ajeno no ve el evento, aunque conozca su ID.
4. Una instancia de artista reconstruida con el mismo ID conserva visibilidad.
5. Igualdad numérica entre IDs de distintos tipos de cuenta no cambia el algoritmo seleccionado: se usa el rol.
6. La política determina el alcance por evento; `Permisos` y `ControlSesion` siguen autorizando acciones. `buscarEventoParaCambio` reutiliza ese alcance después de exigir el permiso. Ver no implica poder editar.
7. API, rutas, DTO y esquema SQLite permanecen compatibles.

El costo de cuatro tipos adicionales es deliberado para hacer explícitos contrato, variantes y selección. En una aplicación que no necesite esta separación, el condicional anterior era válido; no todo `if` necesita un patrón.

## 3. Facade existente

Una [Facade](https://refactoring.guru/design-patterns/facade) ofrece acceso simplificado a un subsistema. `GestorEventosEnVivo` coordina `ControlSesion`, `ControlAcceso`, las entidades del modelo, `Persistencia` y `PublicadorCalendario`. Las vistas utilizan el contrato `ServicioEventos` en lugar de manejar directamente esos colaboradores.

`ServicioEventos` agrupa tres interfaces pequeñas: sesión, consultas y comandos. El contrato es la cara pública; el objeto que realmente implementa la colaboración de fachada es el gestor. `GestorRemoto` conserva ese contrato y actúa como Proxy remoto, delegando la ejecución autoritativa al servidor.

No se agregó `FachadaEventos` por encima del gestor: duplicaría el contrato y obligaría a mantener reenvíos sin simplificar el uso. Si el módulo crece, conviene separar internamente servicios de cuentas, catálogo y acceso y mantener la fachada como punto de entrada.

Límite importante: existen métodos de bajo nivel del gestor para carga y datos iniciales. La fachada no es por sí sola una barrera universal de autorización; los casos de uso expuestos a usuarios pasan por los controles de sesión y la API.

## 4. Composite: dónde existe y dónde sería razonable

[Composite](https://refactoring.guru/design-patterns/composite) requiere una estructura recursiva en la que hojas y contenedores puedan tratarse mediante un contrato común. Tener `List<Evento>` en un gestor no cumple esa condición por sí solo.

### Uso indirecto real en la UI

`VentanaPrincipal` construye paneles anidados y agrega botones, tablas y otros paneles con `add(...)`. `Formulario.agregar` acepta cualquier `Component`. La jerarquía AWT/Swing provee el contrato común y el contenedor recursivo: [Container hereda de Component y contiene componentes](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Container.html).

Esto es aprovechamiento del diseño Composite de la biblioteca, no una implementación propia de Composite para eventos. No corresponde introducir clases duplicadas para reemplazar los contenedores Swing.

### Aplicación futura al dominio

Sería razonable si se pidieran festivales con escenarios, bloques o subeventos anidados y operaciones uniformes como consultar programación o cancelar una rama completa. Habría que definir qué significa capacidad, plan y autorización de un contenedor, evitar ciclos y decidir persistencia y API para la jerarquía.

Hoy no existe ese requisito ni una entidad de agrupación recursiva. Agregar Composite ahora modificaría el modelo y la base sin resolver una necesidad vigente. Decisión: no implementarlo en el dominio.

## 5. Decorator: evaluación y límite

[Decorator](https://refactoring.guru/design-patterns/decorator) agrega responsabilidades mediante envoltorios que mantienen el contrato del componente y admiten composición de capas. No se encontró ese diseño en clases propias de esta aplicación.

`GestorRemoto` comparte interfaz con el gestor, pero su intención es acceder al servicio remoto: es Proxy. `PersistenciaSqlite` implementa un contrato y utiliza repositorios; no envuelve otra `Persistencia`, por lo que tampoco es Decorator.

`JScrollPane` agrega desplazamiento visual a una tabla o lista, pero no conserva el contrato específico de `JTable` ni permite sustituirla en todos sus usos. La [documentación de JScrollPane](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/JScrollPane.html) describe su viewport y componentes auxiliares. Es preferible no presentar esa contención visual como prueba de un Decorator GoF propio.

### Candidatos evaluados

| Candidato | Beneficio posible | Condición previa / riesgo | Decisión |
|---|---|---|---|
| Decorar `Notificador` con medición o registro | Agregar observabilidad sin cambiar el canal | Definir privacidad y política de fallos; hoy el canal es inactivo | Posponer hasta tener notificaciones reales |
| Decorar `Persistencia` con métricas | Medir guardados y cargas conservando el contrato | Especificar instrumentación y evitar registrar información sensible | Posponer; no hay requisito de medición |
| Decorar `Persistencia` con reintentos | Tolerar errores transitorios | El gestor ya muta memoria antes de guardar; un reintento genérico no resuelve consistencia | No aplicar como arreglo automático |
| Decorar `ServicioEventos` con caché | Reducir consultas remotas | Invalidez por rol y sesión, datos obsoletos y cambio externo de eventos | No aplicar; riesgo superior al beneficio actual |

No se agregó un envoltorio vacío para mostrar el patrón. Si aparece un requisito real, hay puertos existentes donde aplicarlo sin cambiar el modelo.

## 6. Otros patrones y distinciones útiles

| Elemento | Clasificación y evidencia |
|---|---|
| Repositorios por entidad | Repository; concentran SQL y reconstrucción del dominio |
| `GestorRemoto` | Proxy remoto sobre `ServicioEventos` |
| Registros de `api/` | DTO; transporte sin exponer objetos internos ni hashes |
| `NotificadorInactivo` | Null Object; satisface el contrato sin generar efectos |
| `Arranque` | Raíz de composición; ensambla las implementaciones concretas |
| `EstadoEvento` | Enum con reglas de transición; no es una jerarquía de objetos State |
| `Evento` y `RecitalEnVivo` | Herencia del dominio; no prueba por sí sola Template Method ni Composite |

No se recomienda convertir seis estados simples en seis clases State hasta que sus comportamientos y transiciones requieran esa complejidad. Tampoco reemplazar la comparación jerárquica de `PlanSuscripcion.cubre` por una Strategy por plan: hoy es una regla ordinal uniforme.

## 7. Hallazgos de revisión que no requieren otro patrón

- El rollback de SQLite revierte SQL, pero las operaciones del gestor ya pueden haber modificado los objetos en memoria. Esto requiere una decisión transaccional específica; añadir Decorator no lo soluciona.
- La URL pública se considera configurada si no está vacía. `PublicadorHttp` no valida que el esquema sea HTTPS, aunque su mensaje y el README lo exigen para el uso esperado. Se documenta el comportamiento real.
- Las sesiones remotas no vencen por tiempo.
- La UI local puede leer objetos fuera del bloqueo del gestor, como ya advierte el README.
- Hay altas de bajo nivel pensadas para arranque sin sesión. No deben exponerse como rutas nuevas sin autorización.
- No existe una validación central exhaustiva de campos del dominio; no se afirma que cualquier dato incorrecto ya sea rechazado en todas las vías de entrada.

Estos son límites del sistema existente, no regresiones introducidas por la extracción de visibilidad. Su corrección cambia comportamientos o requiere definir alcance adicional.

## Fuentes y criterio de investigación

Se contrastaron fuentes Java, bytecode compilado, documentación anterior, permisos y recorridos de API. Las definiciones se consultaron en el catálogo de sus autores y la documentación oficial del JDK, enlazados junto a cada decisión. La notación se basó en [UML 2.5.1 de OMG](https://www.omg.org/spec/UML/2.5.1) y la [documentación oficial de clases de PlantUML](https://plantuml.com/class-diagram).

Los tipos concretos y sus colaboraciones son evidencia del repositorio; la recomendación de adoptar o posponer un patrón es una decisión de ingeniería de esta revisión.
