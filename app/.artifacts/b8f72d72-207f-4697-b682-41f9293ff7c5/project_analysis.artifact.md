# Análisis de Lógica y Arquitectura del Proyecto (IncidentApp)

Este documento detalla la estructura, organización y el flujo lógico de la aplicación móvil **IncidentApp**, diseñado para que puedas explicar con total seguridad y dominio técnico a tu profesor.

---

## 1. Arquitectura General y Tecnologías

El proyecto sigue una arquitectura modular orientada a **Activities y Fragments** en Java, utilizando las prácticas recomendadas de Android moderno:
- **View Binding**: Para la vinculación segura de vistas XML con código Java sin necesidad de `findViewById`.
- **Jetpack Navigation Component (`nav_graph.xml`)**: Gestiona la navegación jerárquica entre pantallas (Fragments) dentro de una única `MainActivity`.
- **Retrofit 2**: Librería HTTP para consumir la API REST desarrollada en **Python Flask** conectada a una base de datos MySQL (con autenticación **Flask-JWT**).
- **SharedPreferences**: Almacenamiento local seguro para guardar el token JWT de sesión (`TOKEN_JWT`) y el rol del usuario (`USER_ROLE`).

---

## 2. Mapa de Paquetes y Ubicación de Componentes

Todo el código fuente se encuentra en el paquete base:
`com.example.py_damb2026ii`

### A. Capa de Red y Endpoints (`Interface/` y `network/`)
* **`Interface/JCPyAnyApi.java`**:
  * **¿Qué hace?**: Define todas las rutas (endpoints) de comunicación con el servidor Flask (por ejemplo: `auth`, `api_obtenerIncidentes`, `api_registrarIncidente`, `api_listarSucursales`, `api_insertarSucursal`, `api_asignarTecnico`, `api_cambiarEstadoIncidente`, etc.).
* **`network/`**:
  * Contiene clases de entrada/entidades como `IncidentEntry`, `SucursalEntry` y `UbicacionTecnicoEntry` para mapear los objetos JSON recibidos de la API.

### B. Capa de Modelos de Datos (`model/`)
* **Peticiones (`Requ*`)**: `RequAuth`, `RequInsertarIncidente`, `RequInsertarSucursal`, `RequInsertarUsuario`. Estructuran los datos enviados al servidor mediante `POST` o `PUT`.
* **Respuestas (`Rpta*`)**: `RptaAuth`, `RptaObtenerIncidentes`, `RptaDetalleIncidente`, `RptaObtenerSucursales`, `RptaObtenerUsuarios`, etc. Interpretan las respuestas JSON del servidor (incluyendo el código de estado `code`, mensajes y datos).

### C. Actividades Principales (`activities/`)
* **`LoginActivity.java`**:
  * **Lógica**: Gestiona la autenticación. Envía credenciales (correo y contraseña) al endpoint `/auth` de Flask. Al recibir el token JWT y el objeto `user` con su `rol` (`Administrador`, `Técnico`, `Solicitante`), los almacena en `SharedPreferences` y redirige a `MainActivity`.
* **`MainActivity.java`**:
  * **Lógica**: Contenedor principal (`NavHostFragment`). Al cargar, lee el rol del usuario (`USER_ROLE`) de `SharedPreferences` y configura dinámicamente la navegación inicial y las acciones del botón flotante (FAB) según el perfil.

### D. Pantallas y Vistas por Rol (`Fragments`)
* **Vistas del Solicitante**:
  * **`SolicitanteHomeFragment.java`**: Menú principal del empleado (Reportar incidente, Mis incidentes, Notificaciones, Mi perfil).
  * **`AddIncidentFragment.java`**: Formulario para registrar un nuevo incidente (con selectores de categoría, prioridad con semáforo y sucursal).
  * **`IncidentConfirmationFragment.java`**: Pantalla de éxito tras registrar un ticket (ej. `#INC-008`).
* **Vistas del Técnico**:
  * **`TecnicoHomeFragment.java`**: Menú principal del técnico (Incidentes asignados, Escanear QR, Mapa de ubicación, Perfil).
  * **`MapFragment.java`**: Visualización de ubicaciones en Google Maps / OpenStreetMap de los incidentes y técnicos en campo.
  * **`UbicacionTecnicoService.java`**: Servicio en segundo plano para enviar la geoposición del técnico.
* **Vistas del Administrador**:
  * **`IncidentListFragment.java`**: Panel principal del Administrador. Muestra todas las incidencias del sistema, tarjetas de métricas (*Total, Pendientes, En proceso, Resueltos*) y accesos rápidos.
  * **`AdminReportsFragment.java`**: Gráficas y reportes estadísticos de incidentes por estado, prioridad, sucursal y tiempo promedio de resolución.
  * **`AddBranchFragment.java` & `BranchListFragment.java`**: Gestión y registro de sucursales.
  * **`AddCollaboratorFragment.java` & `ProfileFragment.java`**: Gestión de usuarios/colaboradores y perfil.

---

## 3. Flujo Lógico Paso a Paso (Para Explicar al Profesor)

1. **Inicio de Sesión (Login)**:
   * El usuario abre la app y se encuentra con `LoginActivity`.
   * Ingresa su correo y contraseña y presiona "Siguiente".
   * Retrofit realiza una petición `POST` al endpoint `/auth`. El servidor valida el `password_hash` en la base de datos MySQL y devuelve un JSON con el `access_token` y el objeto `user` (que contiene el `rol`).
   * La app guarda estos datos en `SharedPreferences` y abre `MainActivity`.

2. **Validación y Apertura por Roles**:
   * `MainActivity` consulta `SharedPreferences` para obtener el `USER_ROLE`.
   * Si el rol es **Administrador**, abre el panel de control con métricas y lista general de incidentes.
   * Si el rol es **Solicitante**, redirige al menú del solicitante para reportar o consultar sus tickets.
   * Si el rol es **Técnico**, redirige al menú del técnico para ver sus asignaciones y mapa.

3. **Consumo de APIs y Base de Datos**:
   * Todas las operaciones (listar, registrar, cambiar estados, asignar técnicos) viajan protegidas con la cabecera `Authorization: JWT <token>`.
   * Las respuestas del servidor Flask son mapeadas mediante Gson a objetos de Java para mostrarse en los `RecyclerView` con sus respectivos adaptadores (`IncidentCardRecyclerViewAdapter`, `BranchCardRecyclerViewAdapter`, etc.).
