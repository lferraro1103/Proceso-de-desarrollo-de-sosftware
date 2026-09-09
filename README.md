# Corrección de código — Clase 4: Bad Smells

Módulo: UADE Beats — Acceso a eventos en vivo

## 1. Herencia

`Usuario` y `Artista` son dos tipos de cuenta dentro de la plataforma, cada uno con datos de identidad y autenticación propios (identificador, nombre de cuenta, contraseña y estado de la cuenta). Para no duplicar esos atributos y su comportamiento en dos clases distintas, se define una clase base `Cuenta` de la cual ambas heredan. El tipo de cuenta se modela con el enumerado `TipoUsuario` (USUARIO, ADMINISTRADOR, ARTISTA), heredado también desde `Cuenta`. Un administrador se representa como un `Usuario` cuyo `TipoUsuario` es ADMINISTRADOR, sin necesidad de una clase adicional que no aportaría atributos propios.

## 2. Bad smells corregidos

| Smell | Descripción | Solución aplicada |
|---|---|---|
| Código duplicado | La construcción de un evento "placeholder" para registrar intentos de acceso sin evento real se repetía en dos métodos de `GestorEventosEnVivo` | Se extrajo el método `eventoDesconocido()`, reutilizado en ambos casos |
| Código duplicado | El filtrado de eventos disponibles para expulsar usuarios repetía la misma lógica que el filtrado usado para cambiar el estado de un evento | Se unificó bajo un mismo método parametrizado por la acción a realizar |
| God Class | La clase `App` concentraba el menú de consola, la lectura de datos, las validaciones, la construcción de entidades y la persistencia | Se distribuyeron las responsabilidades en las clases `ConsoleIO`, `DatosIniciales` y `MenuAcciones`, dejando a `App` solo el inicio del programa y el despacho de opciones |
| Método largo | La creación manual de un evento mezclaba la selección del artista, la lectura de datos y la construcción del objeto en un único método | Se dividió en métodos independientes para cada paso |
| Método largo | El registro de un usuario mezclaba la lectura y validación del nombre de cuenta con el resto del alta | Se separó la validación del nombre de cuenta en un método propio |
| Parámetros largos | El constructor del recital recibía once parámetros individuales | Se agrupan los datos propios del recital en un objeto `DatosRecital` |
| Nombre poco representativo | La condición para determinar el acceso prioritario de un usuario se expresaba directamente como comparación de su plan de suscripción | Se incorporó el método `tieneAccesoPrioritario()` en `Usuario`, con un nombre que expresa la responsabilidad |
