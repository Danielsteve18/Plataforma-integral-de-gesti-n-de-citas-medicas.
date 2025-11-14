@echo off
echo ===========================================
echo    VERIFICACION RAPIDA - USUARIOS MEDIPAC
echo ===========================================
echo.

echo Conectando a MySQL...
mysql -u root -p -e "
USE gestion_citas_medicas;

SELECT '=== ESTRUCTURA DE TABLA ===' as info;
DESCRIBE usuarios;

SELECT '=== TOTAL USUARIOS ===' as info;
SELECT COUNT(*) as total_usuarios FROM usuarios;

SELECT '=== ULTIMOS USUARIOS REGISTRADOS ===' as info;
SELECT 
    id,
    username,
    email,
    nombre_completo,
    rol,
    DATE_FORMAT(fecha_creacion, '%%Y-%%m-%%d %%H:%%i:%%s') as fecha_registro
FROM usuarios 
ORDER BY id DESC 
LIMIT 10;
"

echo.
echo Verificacion completada.
pause