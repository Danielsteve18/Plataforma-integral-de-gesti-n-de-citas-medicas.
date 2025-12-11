# 📋 INFORMACIÓN COMPLETA DE LA APLICACIÓN MEDIPAC

## 🎯 INFORMACIÓN GENERAL

**Nombre de la Aplicación:** MediPac  
**Versión:** 1.0.0  
**Tipo:** Sistema de Gestión de Citas Médicas  
**Tecnología:** Spring Boot 3.5.6 con Java 21  
**Base de Datos:** MySQL 8.0+  
**Puerto:** 8080  
**URL Base:** http://localhost:8080

---

## 📖 DESCRIPCIÓN DEL SISTEMA

MediPac es un sistema completo de gestión de citas médicas diseñado para facilitar la interacción entre pacientes, doctores y administradores. El sistema permite agendar, gestionar y realizar seguimiento de citas médicas de manera eficiente y organizada.

### Propósito Principal
- Gestionar citas médicas de forma centralizada
- Facilitar la comunicación entre pacientes y doctores
- Administrar usuarios, doctores y especialidades médicas
- Mantener historiales clínicos y prescripciones
- Proporcionar estadísticas y reportes del sistema

---

## 🏗️ ARQUITECTURA Y TECNOLOGÍAS

### Backend
- **Framework:** Spring Boot 3.5.6
- **Lenguaje:** Java 21
- **ORM:** Spring Data JPA / Hibernate
- **Seguridad:** Spring Security 6
- **Template Engine:** Thymeleaf
- **Validación:** Jakarta Validation
- **Librerías:**
  - Lombok (reducción de código boilerplate)
  - MySQL Connector 8.0.33
  - Spring Boot Actuator (monitoreo)
  - Spring Boot DevTools (desarrollo)

### Frontend
- **HTML5** con Thymeleaf Templates
- **CSS3** con diseño responsive
- **JavaScript ES6+** para interactividad
- **Font Awesome 6.0** para iconos
- **Diseño:** Responsive (móviles, tablets, desktop)

### Base de Datos
- **Motor:** MySQL 8.0+
- **Nombre de BD:** `gestion_citas_medicas`
- **Charset:** utf8mb4
- **Collation:** utf8mb4_unicode_ci
- **Pool de Conexiones:** HikariCP

### Configuración de Base de Datos
```properties
URL: jdbc:mysql://localhost:3306/gestion_citas_medicas
Usuario: root
Contraseña: daniel
Pool máximo: 20 conexiones
Pool mínimo: 5 conexiones
```

---

## 👥 ROLES Y USUARIOS DEL SISTEMA

### 1. PACIENTE
**Descripción:** Usuarios que pueden agendar y gestionar sus citas médicas.

**Credenciales de Prueba:**
- Usuario: `paciente1`
- Contraseña: `paciente123`
- Dashboard: http://localhost:8080/paciente/dashboard

**Funcionalidades:**
- ✅ Agendar nuevas citas médicas
- ✅ Seleccionar especialidad médica
- ✅ Elegir doctor disponible
- ✅ Ver horarios disponibles en tiempo real
- ✅ Ver todas sus citas (programadas, confirmadas, completadas, canceladas)
- ✅ Filtrar citas por estado
- ✅ Cancelar citas propias
- ✅ Ver historial médico
- ✅ Actualizar información personal
- ✅ Buscar doctores
- ✅ Ver perfil de doctores
- ✅ Ver notificaciones

**Vistas Disponibles:**
- `/paciente/dashboard` - Panel principal
- `/paciente/agendar-cita` - Formulario para agendar citas
- `/paciente/mis-citas` - Lista de todas las citas del paciente
- `/paciente/historial` - Historial médico completo
- `/paciente/perfil` - Perfil personal
- `/paciente/buscar-doctores` - Búsqueda de doctores
- `/paciente/perfil-doctor` - Ver perfil de un doctor
- `/paciente/notificaciones` - Notificaciones del paciente

### 2. DOCTOR
**Descripción:** Profesionales médicos que atienden pacientes y gestionan su agenda.

**Credenciales de Prueba:**
- Usuario: `doctor2`
- Contraseña: `doctor123`
- Dashboard: http://localhost:8080/doctor/dashboard

**Funcionalidades:**
- ✅ Ver y gestionar agenda de citas
- ✅ Confirmar citas pendientes
- ✅ Completar citas confirmadas
- ✅ Cancelar citas (con restricciones)
- ✅ Marcar pacientes que no asistieron
- ✅ Ver lista de pacientes
- ✅ Gestionar historias clínicas
- ✅ Crear y editar prescripciones
- ✅ Editar perfil profesional
- ✅ Ver estadísticas de citas
- ✅ Ver calendario de citas
- ✅ Ver historias clínicas de pacientes

**Vistas Disponibles:**
- `/doctor/dashboard` - Panel principal del doctor
- `/doctor/mis-citas` - Agenda completa de citas
- `/doctor/mis-pacientes` - Lista de pacientes atendidos
- `/doctor/historias` - Historias clínicas gestionadas
- `/doctor/historias-paciente` - Historial de un paciente específico
- `/doctor/prescripciones` - Gestión de prescripciones
- `/doctor/editar-perfil` - Editar información profesional
- `/doctor/calendario` - Vista de calendario
- `/doctor/notificaciones` - Notificaciones del doctor

### 3. ADMINISTRADOR
**Descripción:** Usuarios con acceso completo al sistema para gestión y administración.

**Credenciales de Prueba:**
- Usuario: `admin`
- Contraseña: `admin123`
- Dashboard: http://localhost:8080/admin/dashboard

**Funcionalidades:**
- ✅ Gestión completa de usuarios (crear, editar, eliminar)
- ✅ Cambiar roles de usuarios (ADMIN, DOCTOR, PACIENTE)
- ✅ Activar/desactivar usuarios
- ✅ Gestión de doctores y especialidades
- ✅ Asignar especialidades a doctores
- ✅ Ver todas las citas del sistema
- ✅ Confirmar/Completar/Cancelar cualquier cita
- ✅ Eliminar citas del sistema
- ✅ Ver estadísticas del sistema
- ✅ Gestión de administradores
- ✅ Búsqueda avanzada de usuarios y citas
- ✅ Filtros múltiples para citas

**Vistas Disponibles:**
- `/admin/dashboard` - Panel principal de administración
- `/admin/usuarios` - Gestión de usuarios
- `/admin/doctores` - Gestión de doctores y especialidades
- `/admin/citas` - Gestión completa de citas
- `/admin/administradores` - Gestión de administradores

---

## 🗄️ ESTRUCTURA DE BASE DE DATOS

### Tablas Principales

#### 1. usuarios
Almacena información básica de todos los usuarios del sistema.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `username` (VARCHAR(100), UNIQUE, NOT NULL)
- `password` (VARCHAR(255), NOT NULL) - Hasheado con BCrypt
- `email` (VARCHAR(150), UNIQUE, NOT NULL)
- `rol` (ENUM: 'PACIENTE', 'DOCTOR', 'ADMINISTRADOR', NOT NULL)
- `activo` (BOOLEAN, DEFAULT TRUE)
- `fecha_registro` (DATETIME, DEFAULT CURRENT_TIMESTAMP)
- `ultimo_acceso` (DATETIME)

**Índices:**
- idx_username
- idx_email
- idx_rol
- idx_activo

#### 2. especialidades
Catálogo de especialidades médicas disponibles.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `nombre` (VARCHAR(100), UNIQUE, NOT NULL)
- `descripcion` (TEXT)
- `fecha_creacion` (DATETIME, DEFAULT CURRENT_TIMESTAMP)

**Especialidades Disponibles:**
1. Cardiología
2. Dermatología
3. Endocrinología
4. Gastroenterología
5. Ginecología
6. Medicina General
7. Neurología
8. Oftalmología
9. Oncología
10. Ortopedia
11. Otorrinolaringología
12. Pediatría
13. Psiquiatría
14. Psicología
15. Neumología
16. Urología
17. Traumatología
18. Radiología
19. Anestesiología
20. Medicina Interna

#### 3. pacientes
Información específica de los pacientes.
- `usuario_id` (BIGINT, PK, FK -> usuarios.id)
- `nombre` (VARCHAR(100), NOT NULL)
- `apellido` (VARCHAR(100), NOT NULL)
- `fecha_nacimiento` (DATE)
- `genero` (VARCHAR(50))
- `telefono` (VARCHAR(50))
- `direccion` (VARCHAR(255))

**Relaciones:**
- OneToOne con `usuarios`
- OneToMany con `citas`

#### 4. doctores
Información específica de los doctores.
- `usuario_id` (BIGINT, PK, FK -> usuarios.id)
- `nombre` (VARCHAR(100), NOT NULL)
- `apellido` (VARCHAR(100), NOT NULL)
- `numero_licencia` (VARCHAR(100), UNIQUE, NOT NULL)
- `telefono` (VARCHAR(50))
- `consultorio` (VARCHAR(100))

**Relaciones:**
- OneToOne con `usuarios`
- ManyToMany con `especialidades` (tabla intermedia: `doctores_especialidades`)
- OneToMany con `citas`
- OneToMany con `horarios`

#### 5. administradores
Información específica de los administradores.
- `usuario_id` (BIGINT, PK, FK -> usuarios.id)
- `nombre` (VARCHAR(100), NOT NULL)
- `apellido` (VARCHAR(100), NOT NULL)
- `departamento` (VARCHAR(100))

**Relaciones:**
- OneToOne con `usuarios`

#### 6. doctores_especialidades
Tabla intermedia para relación muchos a muchos entre doctores y especialidades.
- `doctor_id` (BIGINT, PK, FK -> doctores.usuario_id)
- `especialidad_id` (BIGINT, PK, FK -> especialidades.id)
- `fecha_asignacion` (DATETIME, DEFAULT CURRENT_TIMESTAMP)

#### 7. horarios
Horarios de disponibilidad de los doctores.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `doctor_id` (BIGINT, FK -> doctores.usuario_id)
- `fecha` (DATE, NOT NULL)
- `hora_inicio` (TIME, NOT NULL)
- `hora_fin` (TIME, NOT NULL)
- `disponible` (BOOLEAN, DEFAULT TRUE)
- `fecha_creacion` (DATETIME, DEFAULT CURRENT_TIMESTAMP)

#### 8. citas
Gestión completa de citas médicas.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `paciente_id` (BIGINT, FK -> pacientes.usuario_id, NOT NULL)
- `doctor_id` (BIGINT, FK -> doctores.usuario_id, NOT NULL)
- `fecha_hora` (DATETIME, NOT NULL)
- `duracion_minutos` (INT, DEFAULT 30)
- `motivo` (VARCHAR(500))
- `estado` (ENUM: 'PROGRAMADA', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO', DEFAULT 'PROGRAMADA')
- `notas_cancelacion` (VARCHAR(500))
- `fecha_creacion` (DATETIME, DEFAULT CURRENT_TIMESTAMP)
- `fecha_actualizacion` (DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

**Constraints:**
- UNIQUE KEY unique_doctor_fecha_hora (doctor_id, fecha_hora) - Evita citas duplicadas
- CHECK duracion_minutos >= 15 AND duracion_minutos <= 180

**Índices:**
- idx_fecha_hora
- idx_estado
- idx_paciente_fecha
- idx_doctor_fecha
- idx_fecha_creacion

**Relaciones:**
- ManyToOne con `pacientes`
- ManyToOne con `doctores`
- OneToOne con `historias_clinicas`

#### 9. historias_clinicas
Registro de historias clínicas asociadas a citas completadas.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `cita_id` (BIGINT, FK -> citas.id, UNIQUE, NOT NULL)
- `diagnostico` (TEXT, NOT NULL)
- `prescripcion` (TEXT)
- `notas` (TEXT)
- `fecha_creacion` (DATETIME, DEFAULT CURRENT_TIMESTAMP)
- `fecha_actualizacion` (DATETIME, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

**Relaciones:**
- OneToOne con `citas`

#### 10. prescripciones (si existe)
Prescripciones médicas.
- Relacionadas con historias clínicas o citas

#### 11. doctores_pacientes_favoritos (si existe)
Relación de doctores favoritos por paciente.
- Permite a pacientes marcar doctores como favoritos

---

## 🔄 FLUJOS DE TRABAJO PRINCIPALES

### Flujo de Agendamiento de Citas (Paciente)

1. **Acceso al Sistema**
   - Login en http://localhost:8080/login
   - Credenciales: `paciente1` / `paciente123`
   - Redirección a `/paciente/dashboard`

2. **Iniciar Agendamiento**
   - Click en "Agendar Cita" desde el dashboard
   - Acceso a `/paciente/agendar-cita`

3. **Selección de Especialidad**
   - Seleccionar especialidad médica del dropdown
   - Opciones: Medicina General, Pediatría, Cardiología, Dermatología, Traumatología, Ginecología, Oftalmología, Psiquiatría, Neurología, Odontología

4. **Selección de Doctor (Dinámica)**
   - Al seleccionar especialidad, se cargan automáticamente los doctores disponibles
   - Endpoint: `GET /api/doctores/especialidad/{nombre}`
   - Muestra: nombre completo, número de licencia, teléfono
   - Solo doctores activos con esa especialidad

5. **Selección de Fecha**
   - Input tipo `date` con validación `min=today`
   - Solo fechas futuras permitidas

6. **Selección de Horario (Tiempo Real)**
   - Endpoint: `GET /api/citas/disponibles?doctorId={id}&fecha={fecha}`
   - Horarios estándar:
     - Mañana: 08:00, 09:00, 10:00, 11:00, 12:00
     - Tarde: 14:00, 15:00, 16:00, 17:00, 18:00
   - Filtra automáticamente horarios ya ocupados
   - Muestra solo slots disponibles
   - Selección visual con botones interactivos

7. **Completar Formulario**
   - Motivo de consulta (requerido)
   - Notas adicionales (opcional)

8. **Confirmación y Creación**
   - Endpoint: `POST /api/citas`
   - Validaciones automáticas:
     - Doctor existe y está activo
     - Fecha/hora no está ocupada
     - Fecha es futura
     - Todos los campos requeridos completos
   - Resultado: Cita creada con estado `PROGRAMADA`

### Flujo de Gestión de Citas (Doctor)

1. **Acceso a la Agenda**
   - Login como doctor: `doctor2` / `doctor123`
   - Dashboard: `/doctor/dashboard`
   - Click en "Mi Agenda" o "Ver Agenda"

2. **Vista de Agenda**
   - Vista: `/doctor/mis-citas`
   - Endpoints de datos:
     - `GET /api/citas/doctor/{doctorId}/hoy` - Citas del día
     - `GET /api/citas/doctor/{doctorId}/proximas` - Próximas citas

3. **Filtros Disponibles**
   - Por estado (PROGRAMADA, CONFIRMADA, COMPLETADA, CANCELADA)
   - Por rango de fechas
   - Vista de hoy / Vista semanal

4. **Información Mostrada por Cita**
   - Hora de la cita
   - Datos del paciente (nombre, edad)
   - Motivo de la consulta
   - Estado actual
   - Tiempo desde/hasta la cita

5. **Acciones Disponibles**

   **A) Confirmar Cita**
   - Endpoint: `PATCH /api/citas/{id}/confirmar`
   - Desde estado: PROGRAMADA
   - A estado: CONFIRMADA
   - Efecto: Notifica al paciente que su cita está confirmada

   **B) Completar Cita**
   - Endpoint: `PATCH /api/citas/{id}/completar`
   - Desde estado: CONFIRMADA
   - A estado: COMPLETADA
   - Acción posterior: Opción de crear historia clínica

   **C) Marcar No Asistió**
   - Endpoint: `PATCH /api/citas/{id}/no-asistio`
   - Desde estado: CONFIRMADA
   - A estado: NO_ASISTIO
   - Uso: Cuando el paciente no llega a la cita

   **D) Cancelar Cita**
   - Endpoint: `PATCH /api/citas/{id}/cancelar`
   - Disponible para: PROGRAMADA o CONFIRMADA
   - Requiere: Motivo de cancelación
   - Efecto: Notifica al paciente

### Flujo de Administración (Admin)

1. **Gestión de Usuarios**
   - Dashboard: `/admin/dashboard`
   - Sección: "Usuarios"
   - Funcionalidades:
     - Crear nuevos usuarios
     - Editar información de usuarios
     - Cambiar roles (PACIENTE, DOCTOR, ADMINISTRADOR)
     - Activar/desactivar usuarios
     - Eliminar usuarios

2. **Gestión de Doctores**
   - Vista: `/admin/doctores`
   - Layout: Cards en grid 3x3 (responsive)
   - Por cada doctor muestra:
     - Nombre completo
     - Número de licencia
     - Especialidades actuales (badges)
     - Selector para agregar nueva especialidad
   - Endpoint: `POST /admin/agregar-especialidad-doctor`
   - Proceso para crear doctores:
     1. Crear usuario normal
     2. Cambiar rol a DOCTOR
     3. Automático: Se asigna especialidad "Medicina General"
     4. Agregar especialidades adicionales desde `/admin/doctores`

3. **Gestión de Citas (Vista Global)**
   - Vista: `/admin/citas`
   - Capacidades:
     - Ver todas las citas del sistema
     - Filtrar por: estado, doctor, paciente, fecha
     - Estadísticas generales
     - Modificar/cancelar cualquier cita
     - Eliminar citas del sistema
     - Buscar por paciente o doctor

---

## 📊 ESTADOS DE LAS CITAS

### Diagrama de Transición de Estados

```
┌──────────────┐
│  PROGRAMADA  │ ← Estado inicial al crear cita
└──────┬───────┘
       │
       ├─── Paciente cancela ──→ CANCELADA
       │
       ├─── Doctor cancela ───→ CANCELADA
       │
       └─── Doctor confirma ──→ ┌────────────┐
                                 │ CONFIRMADA │
                                 └──────┬─────┘
                                        │
                                        ├─── Doctor completa ──→ COMPLETADA
                                        │
                                        ├─── Cancelación ──────→ CANCELADA
                                        │
                                        └─── No asistió ───────→ NO_ASISTIO
```

### Descripción de Estados

| Estado | Descripción | Quién lo establece | Acciones disponibles |
|--------|-------------|-------------------|---------------------|
| **PROGRAMADA** | Cita recién creada, pendiente de confirmación | Sistema (al crear) | Confirmar, Cancelar |
| **CONFIRMADA** | Doctor ha confirmado que atenderá | Doctor | Completar, No asistió, Cancelar |
| **COMPLETADA** | Consulta realizada exitosamente | Doctor | Crear historia clínica |
| **CANCELADA** | Cita cancelada antes de realizarse | Doctor o Paciente | Ninguna (estado final) |
| **NO_ASISTIO** | Paciente no llegó a la cita | Doctor | Ninguna (estado final) |

---

## 🔌 API REST - ENDPOINTS DISPONIBLES

### Gestión de Citas

#### Crear Cita
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

#### Obtener Cita por ID
```http
GET /api/citas/{id}
```

#### Listar Todas las Citas
```http
GET /api/citas
```

#### Obtener Próximas Citas de Paciente
```http
GET /api/citas/paciente/{pacienteId}/proximas
```

#### Obtener Historial de Paciente
```http
GET /api/citas/paciente/{pacienteId}/historial
```

#### Obtener Citas de Hoy del Doctor
```http
GET /api/citas/doctor/{doctorId}/hoy
```

#### Obtener Próximas Citas del Doctor
```http
GET /api/citas/doctor/{doctorId}/proximas
```

#### Obtener Citas por Fecha
```http
GET /api/citas/fecha/2025-11-15
```

#### Actualizar Cita
```http
PUT /api/citas/{id}
Content-Type: application/json

{
  "nuevaFechaHora": "2025-11-16T11:00:00",
  "nuevoMotivo": "Consulta de seguimiento",
  "nuevaDuracion": 45
}
```

#### Confirmar Cita
```http
PATCH /api/citas/{id}/confirmar
```

#### Completar Cita
```http
PATCH /api/citas/{id}/completar
```

#### Cancelar Cita
```http
PATCH /api/citas/{id}/cancelar
Content-Type: application/json

{
  "notas": "Paciente reprogramó para otra fecha"
}
```

#### Marcar No Asistió
```http
PATCH /api/citas/{id}/no-asistio
```

#### Eliminar Cita
```http
DELETE /api/citas/{id}
```

#### Obtener Horarios Disponibles
```http
GET /api/citas/disponibles?doctorId={id}&fecha={YYYY-MM-DD}
# Respuesta: ["08:00", "10:00", "15:00", "17:00"]
```

### Doctores y Especialidades

#### Obtener Doctores por Especialidad
```http
GET /api/doctores/especialidad/{nombre}
# Ejemplo: GET /api/doctores/especialidad/Medicina General
# Retorna lista de doctores con esa especialidad
```

#### Obtener Todas las Especialidades
```http
GET /api/especialidades
```

---

## ✅ VALIDACIONES Y RESTRICCIONES

### Validaciones de Fecha y Hora
- ✅ No se pueden crear citas en el pasado
- ✅ Anticipación mínima: 1 hora
- ✅ Anticipación máxima: 90 días
- ✅ Horario laboral: 8:00 AM - 8:00 PM
- ✅ Solo fechas futuras permitidas

### Validaciones de Duración
- ✅ Duración mínima: 15 minutos
- ✅ Duración máxima: 180 minutos (3 horas)
- ✅ Duración por defecto: 30 minutos

### Validaciones de Conflictos
- ✅ Un doctor no puede tener dos citas al mismo tiempo
- ✅ Un paciente no puede tener dos citas al mismo tiempo
- ✅ Se considera la duración de las citas para detectar solapamientos
- ✅ Constraint UNIQUE en base de datos: (doctor_id, fecha_hora)

### Validaciones de Estado
- ✅ Solo se pueden cancelar citas activas (PROGRAMADA, CONFIRMADA)
- ✅ Solo se pueden completar citas activas
- ✅ Solo se puede marcar "no asistió" en citas pasadas
- ✅ Transiciones de estado válidas según diagrama

### Seguridad
- 🔐 Autenticación requerida en todos los endpoints
- 🔐 Autorización por rol (PACIENTE, DOCTOR, ADMIN)
- 🔐 Validación de permisos en cada acción
- 🔐 Protección CSRF habilitada
- 🔐 Protección contra doble reserva (transacciones)
- 🔐 Sanitización de inputs
- 🔐 Contraseñas hasheadas con BCrypt

---

## 🎨 CARACTERÍSTICAS DE UI/UX

### Diseño Visual
- ✅ Colores diferenciados por rol:
  - Azul: Paciente
  - Verde: Doctor
  - Gris: Admin
- ✅ Estados visuales claros (badges de color por estado)
- ✅ Responsive design para móviles y tablets
- ✅ Animaciones suaves en hover
- ✅ Iconos FontAwesome descriptivos
- ✅ Diseño moderno con gradientes

### Interactividad
- ✅ Carga dinámica de datos sin recargar página (AJAX)
- ✅ Validación de formularios en tiempo real
- ✅ Mensajes de éxito/error temporales
- ✅ Confirmaciones antes de acciones críticas
- ✅ Filtros en tiempo real
- ✅ Selección visual de horarios
- ✅ Actualizaciones automáticas de vistas

### Accesibilidad
- ✅ Labels descriptivos
- ✅ Tooltips informativos
- ✅ Estados disabled claros
- ✅ Mensajes de error visibles
- ✅ Navegación intuitiva

### Responsive Design

**Vista de Agendar Cita:**
- Desktop: Formulario centrado (900px max-width)
- Tablet: Adaptación automática de selects
- Mobile: Horarios en columna única

**Vista de Mis Citas:**
- Desktop: Tabla completa con todas las columnas
- Tablet: Columnas prioritarias
- Mobile: Cards apiladas

**Admin - Gestión de Doctores:**
- Desktop: 3 cards por fila
- Tablet (< 1200px): 2 cards por fila
- Mobile (< 768px): 1 card por fila

---

## 📁 ESTRUCTURA DE ARCHIVOS DEL PROYECTO

```
medicap/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/medipac/medipac/
│   │   │       ├── MediPacApplication.java
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── DatabaseConfig.java
│   │   │       │   ├── DataInitializer.java
│   │   │       │   └── CustomAuthenticationSuccessHandler.java
│   │   │       ├── controller/
│   │   │       │   ├── AdminController.java
│   │   │       │   ├── DoctorController.java
│   │   │       │   ├── PacienteController.java
│   │   │       │   ├── CitaRestController.java
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   ├── ErrorController.java
│   │   │       │   ├── HealthController.java
│   │   │       │   ├── SetupController.java
│   │   │       │   └── TestController.java
│   │   │       ├── model/
│   │   │       │   ├── Usuario.java
│   │   │       │   ├── Paciente.java
│   │   │       │   ├── Doctor.java
│   │   │       │   ├── Administrador.java
│   │   │       │   ├── Cita.java
│   │   │       │   ├── EstadoCita.java
│   │   │       │   ├── Especialidad.java
│   │   │       │   ├── HistoriaClinica.java
│   │   │       │   ├── Horario.java
│   │   │       │   ├── Prescripcion.java
│   │   │       │   └── DoctorPacienteFavorito.java
│   │   │       ├── repository/
│   │   │       │   ├── UsuarioRepository.java
│   │   │       │   ├── PacienteRepository.java
│   │   │       │   ├── DoctorRepository.java
│   │   │       │   ├── AdministradorRepository.java
│   │   │       │   ├── CitaRepository.java
│   │   │       │   ├── EspecialidadRepository.java
│   │   │       │   ├── HistoriaClinicaRepository.java
│   │   │       │   ├── HorarioRepository.java
│   │   │       │   ├── PrescripcionRepository.java
│   │   │       │   └── DoctorPacienteFavoritoRepository.java
│   │   │       ├── service/
│   │   │       │   ├── UsuarioService.java
│   │   │       │   ├── UsuarioValidationService.java
│   │   │       │   ├── CitaService.java
│   │   │       │   ├── CitaServiceMejorado.java
│   │   │       │   ├── HistoriaClinicaService.java
│   │   │       │   ├── AdminService.java
│   │   │       │   └── CalendarioService.java
│   │   │       └── dto/
│   │   │           ├── CitaDTO.java
│   │   │           ├── CrearCitaRequest.java
│   │   │           ├── ActualizarCitaRequest.java
│   │   │           ├── CitaMapper.java
│   │   │           ├── CalendarioDTO.java
│   │   │           └── DiaCalendarioDTO.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── templates/
│   │       │   ├── index.html
│   │       │   ├── logins/login.html
│   │       │   ├── error/error.html
│   │       │   ├── paciente/
│   │       │   │   ├── dashboard.html
│   │       │   │   ├── agendar-cita.html
│   │       │   │   ├── mis-citas.html
│   │       │   │   ├── historial.html
│   │       │   │   ├── perfil.html
│   │       │   │   ├── buscar-doctores.html
│   │       │   │   ├── perfil-doctor.html
│   │       │   │   └── notificaciones.html
│   │       │   ├── doctor/
│   │       │   │   ├── dashboard.html
│   │       │   │   ├── mis-citas.html
│   │       │   │   ├── mis-pacientes.html
│   │       │   │   ├── historias.html
│   │       │   │   ├── historias-paciente.html
│   │       │   │   ├── prescripciones.html
│   │       │   │   ├── editar-perfil.html
│   │       │   │   ├── calendario.html
│   │       │   │   └── notificaciones.html
│   │       │   └── admin/
│   │       │       ├── dashboard.html
│   │       │       ├── usuarios.html
│   │       │       ├── doctores.html
│   │       │       ├── citas.html
│   │       │       └── administradores.html
│   │       └── static/
│   │           ├── js/
│   │           │   ├── citas.js
│   │           │   ├── iconosInfintos.js
│   │           │   ├── justificacion.js
│   │           │   └── title.js
│   │           ├── css/
│   │           │   ├── index.css
│   │           │   ├── admin/admin.css
│   │           │   ├── login/login.css
│   │           │   └── error/error.css
│   │           └── components/
│   ├── test/
│   └── pom.xml
├── database-setup.sql
├── gestion-citas-completo.sql
├── insertar_especialidades.sql
├── insertar_especialidades.ps1
├── insertar_especialidades.bat
├── verificar_usuarios.sql
├── crear_admin.sql
├── crear_tabla_favoritos.sql
├── CREDENCIALES.md
├── DOCUMENTACION_CITAS.md
├── FLUJO_OPTIMIZADO_CITAS.md
├── IMPLEMENTACION_CITAS.md
├── CAMBIOS_RECIENTES.md
└── README.md (si existe)
```

---

## 🚀 CONFIGURACIÓN E INSTALACIÓN

### Requisitos Previos
- Java 21 o superior
- Maven 3.6+ o Maven Wrapper incluido
- MySQL 8.0+ instalado y ejecutándose
- Puerto 8080 disponible

### Pasos de Instalación

#### 1. Configurar Base de Datos
```bash
# Conectar a MySQL
mysql -u root -p

# Ejecutar script de creación de base de datos
source database-setup.sql

# Ejecutar script completo de gestión de citas
source gestion-citas-completo.sql

# Insertar especialidades médicas
source insertar_especialidades.sql
```

O desde línea de comandos:
```bash
mysql -u root -p < database-setup.sql
mysql -u root -p < gestion-citas-completo.sql
mysql -u root -p < insertar_especialidades.sql
```

#### 2. Configurar application.properties
Verificar que `src/main/resources/application.properties` tenga:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_citas_medicas
spring.datasource.username=root
spring.datasource.password=daniel
```

#### 3. Compilar el Proyecto
```bash
# Usando Maven Wrapper (recomendado)
./mvnw clean install

# O usando Maven instalado
mvn clean install
```

#### 4. Ejecutar la Aplicación
```bash
# Usando Maven Wrapper
./mvnw spring-boot:run

# O ejecutar el JAR
java -jar target/medipac-0.0.1-SNAPSHOT.jar
```

#### 5. Acceder a la Aplicación
- **Web:** http://localhost:8080
- **Login:** http://localhost:8080/login
- **API REST:** http://localhost:8080/api/citas

### Scripts de Utilidad

#### Insertar Especialidades (PowerShell)
```powershell
.\insertar_especialidades.ps1
```

#### Insertar Especialidades (Windows Batch)
```cmd
.\insertar_especialidades.bat
```

#### Verificar Usuarios
```bash
mysql -u root -p < verificar_usuarios.sql
```

---

## 🔔 NOTIFICACIONES Y ACTUALIZACIONES

### Actualizaciones Automáticas del Sistema

#### Cuando se CREA una cita:
1. ✅ Cita guardada en BD con estado `PROGRAMADA`
2. 📧 (Futuro) Email al paciente con detalles de la cita
3. 📧 (Futuro) Email al doctor sobre nueva cita
4. 🔄 Dashboard del paciente se actualiza automáticamente
5. 🔄 Agenda del doctor muestra la nueva cita

#### Cuando se CONFIRMA una cita:
1. ✅ Estado cambia a `CONFIRMADA`
2. 📧 (Futuro) Notificación al paciente
3. 🔄 Vista del paciente se actualiza

#### Cuando se CANCELA una cita:
1. ✅ Estado cambia a `CANCELADA`
2. 📝 Se guarda motivo de cancelación (si se proporciona)
3. 📧 (Futuro) Notificación a la otra parte (doctor o paciente)
4. 🔄 Horario queda nuevamente disponible
5. 🔄 Se actualiza en todas las vistas

#### Cuando se COMPLETA una cita:
1. ✅ Estado cambia a `COMPLETADA`
2. 📋 Opción de crear historia clínica habilitada
3. 📊 Se contabiliza en estadísticas del doctor
4. 🔄 Se actualiza historial del paciente

---

## 📈 CARACTERÍSTICAS DESTACADAS

### Funcionalidades Principales
1. **Sistema Completo** - Funcionalidad end-to-end implementada
2. **Tres Roles** - Paciente, Doctor y Admin con permisos específicos
3. **UI Moderna** - Diseño atractivo y responsive
4. **API REST** - Endpoints bien documentados
5. **Base de Datos** - Schema completo con relaciones
6. **Validaciones** - Lógica de negocio robusta
7. **Interactivo** - AJAX para experiencia fluida
8. **Seguro** - Autenticación y autorización
9. **Detección Inteligente de Conflictos** - Considera duración de citas
10. **Validaciones en Múltiples Capas** - Base de datos, JPA, Servicio, Controller

### Características Técnicas
- ✅ **Detección inteligente de conflictos** considerando duración de citas
- ✅ **Validaciones en múltiples capas**: Base de datos, JPA, Servicio, Controller
- ✅ **API REST completa** con respuestas JSON estandarizadas
- ✅ **DTOs validados** con Jakarta Validation
- ✅ **Procedimientos almacenados** para operaciones complejas
- ✅ **Vistas SQL** para consultas frecuentes optimizadas
- ✅ **Logging detallado** para debugging
- ✅ **Manejo robusto de errores** con mensajes claros
- ✅ **Transacciones** para garantizar consistencia de datos
- ✅ **Índices optimizados** para consultas rápidas

---

## 🔒 SEGURIDAD

### Medidas de Seguridad Implementadas
- 🔐 **Autenticación:** Spring Security con BCrypt para contraseñas
- 🔐 **Autorización:** Control de acceso basado en roles
- 🔐 **CSRF Protection:** Habilitado en todos los formularios
- 🔐 **Validación de Inputs:** Sanitización y validación de datos
- 🔐 **Protección de Rutas:** Rutas protegidas según rol
- 🔐 **Sesiones:** Gestión segura de sesiones de usuario
- 🔐 **SQL Injection:** Prevención mediante JPA/Hibernate
- 🔐 **XSS Protection:** Sanitización de datos en templates

### Permisos por Rol

**PACIENTE:**
- Ver y gestionar solo sus propias citas
- Agendar nuevas citas
- Ver su historial médico
- Actualizar su perfil

**DOCTOR:**
- Ver y gestionar citas donde es el médico asignado
- Confirmar/completar/cancelar sus citas
- Ver información de sus pacientes
- Crear historias clínicas
- Gestionar prescripciones

**ADMIN:**
- Acceso completo al sistema
- Gestión de todos los usuarios
- Gestión de todas las citas
- Asignación de especialidades
- Ver estadísticas globales

---

## 📊 ESTADÍSTICAS Y REPORTES

### Estadísticas Disponibles

**Para Pacientes:**
- Total de citas programadas
- Citas confirmadas
- Citas completadas
- Próximas citas

**Para Doctores:**
- Citas del día
- Próximas citas
- Total de pacientes atendidos
- Estadísticas por estado

**Para Administradores:**
- Total de usuarios en el sistema
- Total de doctores activos
- Total de pacientes registrados
- Total de citas del sistema
- Citas por estado
- Estadísticas por especialidad

---

## 🛠️ MANTENIMIENTO Y CONFIGURACIÓN

### Configuración de Logging
```properties
logging.level.com.medipac=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.com.zaxxer.hikari=DEBUG
```

### Configuración de JPA/Hibernate
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

### Configuración del Pool de Conexiones
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
spring.datasource.hikari.connection-timeout=20000
```

### Actuator (Monitoreo)
```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

Endpoints disponibles:
- `/actuator/health` - Estado de salud de la aplicación
- `/actuator/info` - Información de la aplicación
- `/actuator/metrics` - Métricas del sistema

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Error: "Cita ya existe en ese horario"
- **Causa:** Ya existe una cita activa (PROGRAMADA o CONFIRMADA) en el mismo horario
- **Solución:** Verificar que no haya una cita activa en el mismo horario, considerar la duración de las citas existentes

### Error: "Anticipación mínima no cumplida"
- **Causa:** Las citas deben agendarse con al menos 1 hora de anticipación
- **Solución:** Ajustar la constante `MINUTOS_ANTICIPACION_MINIMA` en `CitaServiceMejorado` si es necesario

### Error: "Fuera de horario laboral"
- **Causa:** Las citas solo pueden agendarse entre 8:00 AM y 8:00 PM
- **Solución:** Modificar las validaciones en `validarFechaHora()` si necesitas horarios diferentes

### Error: "LazyInitializationException"
- **Causa:** Intento de acceder a relaciones lazy fuera de una transacción
- **Solución:** Usar `@Transactional` y `Hibernate.initialize()` cuando sea necesario

### Error de Conexión a Base de Datos
- **Verificar:**
  1. MySQL está ejecutándose en `localhost:3306`
  2. La base de datos `gestion_citas_medicas` existe
  3. Usuario `root` tiene permisos
  4. Contraseña es correcta en `application.properties`

---

## 📝 NOTAS IMPORTANTES

### Cambios Recientes (13 de Noviembre, 2025)

1. **Corrección de Especialidades en Agendamiento**
   - Los nombres de especialidades ahora coinciden exactamente con la base de datos
   - Formato: "Medicina General" (no "MEDICINA_GENERAL")

2. **Eliminación de Sección "Bloqueados"**
   - Removida del panel de administración

3. **Corrección de Edición de Especialidades**
   - Agregado `@Transactional` y `Hibernate.initialize()` para evitar LazyInitializationException

### Consideraciones de Producción

⚠️ **IMPORTANTE:** Estas son configuraciones de desarrollo. Para producción:

1. **Cambiar todas las contraseñas** por defecto
2. **Configurar HTTPS** para comunicación segura
3. **Deshabilitar `spring.jpa.show-sql`** en producción
4. **Configurar logging apropiado** para producción
5. **Implementar backup automático** de base de datos
6. **Configurar variables de entorno** para credenciales
7. **Implementar autenticación de dos factores** (2FA)
8. **Configurar rate limiting** para APIs
9. **Implementar monitoreo y alertas**
10. **Configurar CORS** apropiadamente si hay frontend separado

---

## 🚀 PRÓXIMAS MEJORAS SUGERIDAS

### Fase 2 (Futuro)
- 📧 Sistema de notificaciones por email
- 📱 Notificaciones push en tiempo real
- 📅 Sincronización con Google Calendar
- 🔔 Recordatorios automáticos 24h antes
- 💬 Chat doctor-paciente
- 📊 Dashboard con gráficos interactivos
- 📄 Generación de reportes PDF
- 🔄 Reprogramación fácil de citas
- ⏰ Configuración de horarios personalizados por doctor
- 🎨 Temas personalizables
- 📱 Aplicación móvil nativa
- 💳 Sistema de pagos en línea
- 📋 Facturación automática
- 🔍 Búsqueda avanzada de pacientes
- 📈 Reportes estadísticos avanzados

---

## 📞 SOPORTE Y DOCUMENTACIÓN ADICIONAL

### Archivos de Documentación Disponibles
- `CREDENCIALES.md` - Credenciales de acceso por defecto
- `DOCUMENTACION_CITAS.md` - Documentación completa del sistema de citas
- `FLUJO_OPTIMIZADO_CITAS.md` - Flujos de trabajo detallados
- `IMPLEMENTACION_CITAS.md` - Detalles de implementación
- `CAMBIOS_RECIENTES.md` - Historial de cambios recientes

### Scripts SQL Disponibles
- `database-setup.sql` - Creación de base de datos
- `gestion-citas-completo.sql` - Schema completo de citas
- `insertar_especialidades.sql` - Inserción de especialidades médicas
- `crear_admin.sql` - Script para crear administrador
- `verificar_usuarios.sql` - Consultas de verificación
- `crear_tabla_favoritos.sql` - Tabla de doctores favoritos

---

## 📋 RESUMEN EJECUTIVO

**MediPac** es un sistema completo de gestión de citas médicas desarrollado con Spring Boot que permite:

✅ **Para Pacientes:**
- Agendar citas médicas de forma sencilla e intuitiva
- Ver y gestionar todas sus citas
- Acceder a su historial médico

✅ **Para Doctores:**
- Gestionar su agenda de citas
- Confirmar y completar consultas
- Crear historias clínicas y prescripciones

✅ **Para Administradores:**
- Control total del sistema
- Gestión de usuarios, doctores y especialidades
- Estadísticas y reportes completos

**Tecnologías:** Spring Boot 3.5.6, Java 21, MySQL 8.0+, Thymeleaf, Spring Security  
**Estado:** ✅ Producción Ready  
**Versión:** 1.0.0  
**Última actualización:** 13 de noviembre de 2025

---

**Este documento contiene toda la información necesaria para que una IA pueda generar un manual de usuario completo y detallado del sistema MediPac.**

