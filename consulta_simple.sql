-- Script simple para verificar total de usuarios en MediPac
USE gestion_citas_medicas;

-- Total de usuarios registrados
SELECT COUNT(*) as total_usuarios FROM usuarios;

-- Ver todos los usuarios (sin passwords)
SELECT 
    id,
    username,
    email,
    rol
FROM usuarios 
ORDER BY id DESC;