-- Script para verificar usuarios registrados en MediPac
-- Actualizado según esquema real de BD

-- 1. Conectar a la base de datos
USE gestion_citas_medicas;

-- 2. Verificar estructura de las tablas principales
DESCRIBE usuarios;
DESCRIBE pacientes;
DESCRIBE doctores;

-- 3. Contar total de usuarios registrados
SELECT COUNT(*) as total_usuarios FROM usuarios;

-- 4. Ver todos los usuarios con sus datos completos
SELECT 
    u.id,
    u.username,
    u.email,
    u.rol,
    u.fecha_creacion,
    CASE 
        WHEN u.rol = 'PACIENTE' THEN CONCAT(p.nombre, ' ', p.apellido)
        WHEN u.rol = 'DOCTOR' THEN CONCAT(d.nombre, ' ', d.apellido)
        ELSE 'Sin nombre'
    END as nombre_completo
FROM usuarios u
LEFT JOIN pacientes p ON u.id = p.usuario_id AND u.rol = 'PACIENTE'
LEFT JOIN doctores d ON u.id = d.usuario_id AND u.rol = 'DOCTOR'
ORDER BY u.fecha_creacion DESC;

-- 5. Ver solo usuarios (tabla base)
SELECT 
    id,
    username,
    email,
    rol,
    fecha_creacion
FROM usuarios 
ORDER BY fecha_creacion DESC
LIMIT 10;

-- 6. Ver pacientes con datos completos
SELECT 
    u.id,
    u.username,
    u.email,
    CONCAT(p.nombre, ' ', p.apellido) as nombre_completo,
    p.fecha_nacimiento,
    p.genero,
    p.telefono,
    u.fecha_creacion
FROM usuarios u
JOIN pacientes p ON u.id = p.usuario_id
WHERE u.rol = 'PACIENTE'
ORDER BY u.fecha_creacion DESC;

-- 6. Verificar si existe un usuario específico por username
-- SELECT * FROM usuarios WHERE username = 'tu_usuario_aqui';

-- 7. Verificar si existe un usuario específico por email
-- SELECT * FROM usuarios WHERE email = 'tu_email@ejemplo.com';

-- 8. Ver usuarios por rol
SELECT 
    rol,
    COUNT(*) as cantidad
FROM usuarios 
GROUP BY rol;

-- 9. Ver últimos 5 usuarios registrados
SELECT 
    id,
    username,
    email,
    rol
FROM usuarios 
ORDER BY id DESC 
LIMIT 5;

-- 10. Verificar integridad de datos (usuarios sin email o username)
SELECT 'Usuarios sin email' as problema, COUNT(*) as cantidad
FROM usuarios 
WHERE email IS NULL OR email = ''
UNION ALL
SELECT 'Usuarios sin username' as problema, COUNT(*) as cantidad
FROM usuarios 
WHERE username IS NULL OR username = ''
UNION ALL
SELECT 'Usuarios sin password' as problema, COUNT(*) as cantidad
FROM usuarios 
WHERE password IS NULL OR password = '';
