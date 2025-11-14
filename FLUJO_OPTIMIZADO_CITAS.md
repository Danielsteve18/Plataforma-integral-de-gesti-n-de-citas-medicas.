# 🔄 Flujo Optimizado de Gestión de Citas - MediPac

## 📋 Índice
1. [Flujo Completo del Paciente](#flujo-completo-del-paciente)
2. [Flujo Completo del Doctor](#flujo-completo-del-doctor)
3. [Flujo del Administrador](#flujo-del-administrador)
4. [Endpoints API Disponibles](#endpoints-api-disponibles)
5. [Estados de las Citas](#estados-de-las-citas)
6. [Notificaciones y Actualizaciones](#notificaciones-y-actualizaciones)

---

## 🩺 Flujo Completo del Paciente

### 1. Acceso al Sistema
```
Login → Dashboard del Paciente → Agendar Cita
```

### 2. Proceso de Agendamiento (Optimizado)

#### Paso 1: Selección de Especialidad
- **Vista:** `/paciente/agendar-cita`
- **Acción:** Paciente selecciona la especialidad médica necesaria
- **Opciones disponibles:**
  - Medicina General
  - Pediatría
  - Cardiología
  - Dermatología
  - Traumatología
  - Ginecología
  - Oftalmología
  - Psiquiatría
  - Neurología
  - Odontología

#### Paso 2: Selección de Doctor (Dinámica)
- **Endpoint:** `GET /api/doctores/especialidad/{nombre}`
- **Comportamiento:** 
  - Al seleccionar especialidad, se cargan automáticamente los doctores disponibles
  - Muestra información del doctor: nombre completo, licencia, teléfono
  - Solo doctores activos con esa especialidad

#### Paso 3: Selección de Fecha
- **Restricción:** Solo fechas futuras (desde hoy en adelante)
- **Input tipo:** `date` con validación `min=today`

#### Paso 4: Selección de Horario (Tiempo Real)
- **Endpoint:** `GET /api/citas/disponibles?doctorId={id}&fecha={fecha}`
- **Horarios estándar:**
  - Mañana: 08:00, 09:00, 10:00, 11:00, 12:00
  - Tarde: 14:00, 15:00, 16:00, 17:00, 18:00
- **Comportamiento:**
  - Filtra automáticamente horarios ya ocupados
  - Muestra solo slots disponibles
  - Selección visual con feedback (botones interactivos)

#### Paso 5: Motivo y Notas
- **Campos:**
  - Motivo de consulta (requerido)
  - Notas adicionales (opcional)

#### Paso 6: Confirmación
- **Endpoint:** `POST /api/citas`
- **Validaciones automáticas:**
  - Doctor existe y está activo
  - Fecha/hora no está ocupada
  - Fecha es futura
  - Todos los campos requeridos completos
- **Resultado:**
  - ✅ Éxito: Cita creada con estado `PROGRAMADA`
  - ❌ Error: Mensaje específico del problema

### 3. Gestión de Citas Programadas

#### Ver Mis Citas
- **Vista:** `/paciente/mis-citas`
- **Funcionalidades:**
  - Tabs de filtrado por estado:
    - Todas
    - Programadas
    - Confirmadas
    - Completadas
    - Canceladas
  - Para cada cita muestra:
    - Fecha y hora
    - Doctor asignado
    - Especialidad
    - Estado actual
    - Motivo de la consulta

#### Acciones Disponibles
- **Cancelar Cita:**
  - Endpoint: `PATCH /api/citas/{id}/cancelar`
  - Disponible solo para citas: PROGRAMADA o CONFIRMADA
  - Requiere confirmación del usuario
  - Opcional: agregar razón de cancelación

---

## 👨‍⚕️ Flujo Completo del Doctor

### 1. Acceso a la Agenda
```
Login → Dashboard del Doctor → Mi Agenda
```

### 2. Vista de Agenda
- **Vista:** `/doctor/mis-citas`
- **Endpoints de datos:**
  - `GET /api/citas/doctor/{doctorId}/hoy` - Citas del día
  - `GET /api/citas/doctor/{doctorId}/proximas` - Próximas citas

#### Funcionalidades de la Agenda

**Filtros Disponibles:**
- Por estado (PROGRAMADA, CONFIRMADA, COMPLETADA, CANCELADA)
- Por rango de fechas
- Vista de hoy / Vista semanal

**Información mostrada por cada cita:**
- Hora de la cita
- Datos del paciente (nombre, edad)
- Motivo de la consulta
- Estado actual
- Tiempo desde/hasta la cita

### 3. Acciones del Doctor

#### A) Confirmar Cita
- **Endpoint:** `PATCH /api/citas/{id}/confirmar`
- **Desde estado:** PROGRAMADA
- **A estado:** CONFIRMADA
- **Efecto:** Notifica al paciente que su cita está confirmada

#### B) Completar Cita
- **Endpoint:** `PATCH /api/citas/{id}/completar`
- **Desde estado:** CONFIRMADA
- **A estado:** COMPLETADA
- **Acción posterior:** Opción de crear historia clínica

#### C) Marcar No Asistió
- **Endpoint:** `PATCH /api/citas/{id}/no-asistio`
- **Desde estado:** CONFIRMADA
- **A estado:** NO_ASISTIO
- **Uso:** Cuando el paciente no llega a la cita

#### D) Cancelar Cita
- **Endpoint:** `PATCH /api/citas/{id}/cancelar`
- **Disponible para:** PROGRAMADA o CONFIRMADA
- **Requiere:** Motivo de cancelación
- **Efecto:** Notifica al paciente

### 4. Editar Perfil Profesional
- **Vista:** `/doctor/editar-perfil`
- **Campos editables:**
  - Nombre y apellido
  - Teléfono de contacto
- **Visualización:**
  - Especialidades asignadas (gestionadas por admin)
  - Número de licencia médica

---

## 🔧 Flujo del Administrador

### 1. Gestión de Usuarios y Doctores
```
Login → Dashboard Admin → Gestión de Doctores
```

#### Crear Doctores
1. Crear usuario normal
2. Cambiar rol a DOCTOR
3. **Automático:** Se asigna especialidad "Medicina General"
4. Agregar especialidades adicionales desde `/admin/doctores`

#### Vista de Doctores (`/admin/doctores`)
- **Layout:** Cards en grid 3x3 (responsive)
- **Por cada doctor:**
  - Nombre completo
  - Número de licencia
  - Especialidades actuales (badges)
  - Selector para agregar nueva especialidad
- **Endpoint:** `POST /admin/agregar-especialidad-doctor`

### 2. Gestión de Citas (Vista Global)
- **Vista:** `/admin/citas`
- **Capacidades:**
  - Ver todas las citas del sistema
  - Filtrar por: estado, doctor, paciente, fecha
  - Estadísticas generales
  - Modificar/cancelar cualquier cita

---

## 🔌 Endpoints API Disponibles

### Doctores
```http
GET /api/doctores/especialidad/{nombre}
# Retorna lista de doctores con esa especialidad
# Ejemplo: GET /api/doctores/especialidad/CARDIOLOGIA
```

### Horarios Disponibles
```http
GET /api/citas/disponibles?doctorId={id}&fecha={YYYY-MM-DD}
# Retorna array de horarios libres (formato "HH:mm")
# Ejemplo: GET /api/citas/disponibles?doctorId=2&fecha=2025-11-15
# Respuesta: ["08:00", "10:00", "15:00", "17:00"]
```

### Crear Cita
```http
POST /api/citas
Content-Type: application/json

{
  "pacienteId": 1,
  "doctorId": 2,
  "fechaHora": "2025-11-15T10:00:00",
  "motivo": "Consulta general",
  "duracionMinutos": 30
}
```

### Obtener Citas
```http
# Próximas citas del paciente
GET /api/citas/paciente/{pacienteId}/proximas

# Historial completo del paciente
GET /api/citas/paciente/{pacienteId}/historial

# Citas de hoy del doctor
GET /api/citas/doctor/{doctorId}/hoy

# Próximas citas del doctor
GET /api/citas/doctor/{doctorId}/proximas

# Citas por fecha
GET /api/citas/fecha/{YYYY-MM-DD}

# Cita específica por ID
GET /api/citas/{id}
```

### Acciones sobre Citas
```http
# Confirmar cita
PATCH /api/citas/{id}/confirmar

# Completar cita
PATCH /api/citas/{id}/completar

# Cancelar cita
PATCH /api/citas/{id}/cancelar
Body (opcional): { "notas": "Razón de cancelación" }

# Marcar no asistió
PATCH /api/citas/{id}/no-asistio
```

---

## 📊 Estados de las Citas

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

## 🔔 Notificaciones y Actualizaciones

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

## 🎯 Ventajas del Flujo Optimizado

### Para el Paciente
✅ **Proceso intuitivo** en 6 pasos claros
✅ **Selección dinámica** de doctores según especialidad
✅ **Horarios en tiempo real** - solo ve lo disponible
✅ **Sin doble reserva** - validación automática
✅ **Feedback inmediato** en cada paso
✅ **Control total** sobre sus citas (cancelación fácil)

### Para el Doctor
✅ **Agenda organizada** con filtros avanzados
✅ **Acciones rápidas** (confirmar/completar/cancelar)
✅ **Vista clara** de pacientes del día
✅ **Información relevante** del paciente en cada cita
✅ **Gestión de no asistencias** para seguimiento
✅ **Perfil profesional** editable

### Para el Administrador
✅ **Control total** sobre el sistema
✅ **Vista global** de todas las citas
✅ **Gestión de especialidades** visual y sencilla
✅ **Asignación automática** de especialidad base
✅ **Layout responsive** para doctores (3x3 grid)
✅ **Modificación flexible** de cualquier cita

---

## 🔒 Seguridad y Validaciones

### Validaciones del Sistema

#### Al crear cita:
- ✅ Fecha debe ser futura
- ✅ Doctor debe existir y estar activo
- ✅ Horario no debe estar ocupado
- ✅ Paciente debe estar autenticado
- ✅ Todos los campos requeridos completos

#### Al cambiar estado:
- ✅ Solo el doctor puede confirmar/completar
- ✅ Solo el paciente puede cancelar su propia cita (o admin)
- ✅ Transiciones de estado válidas según diagrama
- ✅ No se pueden modificar citas del pasado
- ✅ CSRF token validado en todas las operaciones

#### Seguridad:
- 🔐 Autenticación requerida en todos los endpoints
- 🔐 Autorización por rol (PACIENTE, DOCTOR, ADMIN)
- 🔐 Validación de permisos en cada acción
- 🔐 Protección contra doble reserva (transacciones)
- 🔐 Sanitización de inputs

---

## 📱 Responsive Design

### Vista de Agendar Cita
- **Desktop:** Formulario centrado (900px max-width)
- **Tablet:** Adaptación automática de selects
- **Mobile:** Horarios en columna única

### Vista de Mis Citas
- **Desktop:** Tabla completa con todas las columnas
- **Tablet:** Columnas prioritarias
- **Mobile:** Cards apiladas

### Admin - Gestión de Doctores
- **Desktop:** 3 cards por fila
- **Tablet (< 1200px):** 2 cards por fila
- **Mobile (< 768px):** 1 card por fila

---

## 🚀 Próximas Mejoras

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

---

**Última actualización:** 13 de noviembre de 2025
**Versión:** 1.0.0
**Autor:** Sistema MediPac
