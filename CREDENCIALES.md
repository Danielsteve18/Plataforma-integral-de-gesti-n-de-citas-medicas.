# 🔐 Credenciales de Acceso - MediPac

Este documento contiene las credenciales por defecto para acceder al sistema de gestión de citas médicas MediPac.

---

## 👨‍💼 Administrador

**Usuario:** `admin`  
**Contraseña:** `admin123`  
**URL de acceso:** http://localhost:8080/login  
**Dashboard:** http://localhost:8080/admin/dashboard

### Funcionalidades del Admin:
- Gestión de usuarios (crear, editar, eliminar)
- Cambiar roles de usuarios (ADMIN, DOCTOR, PACIENTE)
- Gestión de doctores y especialidades
- Ver estadísticas del sistema
- Administración completa del sistema

---

## 👨‍⚕️ Doctor

**Usuario:** `doctor2`  
**Contraseña:** `doctor123`  
**URL de acceso:** http://localhost:8080/login  
**Dashboard:** http://localhost:8080/doctor/dashboard

### Funcionalidades del Doctor:
- Ver y gestionar agenda de citas
- Confirmar, completar o rechazar citas
- Ver lista de pacientes
- Gestionar historias clínicas
- Editar perfil profesional
- Ver prescripciones

---

## 👤 Paciente

**Usuario:** `paciente1`  
**Contraseña:** `paciente123`  
**URL de acceso:** http://localhost:8080/login  
**Dashboard:** http://localhost:8080/paciente/dashboard

### Funcionalidades del Paciente:
- Agendar nuevas citas médicas
- Ver mis citas programadas
- Cancelar citas
- Ver historial médico
- Actualizar información personal

---

## 🚀 Cómo Iniciar el Sistema

1. Asegúrate de tener MySQL ejecutándose en `localhost:3306`
2. La base de datos debe llamarse: `gestion_citas_medicas`
3. Usuario de MySQL: `root`
4. Contraseña de MySQL: `daniel`
5. Ejecuta el proyecto:
   ```bash
   ./mvnw spring-boot:run
   ```
6. Accede a: http://localhost:8080

---

## 📝 Notas Importantes

- **Cambiar contraseñas:** Las contraseñas están hasheadas con BCrypt. Para cambiarlas, contacta al administrador del sistema.
- **Crear nuevos usuarios:** El administrador puede crear nuevos usuarios desde su dashboard.
- **Crear doctores:** Crea un usuario normal y luego cambia su rol a DOCTOR desde el panel de administración.
- **Especialidades:** Los doctores nuevos reciben automáticamente la especialidad "Medicina General". El admin puede agregar más especialidades.

---

## ⚠️ Seguridad

- Estas son credenciales de **desarrollo**
- En producción, **DEBES** cambiar todas las contraseñas
- Nunca compartas estas credenciales en repositorios públicos
- Implementa autenticación de dos factores en producción

---

**Última actualización:** 13 de noviembre de 2025
