-- =============================================================================
-- SCRIPT COMPLETO PARA GESTIÓN DE CITAS MÉDICAS - MEDIPAC
-- Versión: 1.0
-- Fecha: Noviembre 2025
-- =============================================================================

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS gestion_citas_medicas 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE gestion_citas_medicas;

-- =============================================================================
-- TABLA: usuarios
-- Almacena información básica de todos los usuarios del sistema
-- =============================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    rol ENUM('PACIENTE', 'DOCTOR', 'ADMINISTRADOR') NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_rol (rol),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: especialidades
-- Catálogo de especialidades médicas disponibles
-- =============================================================================
CREATE TABLE IF NOT EXISTS especialidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: pacientes
-- Información específica de los pacientes
-- =============================================================================
CREATE TABLE IF NOT EXISTS pacientes (
    usuario_id BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    genero VARCHAR(50),
    telefono VARCHAR(50),
    direccion VARCHAR(255),
    CONSTRAINT fk_paciente_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE,
    INDEX idx_nombre_apellido (nombre, apellido),
    INDEX idx_telefono (telefono)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: doctores
-- Información específica de los doctores
-- =============================================================================
CREATE TABLE IF NOT EXISTS doctores (
    usuario_id BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    numero_licencia VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(50),
    consultorio VARCHAR(100),
    CONSTRAINT fk_doctor_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE,
    INDEX idx_nombre_apellido (nombre, apellido),
    INDEX idx_numero_licencia (numero_licencia),
    INDEX idx_telefono (telefono)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: administradores
-- Información específica de los administradores
-- =============================================================================
CREATE TABLE IF NOT EXISTS administradores (
    usuario_id BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    departamento VARCHAR(100),
    CONSTRAINT fk_admin_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE,
    INDEX idx_nombre_apellido (nombre, apellido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: doctores_especialidades
-- Relación muchos a muchos entre doctores y especialidades
-- =============================================================================
CREATE TABLE IF NOT EXISTS doctores_especialidades (
    doctor_id BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,
    fecha_asignacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (doctor_id, especialidad_id),
    CONSTRAINT fk_de_doctor 
        FOREIGN KEY (doctor_id) 
        REFERENCES doctores(usuario_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_de_especialidad 
        FOREIGN KEY (especialidad_id) 
        REFERENCES especialidades(id) 
        ON DELETE CASCADE,
    INDEX idx_doctor (doctor_id),
    INDEX idx_especialidad (especialidad_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: horarios
-- Horarios de disponibilidad de los doctores
-- =============================================================================
CREATE TABLE IF NOT EXISTS horarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_horario_doctor 
        FOREIGN KEY (doctor_id) 
        REFERENCES doctores(usuario_id) 
        ON DELETE CASCADE,
    INDEX idx_doctor_fecha (doctor_id, fecha),
    INDEX idx_fecha (fecha),
    INDEX idx_disponible (disponible)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: citas
-- Gestión completa de citas médicas
-- =============================================================================
CREATE TABLE IF NOT EXISTS citas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    duracion_minutos INT DEFAULT 30,
    motivo VARCHAR(500),
    estado ENUM('PROGRAMADA', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO') DEFAULT 'PROGRAMADA' NOT NULL,
    notas_cancelacion VARCHAR(500),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cita_paciente 
        FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(usuario_id) 
        ON DELETE RESTRICT,
    CONSTRAINT fk_cita_doctor 
        FOREIGN KEY (doctor_id) 
        REFERENCES doctores(usuario_id) 
        ON DELETE RESTRICT,
    INDEX idx_fecha_hora (fecha_hora),
    INDEX idx_estado (estado),
    INDEX idx_paciente_fecha (paciente_id, fecha_hora),
    INDEX idx_doctor_fecha (doctor_id, fecha_hora),
    INDEX idx_fecha_creacion (fecha_creacion),
    -- Constraint para evitar citas duplicadas en el mismo horario con el mismo doctor
    UNIQUE KEY unique_doctor_fecha_hora (doctor_id, fecha_hora),
    -- Constraint para asegurar duraciones válidas
    CONSTRAINT chk_duracion CHECK (duracion_minutos >= 15 AND duracion_minutos <= 180)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLA: historias_clinicas
-- Registro de historias clínicas asociadas a citas completadas
-- =============================================================================
CREATE TABLE IF NOT EXISTS historias_clinicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cita_id BIGINT NOT NULL UNIQUE,
    diagnostico TEXT NOT NULL,
    prescripcion TEXT,
    notas TEXT,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_historia_cita 
        FOREIGN KEY (cita_id) 
        REFERENCES citas(id) 
        ON DELETE RESTRICT,
    INDEX idx_cita (cita_id),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- VISTAS ÚTILES PARA CONSULTAS FRECUENTES
-- =============================================================================

-- Vista: Citas con información completa
CREATE OR REPLACE VIEW vista_citas_completas AS
SELECT 
    c.id AS cita_id,
    c.fecha_hora,
    c.duracion_minutos,
    c.motivo,
    c.estado,
    c.fecha_creacion AS cita_fecha_creacion,
    p.nombre AS paciente_nombre,
    p.apellido AS paciente_apellido,
    p.telefono AS paciente_telefono,
    d.nombre AS doctor_nombre,
    d.apellido AS doctor_apellido,
    d.consultorio,
    GROUP_CONCAT(e.nombre SEPARATOR ', ') AS especialidades,
    CASE 
        WHEN hc.id IS NOT NULL THEN 'Si'
        ELSE 'No'
    END AS tiene_historia_clinica
FROM citas c
INNER JOIN pacientes p ON c.paciente_id = p.usuario_id
INNER JOIN doctores d ON c.doctor_id = d.usuario_id
LEFT JOIN doctores_especialidades de ON d.usuario_id = de.doctor_id
LEFT JOIN especialidades e ON de.especialidad_id = e.id
LEFT JOIN historias_clinicas hc ON c.id = hc.cita_id
GROUP BY c.id, c.fecha_hora, c.duracion_minutos, c.motivo, c.estado, c.fecha_creacion,
         p.nombre, p.apellido, p.telefono, d.nombre, d.apellido, d.consultorio, hc.id;

-- Vista: Estadísticas de citas por doctor
CREATE OR REPLACE VIEW vista_estadisticas_doctor AS
SELECT 
    d.usuario_id,
    d.nombre,
    d.apellido,
    COUNT(DISTINCT c.id) AS total_citas,
    SUM(CASE WHEN c.estado = 'PROGRAMADA' THEN 1 ELSE 0 END) AS citas_programadas,
    SUM(CASE WHEN c.estado = 'CONFIRMADA' THEN 1 ELSE 0 END) AS citas_confirmadas,
    SUM(CASE WHEN c.estado = 'COMPLETADA' THEN 1 ELSE 0 END) AS citas_completadas,
    SUM(CASE WHEN c.estado = 'CANCELADA' THEN 1 ELSE 0 END) AS citas_canceladas,
    COUNT(DISTINCT hc.id) AS historias_clinicas_creadas
FROM doctores d
LEFT JOIN citas c ON d.usuario_id = c.doctor_id
LEFT JOIN historias_clinicas hc ON c.id = hc.cita_id
GROUP BY d.usuario_id, d.nombre, d.apellido;

-- Vista: Próximas citas (siguientes 7 días)
CREATE OR REPLACE VIEW vista_proximas_citas AS
SELECT 
    c.id,
    c.fecha_hora,
    c.estado,
    CONCAT(p.nombre, ' ', p.apellido) AS paciente,
    CONCAT(d.nombre, ' ', d.apellido) AS doctor,
    c.motivo
FROM citas c
INNER JOIN pacientes p ON c.paciente_id = p.usuario_id
INNER JOIN doctores d ON c.doctor_id = d.usuario_id
WHERE c.fecha_hora BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY)
  AND c.estado IN ('PROGRAMADA', 'CONFIRMADA')
ORDER BY c.fecha_hora ASC;

-- =============================================================================
-- DATOS INICIALES: Especialidades médicas comunes
-- =============================================================================
INSERT INTO especialidades (nombre, descripcion) VALUES
('Medicina General', 'Atención médica general y preventiva'),
('Cardiología', 'Especialidad en el corazón y sistema circulatorio'),
('Pediatría', 'Atención médica para niños y adolescentes'),
('Dermatología', 'Especialidad en enfermedades de la piel'),
('Ginecología', 'Salud reproductiva femenina'),
('Traumatología', 'Lesiones del sistema musculoesquelético'),
('Oftalmología', 'Salud visual y enfermedades de los ojos'),
('Otorrinolaringología', 'Oído, nariz y garganta'),
('Neurología', 'Enfermedades del sistema nervioso'),
('Psiquiatría', 'Salud mental y trastornos psicológicos'),
('Urología', 'Sistema urinario y reproductivo masculino'),
('Endocrinología', 'Trastornos hormonales y metabólicos'),
('Gastroenterología', 'Aparato digestivo'),
('Neumología', 'Sistema respiratorio'),
('Oncología', 'Diagnóstico y tratamiento del cáncer')
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);

-- =============================================================================
-- PROCEDIMIENTOS ALMACENADOS ÚTILES
-- =============================================================================

-- Procedimiento: Obtener disponibilidad de un doctor en una fecha
DELIMITER //
CREATE PROCEDURE sp_obtener_disponibilidad_doctor(
    IN p_doctor_id BIGINT,
    IN p_fecha DATE
)
BEGIN
    -- Obtener horarios disponibles del doctor
    SELECT 
        h.id,
        h.fecha,
        h.hora_inicio,
        h.hora_fin,
        h.disponible,
        COUNT(c.id) AS citas_programadas
    FROM horarios h
    LEFT JOIN citas c ON c.doctor_id = h.doctor_id 
        AND DATE(c.fecha_hora) = h.fecha
        AND c.estado IN ('PROGRAMADA', 'CONFIRMADA')
    WHERE h.doctor_id = p_doctor_id
      AND h.fecha = p_fecha
      AND h.disponible = TRUE
    GROUP BY h.id, h.fecha, h.hora_inicio, h.hora_fin, h.disponible;
END //
DELIMITER ;

-- Procedimiento: Crear una cita con validaciones
DELIMITER //
CREATE PROCEDURE sp_crear_cita(
    IN p_paciente_id BIGINT,
    IN p_doctor_id BIGINT,
    IN p_fecha_hora DATETIME,
    IN p_motivo VARCHAR(500),
    IN p_duracion INT,
    OUT p_resultado VARCHAR(255),
    OUT p_cita_id BIGINT
)
BEGIN
    DECLARE v_existe_conflicto INT;
    DECLARE v_fecha_pasada INT;
    
    -- Verificar que la fecha no sea en el pasado
    SELECT COUNT(*) INTO v_fecha_pasada
    FROM DUAL
    WHERE p_fecha_hora < NOW();
    
    IF v_fecha_pasada > 0 THEN
        SET p_resultado = 'ERROR: No se puede agendar una cita en el pasado';
        SET p_cita_id = NULL;
    ELSE
        -- Verificar conflictos de horario
        SELECT COUNT(*) INTO v_existe_conflicto
        FROM citas
        WHERE doctor_id = p_doctor_id
          AND fecha_hora = p_fecha_hora
          AND estado IN ('PROGRAMADA', 'CONFIRMADA');
        
        IF v_existe_conflicto > 0 THEN
            SET p_resultado = 'ERROR: El doctor ya tiene una cita en ese horario';
            SET p_cita_id = NULL;
        ELSE
            -- Crear la cita
            INSERT INTO citas (paciente_id, doctor_id, fecha_hora, motivo, duracion_minutos, estado)
            VALUES (p_paciente_id, p_doctor_id, p_fecha_hora, p_motivo, IFNULL(p_duracion, 30), 'PROGRAMADA');
            
            SET p_cita_id = LAST_INSERT_ID();
            SET p_resultado = 'SUCCESS: Cita creada exitosamente';
        END IF;
    END IF;
END //
DELIMITER ;

-- Procedimiento: Obtener estadísticas generales del sistema
DELIMITER //
CREATE PROCEDURE sp_estadisticas_sistema()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM usuarios WHERE rol = 'PACIENTE' AND activo = TRUE) AS total_pacientes,
        (SELECT COUNT(*) FROM usuarios WHERE rol = 'DOCTOR' AND activo = TRUE) AS total_doctores,
        (SELECT COUNT(*) FROM citas) AS total_citas,
        (SELECT COUNT(*) FROM citas WHERE estado = 'PROGRAMADA') AS citas_programadas,
        (SELECT COUNT(*) FROM citas WHERE estado = 'CONFIRMADA') AS citas_confirmadas,
        (SELECT COUNT(*) FROM citas WHERE estado = 'COMPLETADA') AS citas_completadas,
        (SELECT COUNT(*) FROM citas WHERE estado = 'CANCELADA') AS citas_canceladas,
        (SELECT COUNT(*) FROM historias_clinicas) AS total_historias_clinicas,
        (SELECT COUNT(*) FROM especialidades) AS total_especialidades;
END //
DELIMITER ;

-- =============================================================================
-- TRIGGERS PARA AUDITORÍA Y VALIDACIONES
-- =============================================================================

-- Trigger: Validar que una cita solo puede tener historia clínica si está completada
DELIMITER //
CREATE TRIGGER trg_validar_historia_clinica
BEFORE INSERT ON historias_clinicas
FOR EACH ROW
BEGIN
    DECLARE v_estado VARCHAR(50);
    
    SELECT estado INTO v_estado
    FROM citas
    WHERE id = NEW.cita_id;
    
    IF v_estado != 'COMPLETADA' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Solo se pueden crear historias clínicas para citas completadas';
    END IF;
END //
DELIMITER ;

-- Trigger: Actualizar fecha de actualización en citas
DELIMITER //
CREATE TRIGGER trg_actualizar_fecha_cita
BEFORE UPDATE ON citas
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END //
DELIMITER ;

-- =============================================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- =============================================================================

-- Índice compuesto para búsquedas de citas por paciente y estado
CREATE INDEX idx_paciente_estado ON citas(paciente_id, estado, fecha_hora);

-- Índice compuesto para búsquedas de citas por doctor y estado
CREATE INDEX idx_doctor_estado ON citas(doctor_id, estado, fecha_hora);

-- Índice para búsquedas por rango de fechas
CREATE INDEX idx_citas_rango_fecha ON citas(fecha_hora, estado);

-- =============================================================================
-- INFORMACIÓN DEL SCRIPT
-- =============================================================================
SELECT '✅ Base de datos creada exitosamente' AS mensaje;
SELECT '✅ Tablas creadas con índices optimizados' AS mensaje;
SELECT '✅ Vistas creadas para consultas frecuentes' AS mensaje;
SELECT '✅ Procedimientos almacenados listos' AS mensaje;
SELECT '✅ Triggers de validación activos' AS mensaje;
SELECT '✅ Especialidades médicas inicializadas' AS mensaje;

-- Mostrar estadísticas del sistema
CALL sp_estadisticas_sistema();

-- Fin del script
