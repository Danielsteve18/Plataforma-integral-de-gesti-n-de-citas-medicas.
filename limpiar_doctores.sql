-- Script para eliminar todos los doctores y sus usuarios
-- Ejecutar en MySQL antes de crear nuevos doctores

USE gestion_citas_medicas;

-- Primero eliminar las relaciones en doctores_especialidades
DELETE FROM doctores_especialidades WHERE doctor_id IN (SELECT usuario_id FROM doctores);

-- Eliminar historias clínicas relacionadas con citas de doctores
DELETE FROM historias_clinicas WHERE cita_id IN (SELECT id FROM citas WHERE doctor_id IN (SELECT usuario_id FROM doctores));

-- Eliminar las citas de los doctores
DELETE FROM citas WHERE doctor_id IN (SELECT usuario_id FROM doctores);

-- Eliminar los registros de doctores
DELETE FROM doctores;

-- Eliminar los usuarios con rol DOCTOR
DELETE FROM usuarios WHERE rol = 'DOCTOR';

-- Verificar que se eliminaron correctamente
SELECT COUNT(*) AS total_doctores FROM doctores;
SELECT COUNT(*) AS total_usuarios_doctor FROM usuarios WHERE rol = 'DOCTOR';

SELECT '✅ Doctores eliminados correctamente. Ahora puedes crear nuevos doctores desde la aplicación.' AS Mensaje;
