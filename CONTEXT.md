# Contexto del proyecto - UADE Beats

Actualizado: 21/10/2026. Rama: `iteracion-4`. Base remota de la integración: `443c4a7`.

## Código y arquitectura

Proyecto Java del Grupo 9, con consola y Swing, API HTTP, SQLite, calendarios y servicio Node.js de transmisión. El módulo Java está en `TP/Grupo9_POO_EntregaFinal/Codigo/`.

El contrato `ServicioEventos` reúne sesión, consultas y comandos. `GestorEventosEnVivo` coordina los casos de uso; `GestorRemoto` implementa el proxy HTTP. Las políticas `PoliticaVisibilidadEvento`, `VisibilidadGeneral` y `VisibilidadArtista` separan el alcance de lectura de los permisos. El proveedor de transmisión recibido por constructor se conserva.

## Entrega de clases

94 tipos en el código. Diagrama completo en `TP/Grupo9_POO_Diagramas_Pdf/fuentes/clases-completo-paquetes.puml` y sus SVG/PNG en `imagenes/`. Incluye atributos, métodos, relaciones con cardinalidades y líneas ortogonales.

Las relaciones de herencia, realización, asociación y composición están diferenciadas. No se inventa una clase Administrador: es un rol de Usuario. Los colaboradores inyectados se comparten por asociación; los controles y repositorios internos tienen propiedad compuesta justificada.

## Verificación

110 comprobaciones en `Codigo/test/PruebasIteracion4.java`. Compilación y ejecución mediante `tools/uml/regenerar.ps1 -SoloPruebas`. No se ejecutaron servicios externos ni reproducción real durante esas pruebas.

## Continuidad

La planificación local permanece en `.planning/`, excluida del repositorio. Se consolida el trabajo sobre la historia existente de iteración 4, sin incorporar la historia de la rama de desarrollo descartada. Los diagramas anteriores rechazados se conservan sólo como respaldo local. La documentación principal está en `docs/iteracion-4/README.md`.
