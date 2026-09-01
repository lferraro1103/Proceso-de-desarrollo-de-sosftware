# Correcciones aplicadas — TP Proceso de Desarrollo de Software

Módulo: UADE Beats — Acceso a eventos en vivo (`TP/Grupo9_POO_EntregaFinal`)

## 1. Herencia (unificación de cuentas)

**Antes:** `Usuario` y `Artista` eran clases independientes, cada una con sus propios campos de identidad (`id`, `nombreUsuario`, `contrasena`, `activo`), duplicando la misma información y la misma lógica de login en dos lugares.

**Corrección:** se creó la clase base `Cuenta` (`modelo/Cuenta.java`) con `id`, `nombreUsuario`, `contrasena`, `activo`, `sesionIniciada` y `tipoUsuario` (enum `TipoUsuario`: USUARIO, ADMINISTRADOR, ARTISTA). `Usuario` y `Artista` ahora extienden `Cuenta` en vez de duplicar esos campos. Un Administrador es simplemente un `Usuario` con `tipoUsuario=ADMINISTRADOR`, no se creó una clase Java aparte para evitar una clase sin atributos propios.

## 2. Bad smells corregidos (según Clase 4 — Bad Smells)

| Smell | Dónde estaba | Corrección |
|---|---|---|
| **Código duplicado** | `GestorEventosEnVivo.solicitarIngreso` y `registrarFalloSinUsuario` construían el mismo evento "placeholder" (id=-1, CANCELADO) por separado | Se extrajo `eventoDesconocido()` como método único, reutilizado en ambos lugares |
| **Código duplicado** | `App.java` tenía `listarEventosParaExpulsion` con la misma lógica de filtrado que `listarEventosParaCambio` | Se eliminó el método aparte; se agregó el caso `"expulsar"` a `puedeAplicarAccion`/`obtenerNombreAccion` (mismo patrón que ya usaban iniciar/pausar/reanudar/finalizar) |
| **Nombre poco representativo / lógica escondida en parámetro** | `usuario.getPlanSuscripcion() == PlanSuscripcion.ARTIST_PASS` pasado directo como argumento booleano al crear `AccesoEvento` | Se agregó el método `Usuario.tieneAccesoPrioritario()` con nombre explícito |
| **God Class** | `App.java` concentraba 999 líneas: menú, lectura de consola, validaciones, construcción de entidades y persistencia | Se separó en `ConsoleIO` (lectura de consola), `DatosIniciales` (datos semilla) y `MenuAcciones` (ejecución de cada opción del menú); `App` quedó solo con `main()` y el despacho del switch |
| **Método largo** | `crearEventoManual` mezclaba selección de artista, lectura de datos, validación y construcción en un solo método | Se extrajeron `seleccionarArtistaParaEvento`, `leerDuracionMinutos`, `leerCapacidadMaxima` |
| **Método largo** | `registrarUsuario` mezclaba lectura y validación del nombre de cuenta con el resto del alta | Se extrajo `leerNombreUsuarioNuevo` |
| **Parámetros largos** | Constructor de `RecitalEnVivo` con 11 parámetros posicionales | Se creó `DatosRecital` (objeto de parámetros) agrupando los 10 datos propios del recital; el constructor quedó en `(id, DatosRecital)` |

## 3. Cambios dentro del alcance del programa (funcionalidad)

Correcciones sobre los 4 casos de uso definidos en el TP1 oficial:

- **CU-02 (ciclo de vida del evento):** "cancelar evento" existía en el modelo (`Evento.cancelarEvento()`) pero no estaba enganchada a ninguna opción de menú — se agregó al menú de consola.
- **CU-03 (expulsar usuario):** la expulsión no dejaba trazabilidad ni notificaba al usuario — ahora `GestorEventosEnVivo.expulsarUsuario` genera un `RegistroAcceso` de la expulsión y devuelve el usuario afectado para poder notificarlo (consola y Swing).
- **Persistencia:** se detectó que la contraseña nunca se guardaba en `usuarios.txt` (al recargar, siempre se hardcodeaba `"1234"`); se agregó el campo al formato de archivo.

## 4. Sistema de roles y permisos (en progreso)

A partir del TP1 oficial (CU-04: "Registrar artista y crear evento"), se está agregando un sistema de roles:

- **Fase 1 (completa):** `TipoUsuario` (enum), `Cuenta` (clase base), `Artista` con credenciales propias (antes no tenía login), persistencia actualizada de forma retrocompatible, y un Administrador sembrado por defecto (`admin`/`admin123`) para poder probar ese rol.
- **Fase 2 (pendiente):** login unificado para cualquier tipo de cuenta + middleware que calcula qué opciones de menú están permitidas según el rol.
- **Fase 3 (pendiente):** 4 pantallas de consola (login + menú Usuario + menú Artista + menú Administrador) y alta real de artistas con credenciales por parte del Administrador.

---
*Notas de trabajo, no parte de la entrega formal del TP.*
