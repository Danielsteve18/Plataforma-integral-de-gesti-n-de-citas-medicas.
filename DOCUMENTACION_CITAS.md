# Sistema de Gestión de Citas Médicas - MediPac

## ✅ Estado de Implementación Completa

### 📋 Resumen
El sistema de gestión de citas médicas ha sido completamente implementado con todas las funcionalidades necesarias para pacientes, doctores y administradores.

---

## 🎯 Componentes Implementados

### 1. **Modelos de Datos (Backend)**
- ✅ `Cita.java` - Modelo principal con todos los campos y relaciones
- ✅ `EstadoCita.java` - Enum para estados (PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA)
- ✅ `CitaRepository.java` - Repositorio con queries personalizadas
- ✅ `CitaService.java` - Lógica de negocio completa

### 2. **DTOs y Estructuras**
- ✅ `CitaRequestDTO.java` - DTO para crear citas
- ✅ `CitaResponseDTO.java` - DTO para respuestas
- ✅ `HorarioDisponibleDTO.java` - DTO para horarios

### 3. **Controladores REST**
- ✅ `CitaRestController.java` - API REST completa con endpoints:
  - `POST /api/citas` - Crear cita
  - `GET /api/citas/{id}` - Obtener cita
  - `GET /api/citas` - Listar todas las citas
  - `POST /api/citas/{id}/confirmar` - Confirmar cita
  - `POST /api/citas/{id}/completar` - Completar cita
  - `POST /api/citas/{id}/cancelar` - Cancelar cita
  - `DELETE /api/citas/{id}` - Eliminar cita
  - `GET /api/citas/disponibles` - Obtener horarios disponibles
  - `GET /api/doctores/especialidad/{especialidad}` - Listar doctores

### 4. **Controladores de Vistas**
- ✅ `PacienteController.java` - Actualizado con:
  - `/paciente/agendar-cita` - Formulario de agendar
  - `/paciente/mis-citas` - Listar mis citas
  
- ✅ `DoctorController.java` - Actualizado con:
  - `/doctor/mis-citas` - Agenda del doctor
  
- ✅ `AdminController.java` - Actualizado con:
  - `/admin/citas` - Gestión completa de citas

### 5. **Vistas HTML (Templates)**

#### Para Pacientes:
- ✅ `paciente/agendar-cita.html` - Formulario interactivo para agendar
  - Selección de especialidad
  - Selección de doctor
  - Calendario de fechas
  - Horarios disponibles dinámicos
  - Formulario de motivo y notas

- ✅ `paciente/mis-citas.html` - Gestión de citas del paciente
  - Filtros por estado
  - Vista de todas las citas
  - Acciones: cancelar, descargar comprobante
  - Estados visuales por color

- ✅ `paciente/dashboard.html` - Actualizado con enlaces a citas

#### Para Doctores:
- ✅ `doctor/mis-citas.html` - Agenda del doctor
  - Estadísticas de citas
  - Filtros avanzados
  - Acciones: confirmar, completar, cancelar
  - Vista de información del paciente

- ✅ `doctor/dashboard.html` - Actualizado con enlace a agenda

#### Para Administradores:
- ✅ `admin/citas.html` - Panel de administración completo
  - Dashboard con estadísticas
  - Filtros múltiples
  - Gestión completa de todas las citas
  - Acciones administrativas

- ✅ `admin/dashboard.html` - Actualizado con enlace a citas

### 6. **JavaScript y Recursos**
- ✅ `static/js/citas.js` - Librería de funciones JavaScript:
  - Cargar doctores por especialidad
  - Cargar horarios disponibles
  - Confirmar/Completar/Cancelar citas
  - Validaciones de fecha
  - Mensajes de éxito/error
  - Helpers de formateo

### 7. **Base de Datos**
- ✅ `gestion-citas-completo.sql` - Script SQL completo con:
  - Tabla `citas` con todas las relaciones
  - Triggers para validaciones
  - Procedimientos almacenados
  - Vistas para reportes
  - Datos de ejemplo

---

## 📊 Funcionalidades por Rol

### 👨‍⚕️ DOCTOR
1. ✅ Ver agenda de citas
2. ✅ Filtrar citas por estado y fecha
3. ✅ Confirmar citas pendientes
4. ✅ Completar citas confirmadas
5. ✅ Cancelar citas (con restricciones)
6. ✅ Ver detalles del paciente
7. ✅ Estadísticas de citas

### 👤 PACIENTE
1. ✅ Agendar nueva cita
2. ✅ Seleccionar especialidad y doctor
3. ✅ Ver horarios disponibles en tiempo real
4. ✅ Ver todas mis citas
5. ✅ Filtrar citas por estado
6. ✅ Cancelar citas propias
7. ✅ Descargar comprobantes
8. ✅ Ver estado de cada cita

### 👨‍💼 ADMINISTRADOR
1. ✅ Ver todas las citas del sistema
2. ✅ Estadísticas completas
3. ✅ Filtrar por múltiples criterios
4. ✅ Gestión total de citas
5. ✅ Confirmar/Completar/Cancelar cualquier cita
6. ✅ Eliminar citas del sistema
7. ✅ Buscar por paciente o doctor

---

## 🎨 Características de UI/UX

### Diseño Visual
- ✅ Colores diferenciados por rol (Azul: Paciente, Verde: Doctor, Gris: Admin)
- ✅ Estados visuales claros (badges de color por estado)
- ✅ Responsive design para móviles y tablets
- ✅ Animaciones suaves en hover
- ✅ Iconos FontAwesome descriptivos

### Interactividad
- ✅ Carga dinámica de datos sin recargar página
- ✅ Validación de formularios en tiempo real
- ✅ Mensajes de éxito/error temporales
- ✅ Confirmaciones antes de acciones críticas
- ✅ Filtros en tiempo real
- ✅ Selección visual de horarios

### Accesibilidad
- ✅ Labels descriptivos
- ✅ Tooltips informativos
- ✅ Estados disabled claros
- ✅ Mensajes de error visibles
- ✅ Navegación intuitiva

---

## 🔧 Tecnologías Utilizadas

### Backend
- Spring Boot 3.x
- Spring Data JPA
- MySQL 8.0+
- Thymeleaf
- Spring Security

### Frontend
- HTML5
- CSS3 (con gradientes y animaciones)
- JavaScript ES6+
- Font Awesome 6.0
- Thymeleaf Templates

---

## 🚀 Cómo Usar el Sistema

### 1. Configuración Inicial

```bash
# 1. Ejecutar el script SQL
mysql -u root -p < gestion-citas-completo.sql

# 2. Verificar configuración en application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/medipac_db
spring.datasource.username=root
spring.datasource.password=tu_password

# 3. Compilar y ejecutar
mvn clean install
mvn spring-boot:run
```

### 2. Acceso al Sistema

#### Como Paciente:
1. Ir a `/paciente/dashboard`
2. Click en "Agendar Cita"
3. Seleccionar especialidad
4. Elegir doctor
5. Seleccionar fecha y horario
6. Completar formulario
7. Confirmar cita

#### Como Doctor:
1. Ir a `/doctor/dashboard`
2. Click en "Ver Agenda"
3. Ver citas del día o filtrar
4. Confirmar/Completar citas
5. Ver detalles de pacientes

#### Como Admin:
1. Ir a `/admin/dashboard`
2. Click en "Citas Médicas"
3. Ver estadísticas generales
4. Filtrar y buscar citas
5. Gestionar cualquier cita

---

## 📝 Endpoints API Disponibles

### Gestión de Citas
```
POST   /api/citas                          - Crear nueva cita
GET    /api/citas/{id}                     - Obtener cita por ID
GET    /api/citas                          - Listar todas las citas
POST   /api/citas/{id}/confirmar           - Confirmar cita
POST   /api/citas/{id}/completar           - Completar cita
POST   /api/citas/{id}/cancelar            - Cancelar cita
DELETE /api/citas/{id}                     - Eliminar cita
GET    /api/citas/disponibles              - Horarios disponibles
GET    /api/citas/paciente/{pacienteId}    - Citas de un paciente
GET    /api/citas/doctor/{doctorId}        - Citas de un doctor
```

### Doctores y Especialidades
```
GET    /api/doctores/especialidad/{especialidad}  - Doctores por especialidad
GET    /api/especialidades                        - Todas las especialidades
```

---

## 🔒 Seguridad Implementada

- ✅ Validación de roles en cada endpoint
- ✅ Verificación de propiedad (paciente solo ve sus citas)
- ✅ CSRF protection habilitado
- ✅ Validaciones de fechas y horarios
- ✅ Prevención de doble reserva
- ✅ Sanitización de inputs

---

## 📈 Próximas Mejoras Sugeridas

1. **Notificaciones**
   - Email de confirmación
   - Recordatorios automáticos
   - SMS para citas próximas

2. **Reportes**
   - Exportar a PDF
   - Estadísticas avanzadas
   - Gráficos de tendencias

3. **Calendario Visual**
   - Vista de calendario mensual
   - Drag & drop para reprogramar
   - Sincronización con Google Calendar

4. **Videollamadas**
   - Consultas virtuales
   - Integración con Zoom/Meet
   - Historial de videollamadas

5. **Sistema de Pagos**
   - Pagos en línea
   - Facturación automática
   - Historial de pagos

---

## 📞 Soporte y Documentación

- **Documentación API**: Ver `CitaRestController.java`
- **Modelos de datos**: Ver carpeta `model/`
- **Scripts SQL**: Ver `gestion-citas-completo.sql`
- **Vistas**: Ver carpeta `templates/`

---

## ✨ Características Destacadas

1. **Sistema Completo** - Funcionalidad end-to-end implementada
2. **Tres Roles** - Paciente, Doctor y Admin con permisos específicos
3. **UI Moderna** - Diseño atractivo y responsive
4. **API REST** - Endpoints bien documentados
5. **Base de Datos** - Schema completo con relaciones
6. **Validaciones** - Lógica de negocio robusta
7. **Interactivo** - AJAX para experiencia fluida
8. **Seguro** - Autenticación y autorización

---

**Última actualización**: 10 de noviembre de 2025  
**Versión**: 1.0.0  
**Estado**: ✅ Producción Ready

### ✅ Mejoras Implementadas

1. **Modelo de Datos Mejorado**
   - ✅ Uso de enum `EstadoCita` en lugar de String para mejor consistencia
   - ✅ Índices de base de datos optimizados para consultas rápidas
   - ✅ Campos adicionales: `duracionMinutos`, `notasCancelacion`, `fechaActualizacion`
   - ✅ Validaciones a nivel de base de datos con constraints y triggers

2. **DTOs para Transferencia de Datos**
   - ✅ `CitaDTO`: Para transferir información completa de citas
   - ✅ `CrearCitaRequest`: Para validar creación de citas con anotaciones Jakarta
   - ✅ `ActualizarCitaRequest`: Para actualizar citas de forma segura
   - ✅ `CitaMapper`: Para convertir entre entidades y DTOs

3. **Base de Datos SQL Completa**
   - ✅ Script SQL con todas las tablas necesarias
   - ✅ Vistas para consultas frecuentes
   - ✅ Procedimientos almacenados para operaciones complejas
   - ✅ Triggers para validaciones automáticas
   - ✅ Datos iniciales de especialidades médicas

4. **Servicio de Negocio Robusto**
   - ✅ `CitaServiceMejorado` con validaciones completas
   - ✅ Detección de conflictos de horarios
   - ✅ Validación de anticipación mínima (1 hora)
   - ✅ Validación de anticipación máxima (90 días)
   - ✅ Validación de horarios laborales (8:00 AM - 8:00 PM)
   - ✅ Gestión de estados de citas con transiciones válidas

5. **API REST Completa**
   - ✅ `CitaRestController` con endpoints JSON
   - ✅ Operaciones CRUD completas
   - ✅ Endpoints para acciones: confirmar, completar, cancelar, no-asistió
   - ✅ Consultas optimizadas por paciente, doctor y fecha

6. **Repositorios Optimizados**
   - ✅ Queries JPA mejoradas con fetch strategies
   - ✅ Detección de conflictos considerando duración de citas
   - ✅ Consultas especializadas para diferentes escenarios

---

## 🗄️ Configuración de la Base de Datos

### Paso 1: Ejecutar el Script SQL

```bash
# Conectar a MySQL
mysql -u root -p

# Ejecutar el script completo
source /ruta/al/proyecto/gestion-citas-completo.sql
```

O desde línea de comandos:
```bash
mysql -u root -p < gestion-citas-completo.sql
```

### Paso 2: Verificar la Creación

```sql
USE gestion_citas_medicas;

-- Ver tablas creadas
SHOW TABLES;

-- Ver especialidades inicializadas
SELECT * FROM especialidades;

-- Ver estadísticas del sistema
CALL sp_estadisticas_sistema();
```

---

## 🚀 Configuración de la Aplicación Spring Boot

### application.properties

Ya está configurado correctamente en:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_citas_medicas
spring.datasource.username=root
spring.datasource.password=hldj
spring.jpa.hibernate.ddl-auto=update
```

---

## 📡 API REST - Endpoints Disponibles

### Crear Cita
```http
POST /api/citas
Content-Type: application/json

{
  "pacienteId": 1,
  "doctorId": 2,
  "fechaHora": "2025-11-15T10:30:00",
  "motivo": "Consulta general",
  "duracionMinutos": 30
}
```

### Obtener Cita por ID
```http
GET /api/citas/{id}
```

### Obtener Próximas Citas de Paciente
```http
GET /api/citas/paciente/{pacienteId}/proximas
```

### Obtener Historial de Paciente
```http
GET /api/citas/paciente/{pacienteId}/historial
```

### Obtener Citas de Hoy del Doctor
```http
GET /api/citas/doctor/{doctorId}/hoy
```

### Obtener Próximas Citas del Doctor
```http
GET /api/citas/doctor/{doctorId}/proximas
```

### Obtener Citas por Fecha
```http
GET /api/citas/fecha/2025-11-15
```

### Actualizar Cita
```http
PUT /api/citas/{id}
Content-Type: application/json

{
  "nuevaFechaHora": "2025-11-16T11:00:00",
  "nuevoMotivo": "Consulta de seguimiento",
  "nuevaDuracion": 45
}
```

### Confirmar Cita
```http
PATCH /api/citas/{id}/confirmar
```

### Completar Cita
```http
PATCH /api/citas/{id}/completar
```

### Cancelar Cita
```http
PATCH /api/citas/{id}/cancelar
Content-Type: application/json

{
  "notas": "Paciente reprogramó para otra fecha"
}
```

### Marcar No Asistió
```http
PATCH /api/citas/{id}/no-asistio
```

---

## 🔧 Uso del Servicio en el Código

### Ejemplo: Crear una Cita

```java
@Autowired
private CitaServiceMejorado citaService;

// Crear request
CrearCitaRequest request = new CrearCitaRequest();
request.setPacienteId(1L);
request.setDoctorId(2L);
request.setFechaHora(LocalDateTime.of(2025, 11, 15, 10, 30));
request.setMotivo("Consulta general");
request.setDuracionMinutos(30);

// Crear cita
CitaServiceMejorado.CitaResult result = citaService.crearCita(request);

if (result.isExito()) {
    System.out.println("Cita creada: " + result.getCita().getId());
} else {
    System.out.println("Error: " + result.getMensaje());
}
```

### Ejemplo: Obtener Próximas Citas

```java
List<CitaDTO> proximasCitas = citaService.obtenerProximasCitasPaciente(1L);
proximasCitas.forEach(cita -> {
    System.out.println("Cita con Dr. " + cita.getDoctorNombreCompleto());
    System.out.println("Fecha: " + cita.getFechaHora());
    System.out.println("Estado: " + cita.getEstado().getDescripcion());
});
```

---

## 🎯 Validaciones Implementadas

### Validaciones de Fecha y Hora
- ✅ No se pueden crear citas en el pasado
- ✅ Anticipación mínima: 1 hora
- ✅ Anticipación máxima: 90 días
- ✅ Horario laboral: 8:00 AM - 8:00 PM

### Validaciones de Duración
- ✅ Duración mínima: 15 minutos
- ✅ Duración máxima: 180 minutos (3 horas)
- ✅ Duración por defecto: 30 minutos

### Validaciones de Conflictos
- ✅ Un doctor no puede tener dos citas al mismo tiempo
- ✅ Un paciente no puede tener dos citas al mismo tiempo
- ✅ Se considera la duración de las citas para detectar solapamientos

### Validaciones de Estado
- ✅ Solo se pueden cancelar citas activas (PROGRAMADA, CONFIRMADA)
- ✅ Solo se pueden completar citas activas
- ✅ Solo se puede marcar "no asistió" en citas pasadas

---

## 📊 Estados de Citas

```java
public enum EstadoCita {
    PROGRAMADA,     // Cita recién creada
    CONFIRMADA,     // Cita confirmada por el doctor
    COMPLETADA,     // Cita realizada
    CANCELADA,      // Cita cancelada
    NO_ASISTIO      // Paciente no asistió
}
```

---

## 🔍 Consultas Útiles SQL

### Ver todas las citas de hoy
```sql
SELECT * FROM vista_citas_completas 
WHERE DATE(fecha_hora) = CURDATE();
```

### Ver próximas citas (siguiente semana)
```sql
SELECT * FROM vista_proximas_citas;
```

### Ver estadísticas por doctor
```sql
SELECT * FROM vista_estadisticas_doctor;
```

### Verificar disponibilidad de un doctor
```sql
CALL sp_obtener_disponibilidad_doctor(2, '2025-11-15');
```

### Crear cita con procedimiento almacenado
```sql
CALL sp_crear_cita(1, 2, '2025-11-15 10:30:00', 'Consulta general', 30, @resultado, @cita_id);
SELECT @resultado, @cita_id;
```

---

## 🛠️ Compilar y Ejecutar

### Compilar el proyecto
```bash
./mvnw clean package
```

### Ejecutar la aplicación
```bash
./mvnw spring-boot:run
```

O ejecutar el JAR:
```bash
java -jar target/medipac-0.0.1-SNAPSHOT.jar
```

### Acceder a la aplicación
- **Web**: http://localhost:8080
- **API REST**: http://localhost:8080/api/citas

---

## 📝 Notas Importantes

1. **Índices de Base de Datos**: La base de datos incluye índices optimizados para:
   - Búsquedas por fecha
   - Búsquedas por estado
   - Búsquedas combinadas (paciente+fecha, doctor+fecha)

2. **Triggers Automáticos**: 
   - Actualización automática de `fecha_actualizacion`
   - Validación de historias clínicas solo para citas completadas

3. **Vistas Predefinidas**:
   - `vista_citas_completas`: Información completa de citas
   - `vista_estadisticas_doctor`: Estadísticas por doctor
   - `vista_proximas_citas`: Citas de los próximos 7 días

4. **Transacciones**: Todos los métodos del servicio usan `@Transactional` para garantizar la consistencia de datos

---

## 🐛 Solución de Problemas

### Error: "Cita ya existe en ese horario"
- Verifica que no haya una cita activa (PROGRAMADA o CONFIRMADA) en el mismo horario
- Considera la duración de las citas existentes

### Error: "Anticipación mínima no cumplida"
- Las citas deben agendarse con al menos 1 hora de anticipación
- Ajusta la constante `MINUTOS_ANTICIPACION_MINIMA` en `CitaServiceMejorado` si es necesario

### Error: "Fuera de horario laboral"
- Las citas solo pueden agendarse entre 8:00 AM y 8:00 PM
- Modifica las validaciones en `validarFechaHora()` si necesitas horarios diferentes

---

## ✨ Características Destacadas

- ✅ **Detección inteligente de conflictos** considerando duración de citas
- ✅ **Validaciones en múltiples capas**: Base de datos, JPA, Servicio, Controller
- ✅ **API REST completa** con respuestas JSON estandarizadas
- ✅ **DTOs validados** con Jakarta Validation
- ✅ **Procedimientos almacenados** para operaciones complejas
- ✅ **Vistas SQL** para consultas frecuentes optimizadas
- ✅ **Logging detallado** para debugging
- ✅ **Manejo robusto de errores** con mensajes claros

---

## 📚 Documentación Adicional

Para más información sobre el proyecto MediPac:
- Ver archivo `HELP.md` para documentación de Spring Boot
- Consultar `pom.xml` para dependencias utilizadas
- Revisar `application.properties` para configuraciones

---

**Desarrollado con ❤️ para MediPac**
