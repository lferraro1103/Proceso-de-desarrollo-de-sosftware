# Casos de uso nuevos

Continúan la numeración del documento de casos de uso (CU-01 a CU-04). CU-05 y CU-06 corresponden a la iteración 3 (roles y permisos) y CU-07 a CU-10 a la iteración 4 (modo cliente, calendarios y transmisiones).

## CU-05: Iniciar sesión y usar el menú según el rol

**Actores:** Usuario, Artista, Administrador

**Descripción:** La persona ingresa su nombre de cuenta y su contraseña. El sistema verifica que la cuenta exista, que esté activa y que la contraseña coincida con el hash guardado (las contraseñas nunca se guardan en texto plano). Si es correcta, abre la sesión y muestra las opciones que la tabla de permisos habilita para su rol. Cada acción vuelve a verificar el permiso en el gestor, aunque la opción salga del menú. Si no puede iniciar sesión, informa el motivo (cuenta inexistente, contraseña incorrecta o cuenta inactiva).

**Clases involucradas:** GestorEventosEnVivo, ControlSesion, Permisos, Accion, Cuenta, Contrasenas, VentanaLogin, MenuPorRol

## CU-06: Registrar usuarios y activar o desactivar cuentas

**Actor:** Administrador

**Descripción:** El administrador registra usuarios con su nombre de cuenta, datos personales, contraseña y plan de suscripción. El sistema asigna el identificador, rechaza el alta si el nombre de cuenta ya existe y guarda la contraseña como hash. El administrador también puede desactivar una cuenta, que deja de poder iniciar sesión, se desconecta de los eventos en los que estaba y se registra su expulsión, o volver a activarla. No puede desactivar su propia cuenta.

**Clases involucradas:** GestorEventosEnVivo, Usuario, Contrasenas, RegistroAcceso

## CU-07: Conectarse a un servidor desde otra PC

**Actores:** Usuario, Artista, Administrador

**Descripción:** Desde otra computadora, la persona abre el programa en modo cliente e indica la dirección del servidor. Inicia sesión y el servidor le entrega un token y solo los datos que su rol puede ver: el artista, sus propios eventos; el usuario, los eventos y sus propios registros; el administrador, todo. Las contraseñas nunca viajan. Desde entonces opera con la misma ventana o consola de siempre: cada acción se envía al servidor, que verifica permisos y reglas y guarda en la base. Si el servidor no responde, el cliente informa que no pudo conectarse.

**Clases involucradas:** GestorRemoto, ClienteHttp, EstadoLocal, ManejadorApi, Sesiones, Instantanea, GestorEventosEnVivo

## CU-08: Agregar un evento al calendario

**Actores:** Usuario, Artista, Administrador

**Descripción:** La persona selecciona un evento que puede ver y elige Google Calendar o Apple Calendar. El sistema arma la dirección pública del calendario de ese evento, con un código secreto de la cuenta, y abre la aplicación elegida con la suscripción ya preparada. Desde ese momento el evento queda al día en su calendario: si cambia el horario se actualiza y si se cancela queda marcado como cancelado. Un artista solo puede hacerlo con sus propios eventos. Requiere que el servidor tenga una dirección pública con https; si no la tiene, el sistema informa el motivo.

**Clases involucradas:** GestorEventosEnVivo, VinculoCalendario, VinculoGoogle, VinculoApple, PublicadorHttp, ManejadorCalendario, FormatoIcs

## CU-09: Ver la transmisión de un evento en curso

**Actores:** Usuario, Artista, Administrador

**Descripción:** Con el evento EN_CURSO, la persona accede a la transmisión. Un usuario la obtiene al ingresar al evento (CU-01); el artista, solo en sus propios eventos; el administrador, en cualquiera. El sistema consulta al servicio de transmisiones, que elige un recital del artista del evento, y abre en el navegador una página con el reproductor a pantalla completa. El video arranca en el minuto que corresponde según la hora de inicio del evento, no muestra cuánto va, no permite pausar, adelantar ni volver, y si es más corto que el evento vuelve a empezar en bucle. Si no hay un recital disponible o el servicio no responde, informa el motivo.

**Clases involucradas:** GestorEventosEnVivo, ProveedorTransmision, ClienteTransmision, ConsultaTransmision, Transmision, ServicioTransmision (Node.js)

## CU-10: Cerrar la transmisión al salir del evento

**Actores:** Usuario, Artista, Administrador

**Descripción:** Cuando una persona cierra sesión, es expulsada del evento (CU-03), su cuenta es desactivada (CU-06) o el evento finaliza o se cancela (CU-02), el sistema cierra su transmisión: la página detiene y elimina el reproductor y avisa que salió del evento. Si cierra la pestaña sin salir, la página deja de enviar señales de actividad y a los 15 segundos se da de baja. Mientras nadie mira una transmisión, el servicio no hace trabajo de fondo.

**Clases involucradas:** GestorEventosEnVivo, ProveedorTransmision, ClienteTransmision, ServicioTransmision (Node.js)

## Cambios sugeridos a los casos existentes

| Caso | Cambio |
|---|---|
| CU-01 | Agregar al final que, si el ingreso se autoriza y el evento tiene transmisión, el usuario entra directamente a ella (CU-09). |
| CU-03 | Agregar que, al expulsar, también se cierra la transmisión del usuario (CU-10). |
| CU-04 | Agregar que el evento se crea a nombre del artista y que un artista solo puede crear eventos propios. |
