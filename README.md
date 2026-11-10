# UADE Beats - Entrega final

Proceso de Desarrollo de Software (3.4.210) - Martes mañana - 2C 2026
Grupo 10: Franco Beron y Leandro Ferraro

## Qué es

UADE Beats es un sistema para gestionar recitales en vivo. Hay tres roles:

- El administrador da de alta usuarios y artistas, maneja los eventos y puede expulsar gente.
- El artista crea sus eventos, los inicia, los pausa y los termina.
- El usuario pide entrar a un evento. El sistema lo deja pasar o no según su plan (Free, Premium o Artist Pass), el estado del evento, el horario y la capacidad.

Todos los intentos de ingreso, los que entran y los rechazados, quedan registrados. Los datos se guardan en una base SQLite. Además se puede usar desde otra PC conectándose a un servidor, suscribir un evento a Google Calendar o Apple Calendar y ver la transmisión de un evento que está en curso.

Partimos de un TP que teníamos de POO (la versión 0) y lo fuimos mejorando a lo largo de la cursada.

## Cómo lo corremos

Hace falta Java 17 o más nuevo. Desde `TP/Grupo9_POO_EntregaFinal/Codigo`, en PowerShell:

```
javac -encoding UTF-8 -cp "lib/*" -d out (Get-ChildItem -Recurse src,test -Filter *.java).FullName
java --enable-native-access=ALL-UNNAMED -cp "out;lib/*" Main
```

En Mac o Linux es así:

```
javac -encoding UTF-8 -cp "lib/*" -d out $(find src test -name "*.java")
java --enable-native-access=ALL-UNNAMED -cp "out:lib/*" Main
```

También se puede abrir la carpeta `Codigo` en IntelliJ, agregar `lib` como librería y correr `Main`.

Al arrancar, el programa pregunta si queremos la ventana o la consola y después pide usuario y contraseña. La base se crea sola la primera vez, con datos de prueba.

Algunas cuentas para probar:

- `admin` / `admin123`: administrador
- `lferraro` / `lferraro2024`: usuario
- `bandahorizonte` / `horizonte2024`: artista

Para probar los ingresos y la transmisión está el evento 106, que está en curso todo el tiempo. La transmisión necesita Node instalado. Los otros eventos sirven para ver los rechazos: el 100 figura en curso pero su horario ya pasó, el 102 está pausado y el 105 está cancelado.

Hay otros dos modos de arranque: `Main servidor` deja corriendo solo los datos y la API, y `Main cliente http://IP:8765` abre solamente la interfaz y trabaja contra ese servidor.

Las pruebas están en `test/PruebasIteracion4.java` y se compilan junto con el resto con los comandos de arriba. Se corren con `java -cp "out;lib/*" PruebasIteracion4`: hace 110 comprobaciones y al final tiene que decir OK.

## Cómo fue cambiando el proyecto

**Versión 0.** El TP de POO como estaba: una clase `App` de casi mil líneas con todo adentro, `Usuario` y `Artista` con los mismos datos repetidos y la persistencia en archivos de texto.

**Iteración 1 - Bad smells.** Creamos la clase `Cuenta` como base de `Usuario` y `Artista`. Partimos la God Class `App` en `ConsoleIO`, `DatosIniciales` y `MenuAcciones`. Agrupamos los 11 parámetros del constructor del recital en `DatosRecital`. También sacamos código duplicado y partimos métodos largos.

**Iteración 2 - GRASP.** Ordenamos quién hace qué. Cada validación quedó en la clase que tiene los datos: `AccesoEvento` valida la autorización y el plan, y `RecitalEnVivo` valida el estado, el horario y la capacidad. El gestor quedó como controlador y las pantallas dejaron de llamar directamente a los métodos de `Evento`. Borramos los métodos que no hacían nada y sacamos los `System.out` del modelo.

**Iteración 3 - SOLID y MVC.** Separamos los ingresos en `ControlAcceso` (responsabilidad única). Sacamos los `instanceof RecitalEnVivo` para que cualquier tipo de `Evento` sirva (abierto/cerrado y Liskov). El gestor pasó a depender de la interfaz `Persistencia` y no de la clase concreta (inversión de dependencias). Separamos modelo, vista y controlador, y agregamos los roles con sus permisos y la ventana de login.

**Iteración 4.** Pasamos de los TXT a SQLite, con un repositorio por tabla. Agregamos el servidor con API HTTP y el modo cliente. Las contraseñas pasaron a guardarse con hash. Sumamos la suscripción a calendarios y el servicio de transmisiones. Las vistas pasaron a depender de la interfaz `ServicioEventos`, así les da lo mismo usar el gestor local o el remoto. Para lo que ve cada rol usamos el patrón Strategy (`PoliticasVisibilidad`).

**Iteración final.** Rehicimos el diagrama de clases con las correcciones que nos marcaron:

- todas las relaciones tienen nombre;
- las interfaces se dibujan punteadas, con triángulo y sin cardinalidad;
- solo es asociación cuando hay un atributo, y lo que se crea o se usa dentro de un método es dependencia;
- las flechas van en el sentido correcto;
- agregamos las relaciones que faltaban.

También rehicimos los diagramas GRASP sobre el código actual. En el código hicimos tres cambios chicos: sacamos el atributo `exclusivo` de los eventos, que no se usaba; los eventos sin video propio ahora usan el video de demostración; y el administrador ya no puede solicitar ingresos, porque no tiene sentido que entre a un recital.

## Patrones que usamos

- GRASP: experto en información, creador, controlador, bajo acoplamiento y alta cohesión. Los diagramas de cada uno están en `Diagramas GRASP.pdf`.
- SOLID: los cinco, como se cuenta en la iteración 3.
- MVC, para separar el modelo, las vistas (ventana y consola) y el controlador (`GestorEventosEnVivo`).
- Strategy, para lo que ve cada rol (`VisibilidadGeneral` y `VisibilidadArtista`).
- Repository, uno por tabla de la base.
- Proxy remoto (`GestorRemoto`) y DTO para la API.

