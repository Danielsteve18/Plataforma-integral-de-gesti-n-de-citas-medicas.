# MediPac - Plataforma Integral de Gestión de Citas Médicas

## Descripción
**MediPac** es una solución robusta y escalable desarrollada para la digitalización y optimización de servicios de salud. El sistema permite una gestión centralizada de citas médicas, historiales clínicos y horarios de atención, facilitando la interacción entre administradores, personal médico y pacientes a través de un entorno seguro y eficiente.

Este proyecto ha sido diseñado siguiendo principios de **Arquitectura Limpia** y **Patrón MVC**, asegurando una base sólida para el crecimiento modular de funcionalidades. Es ideal para clínicas o consultorios que buscan transicionar de procesos manuales a una gestión técnica profesional.

---

## Tecnologías Utilizadas

### Backend
- **Java 21**: Uso de las librerías modernas de la plataforma para un rendimiento óptimo.
- **Spring Boot 3.5.6**: Framework principal para el desarrollo de la lógica de negocio y API.
- **Spring Security**: Implementación de seguridad basada en roles (RBAC) y manejo de sesiones.
- **Spring Data JPA & Hibernate**: Gestión eficiente de la persistencia de datos y relaciones complejas.
- **Lombok**: Reducción de código repetitivo y mejora de la legibilidad.
- **Maven**: Gestión de dependencias y automatización de procesos de construcción.

### Base de Datos
- **MySQL 8.0**: Motor de base de datos relacional para garantizar la integridad y consistencia de los datos médicos.

### Frontend
- **Thymeleaf**: Motor de plantillas para la generación dinámica de vistas en el servidor.
- **Vanilla CSS & JS**: Diseño de interfaz personalizado y responsivo, enfocado en la experiencia del usuario (UX).

---

## Funcionalidades Principales

- **Gestión Multi-rol**: Soporte nativo para tres niveles de acceso:
  - **Administrador**: Control total sobre el personal médico, especialidades y configuración del sistema.
  - **Médico**: Gestión de horarios de disponibilidad, consulta de citas asignadas y actualización de historiales clínicos.
  - **Paciente**: Registro autónomo, búsqueda de especialistas por especialidad y reserva de citas en tiempo real.
- **Control de Citas**: Flujo completo de estados (Pendiente, Confirmada, Cancelada, Realizada).
- **Sistema de Horarios**: Algoritmo para evitar el solapamiento de citas basado en la disponibilidad real de los médicos.
- **Historias Clínicas**: Repositorio centralizado por paciente para el seguimiento de consultas y diagnósticos.
- **Seguridad**: Cifrado de contraseñas con BCrypt y protección contra ataques comunes.

---

## Arquitectura y Estructura del Proyecto

El proyecto sigue una estructura estandarizada de Spring Boot que promueve la separación de responsabilidades:

- `com.medipac.medipac.controller`: Punto de entrada de las peticiones HTTP y manejo de la navegación.
- `com.medipac.medipac.service`: Capa de lógica de negocio donde se procesan las reglas del sistema.
- `com.medipac.medipac.model`: Definición de entidades JPA que representan el dominio del problema.
- `com.medipac.medipac.repository`: Interfaces para el acceso a datos mediante Spring Data.
- `com.medipac.medipac.dto`: Objetos de transferencia de datos para optimizar la comunicación entre capas.
- `src/main/resources/templates`: Vistas modulares organizadas por rol de usuario.

---

## Instalación y Ejecución

### Requisitos Previos
- **JDK 21** instalado y configurado.
- **MySQL 8.0** en ejecución.
- **Maven 3.6+** (opcional, se incluye `mvnw`).

### Pasos para el despliegue local
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Danielsteve18/Plataforma-integral-de-gesti-n-de-citas-medicas.git
   cd Plataforma-integral-de-gesti-n-de-citas-medicas
   ```

2. **Configuración de base de datos:**
   - Asegúrate de tener una base de datos llamada `gestion_citas_medicas`.
   - El sistema cargará automáticamente las tablas al iniciar (`ddl-auto=update`).
   - Puedes ajustar las credenciales en `src/main/resources/application.properties`.

3. **Ejecución rápida (Windows):**
   Utiliza el script proporcionado:
   ```bash
   run-app.bat
   ```

4. **Ejecución manual (Maven):**
   ```bash
   ./mvnw spring-boot:run
   ```

### Credenciales de Prueba (Auto-generadas)
Al iniciar, el sistema crea automáticamente usuarios de prueba si no existen:
- **Administrador**: `admin` / `admin123`
- **Médico**: `doctor1` / `doctor123`

---

## Demo / Video
*[Link al video demostrativo o captura de pantalla]*

---

## Estado del Proyecto
Actualmente en **Fase de Desarrollo / MVP**. Se han implementado las funcionalidades críticas de gestión y seguridad, garantizando un flujo estable de reserva y administración de citas.

---

## Autor y Contacto
**Daniel Steve**
- **GitHub**: [Danielsteve18](https://github.com/Danielsteve18)
- **LinkedIn**: www.linkedin.com/in/daniel-steve-montaño-arboleda-9748a0330
- **Youtube**: https://youtu.be/M6h4t7qhpZk
