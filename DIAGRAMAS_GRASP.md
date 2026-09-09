# Diagramas de interacción GRASP

Los siguientes diagramas siguen el formato de los ejemplos del PDF: plantean una responsabilidad y muestran el paso de mensajes entre objetos para resolverla.

## 1. Experto en información

**Pregunta:** ¿Quién debe decidir si un usuario puede ingresar a un recital?

La respuesta no corresponde a un único objeto. `AccesoEvento` es experto en autorización y plan; `RecitalEnVivo` es experto en disponibilidad, capacidad y usuarios conectados. `GestorEventosEnVivo` solamente coordina el caso de uso.

```mermaid
classDiagram
    direction LR

    class GestorEventosEnVivo {
        +solicitarIngreso(idUsuario, idEvento) RegistroAcceso
    }
    class AccesoEvento {
        -Usuario usuario
        -PlanSuscripcion planRequerido
        -boolean habilitado
        +validarAutorizacion() void
        +validarPlan() void
    }
    class RecitalEnVivo {
        -Set~Usuario~ usuariosConectados
        -PlanSuscripcion planMinimoRequerido
        +validarDisponibilidad(momento) void
        +verificarCapacidad() boolean
        +permitirIngreso(usuario) void
    }
    class Usuario {
        -PlanSuscripcion planSuscripcion
        -boolean activo
        +getPlanSuscripcion() PlanSuscripcion
        +isActivo() boolean
    }

    GestorEventosEnVivo --> AccesoEvento : crea y solicita validación
    AccesoEvento --> Usuario : consulta estado y plan
    GestorEventosEnVivo --> RecitalEnVivo : solicita permitirIngreso
    RecitalEnVivo --> Usuario : incorpora si corresponde
```

## 2. Creador

**Pregunta:** ¿Quién debe crear un `AccesoEvento` para procesar una solicitud de ingreso?

`GestorEventosEnVivo` es el creador apropiado porque administra los eventos, usuarios y el identificador incremental del acceso; además utiliza el objeto recién creado para coordinar la validación.

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant G as :GestorEventosEnVivo
    participant A as :AccesoEvento
    participant R as :RecitalEnVivo

    Operador->>G: solicitarIngreso(idUsuario, idEvento)
    G->>G: buscarUsuarioPorId(idUsuario)
    G->>G: buscarEventoPorId(idEvento)
    G->>A: create(id, usuario, recital, fecha, ...)
    G->>A: validarAutorizacion()
    G->>A: validarPlan()
    G->>R: permitirIngreso(usuario)
```

## 3. Bajo acoplamiento

**Pregunta:** ¿Cómo evitar que la interfaz conozca las reglas internas de un evento?

La alternativa preferida es que la interfaz solicite la acción al gestor y el gestor delegue en el evento. La interfaz no necesita conocer estados válidos ni controlar excepciones de dominio directamente.

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as :VentanaPrincipal
    participant G as :GestorEventosEnVivo
    participant E as :Evento

    Operador->>UI: iniciar evento(idEvento)
    UI->>G: iniciarEvento(idEvento)
    G->>G: buscarEventoPorId(idEvento)
    G->>E: iniciarEvento()
    E-->>G: estado actualizado o excepción
    G-->>UI: resultado
```

> Esto ya es lo que hace el código: `VentanaPrincipal.cambiarEstadoSeleccionado` y `MenuAcciones.ejecutarCambioDeEstado` llaman a `gestor.iniciarEvento/pausarEvento/reanudarEvento/finalizarEvento/cancelarEvento(idEvento)`, nunca a los métodos de `Evento` directamente.

## 4. Alta cohesión

**Pregunta:** ¿Quién debe realizar cada parte de `solicitarIngreso`?

El gestor conserva una responsabilidad cohesionada cuando coordina, sin asumir las validaciones que pertenecen a los objetos expertos.

```mermaid
sequenceDiagram
    autonumber
    participant G as :GestorEventosEnVivo
    participant A as :AccesoEvento
    participant R as :RecitalEnVivo
    participant Reg as :RegistroAcceso

    G->>A: validarAutorizacion()
    A-->>G: autorización válida o excepción
    G->>A: validarPlan()
    A-->>G: plan válido o excepción
    G->>R: permitirIngreso(usuario)
    R->>R: validarDisponibilidad(ahora)
    R->>R: verificarCapacidad()
    R-->>G: usuario conectado o excepción
    G->>Reg: generarRegistro(usuario, recital, resultado, motivo)
    G->>G: registrarAcceso(registro)
```

## 5. Controlador

**Pregunta:** ¿Qué objeto recibe los eventos del sistema y coordina el caso de uso?

`GestorEventosEnVivo` representa el subsistema de eventos y funciona como fachada de dominio entre consola/Swing y las entidades.

```mermaid
classDiagram
    direction LR

    class Interfaz {
        <<UI>>
        solicitar ingreso
        iniciar evento
        pausar evento
        reanudar evento
        finalizar evento
        cancelar evento
        expulsar usuario
    }

    class GestorEventosEnVivo {
        <<Controller>>
        +solicitarIngreso(idUsuario, idEvento) RegistroAcceso
        +crearEvento(evento, artista) void
        +expulsarUsuario(idEvento, idUsuario) Usuario
        +iniciarEvento(idEvento) void
        +pausarEvento(idEvento) void
        +reanudarEvento(idEvento) void
        +finalizarEvento(idEvento) void
        +cancelarEvento(idEvento) void
    }

    class Dominio {
        <<Entidades expertas>>
        Evento
        RecitalEnVivo
        AccesoEvento
        RegistroAcceso
    }

    Interfaz --> GestorEventosEnVivo : eventos del sistema
    GestorEventosEnVivo --> Dominio : delega reglas
```

## Secuencia completa: ingreso autorizado o rechazado

```mermaid
sequenceDiagram
    autonumber
    actor Operador
    participant UI as :MenuAcciones
    participant G as :GestorEventosEnVivo
    participant A as :AccesoEvento
    participant R as :RecitalEnVivo
    participant Reg as :RegistroAcceso

    Operador->>UI: solicitar ingreso
    UI->>G: solicitarIngreso(idUsuario, idEvento)
    G->>A: create(...)
    G->>A: validarAutorizacion()
    G->>A: validarPlan()

    alt usuario autorizado y plan suficiente
        G->>R: permitirIngreso(usuario)
        R->>R: validarDisponibilidad(ahora)
        R->>R: verificarCapacidad()
        R-->>G: usuario conectado
        G->>Reg: generarRegistro(usuario, recital, true, "")
    else autorización, plan o recital inválido
        A-->>G: AccesoDenegadoException
        G->>Reg: registrarIntentoFallido(usuario, recital, motivo)
    end

    G->>G: registrarAcceso(registro)
    G-->>UI: RegistroAcceso
    UI-->>Operador: muestra resultado
```
