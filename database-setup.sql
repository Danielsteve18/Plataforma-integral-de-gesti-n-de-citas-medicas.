-- Script para configurar la base de datos MediPac
-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS gestion_citas_medicas 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE gestion_citas_medicas;

-- Verificar que la base de datos fue creada correctamente
SELECT 'Base de datos gestion_citas_medicas creada correctamente' AS mensaje;

-- Mostrar información de la base de datos
SHOW DATABASES LIKE 'gestion_citas_medicas';