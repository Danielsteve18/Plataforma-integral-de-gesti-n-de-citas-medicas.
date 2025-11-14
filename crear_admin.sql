-- Script para crear usuario administrador en MediPac
-- Ejecutar en MySQL Workbench o cliente MySQL

USE gestion_citas_medicas;

-- Verificar si ya existe el usuario admin
SELECT COUNT(*) as admin_existe FROM usuarios WHERE username = 'admin';

-- Crear usuario admin si no existe
INSERT IGNORE INTO usuarios (username, password, email, rol, fecha_creacion) 
VALUES ('admin', '$2a$12$nmps7Zv2Q0FvPo06uQwHwuPH47Kw/g7wQydv409C5wmtMKO8qhDye', 'admin@medipac.com', 'ADMINISTRADOR', NOW());

-- Verificar que se creó correctamente
SELECT * FROM usuarios WHERE username = 'admin';

-- Mostrar todos los usuarios
SELECT id, username, email, rol, fecha_creacion FROM usuarios ORDER BY fecha_creacion DESC;