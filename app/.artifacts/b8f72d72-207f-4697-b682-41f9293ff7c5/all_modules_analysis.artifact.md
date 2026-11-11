# Análisis Detallado de Todos los Módulos de IncidentApp

Este documento desglosa cada uno de los **módulos funcionales** implementados en el sistema, indicando qué hacen, qué archivos los componen y cómo interactúan con la base de datos y la API Flask.

---

## 1. Módulo de Autenticación y Seguridad (Login & Roles)
* **¿Qué hace?**: Valida las credenciales del usuario contra la tabla `usuarios`, genera y valida el token **JWT** (`Flask-JWT`), y controla qué interfaz ve el usuario según su rol (`Administrador`, `Técnico`, `Solicitante`).
* **Archivos clave**:
  * `LoginActivity.java` y `activity_login.xml`: Pantalla de inicio de sesión.
  * `RptaAuth.java`: Modelo que mapea la respuesta del servidor (`access_token` y el objeto `user` con su `rol`).
  * `MainActivity.java`: Lee el `USER_ROLE` de `SharedPreferences` y rutea al usuario a su panel correspondiente.

## 2. Módulo de Incidentes (Tickets)
* **¿Qué hace?**: Es el núcleo del sistema. Permite registrar nuevos incidentes (con categoría, prioridad, sucursal, fotos y QR), listarlos, ver detalles, cambiar estados (*Abierto, En proceso, Resuelto, Cerrado*) y asignar técnicos.
* **Archivos clave**:
  * `IncidentListFragment.java`: Lista los incidentes (con filtrado y vista de tarjetas).
  * `AddIncidentFragment.java` y `fragment_add_incident.xml`: Formulario de registro de incidentes.
  * `IncidentDetailFragment.java`: Visualiza el detalle completo, descripción, datos del activo, sucursal e historial de cambios.
  * `IncidentCardRecyclerViewAdapter.java` y `usat_incident_card.xml`: Adaptador y diseño visual de cada tarjeta de incidente.
  * `IncidentEntry.java`, `RptaDetalleIncidente.java`, `RequInsertarIncidente.java`: Modelos de datos.

## 3. Módulo de Sucursales (Sedes)
* **¿Qué hace?**: Permite registrar y listar las sucursales u oficinas de la empresa, las cuales están asociadas a los incidentes y cuentan con coordenadas geográficas (`latitud`, `longitud`) para su visualización.
* **Archivos clave**:
  * `BranchListFragment.java`: Muestra el listado de sucursales registradas.
  * `AddBranchFragment.java` y `fragment_add_branch.xml`: Formulario para registrar nuevas sucursales.
  * `BranchCardRecyclerViewAdapter.java` y `usat_branch_card.xml`: Adaptador y diseño de las tarjetas de sucursales.
  * `SucursalEntry.java`, `RequInsertarSucursal.java`, `RptaObtenerSucursales.java`: Modelos de datos.

## 4. Módulo de Mapa y Ubicación en Tiempo Real
* **¿Qué hace?**: Integra mapas (Google Maps / OpenStreetMap) para ubicar geográficamente las sucursales y el seguimiento en tiempo real de los técnicos en campo.
* **Archivos clave**:
  * `MapFragment.java` y `fragment_map.xml`: Pantalla del mapa interactivo.
  * `UbicacionTecnicoService.java`: Servicio en segundo plano encargado de enviar la posición GPS del técnico al servidor.
  * `UbicacionTecnicoEntry.java`, `RptaObtenerUbicacionesTecnicos.java`: Modelos de geoposicionamiento.

## 5. Módulo de Reportes y Métricas (Panel de Administrador)
* **¿Qué hace?**: Ofrece un resumen estadístico y gráfico del estado de los incidentes para la toma de decisiones del Jefe de TI (Administrador).
* **Archivos clave**:
  * `AdminReportsFragment.java` y `fragment_admin_reports.xml`: Muestra gráficos por estado (*Abierto, En proceso, Resuelto*), por prioridad (*Alta, Media, Baja*), por sucursal y el tiempo promedio de resolución (ej. 4.2 días).

## 6. Módulo de Colaboradores / Usuarios
* **¿Qué hace?**: Permite la gestión y registro de usuarios del sistema y técnicos de soporte.
* **Archivos clave**:
  * `AddCollaboratorFragment.java` y `fragment_add_collaborator.xml`: Formulario para registrar colaboradores.
  * `CollaboratorCardRecyclerViewAdapter.java` y `usat_collaborator_card.xml`: Listado de colaboradores.
  * `ProfileFragment.java`: Vista del perfil de usuario.
  * `RequInsertarUsuario.java`, `RptaObtenerUsuarios.java`: Modelos de datos.

## 7. Utilidades y Configuración de Red
* **`Interface/JCPyAnyApi.java`**: Contiene todas las anotaciones Retrofit (`@GET`, `@POST`, `@PUT`, `@DELETE`) que conectan la app con los endpoints de Flask.
* **`SlaHelper.java`**: Utilidad para calcular tiempos de respuesta y cumplimiento de SLAs en la atención de incidentes.
* **`nav_graph.xml`**: Grafo de navegación centralizado que conecta todas las pantallas anteriores mediante el Navigation Component de Android.
