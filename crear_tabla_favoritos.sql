-- Crear tabla para favoritos de doctores
CREATE TABLE IF NOT EXISTS doctor_paciente_favoritos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    paciente_id BIGINT NOT NULL,
    fecha_agregado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctores(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id) ON DELETE CASCADE,
    UNIQUE KEY unique_doctor_paciente (doctor_id, paciente_id)
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_favoritos_doctor ON doctor_paciente_favoritos(doctor_id);
CREATE INDEX idx_favoritos_paciente ON doctor_paciente_favoritos(paciente_id);
