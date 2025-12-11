@echo off
REM =============================================================================
REM SCRIPT PARA INSERTAR ESPECIALIDADES MÉDICAS EN LA BASE DE DATOS
REM Sistema: MediPac
REM =============================================================================

echo.
echo ============================================================
echo   INSERTAR ESPECIALIDADES MÉDICAS - MEDIPAC
echo ============================================================
echo.

REM Configuración de la base de datos (ajustar según tu configuración)
set DB_NAME=gestion_citas_medicas
set DB_USER=root
set DB_PASSWORD=daniel
set SQL_FILE=insertar_especialidades.sql

echo Verificando conexión a la base de datos...
echo Base de datos: %DB_NAME%
echo Usuario: %DB_USER%
echo.

REM Verificar si existe el archivo SQL
if not exist "%SQL_FILE%" (
    echo [ERROR] No se encuentra el archivo: %SQL_FILE%
    echo.
    pause
    exit /b 1
)

echo Ejecutando script SQL...
echo.

REM Ejecutar el script SQL
mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < %SQL_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo   ESPECIALIDADES INSERTADAS EXITOSAMENTE
    echo ============================================================
    echo.
    echo Las especialidades médicas han sido insertadas en la base de datos.
    echo.
) else (
    echo.
    echo ============================================================
    echo   ERROR AL INSERTAR ESPECIALIDADES
    echo ============================================================
    echo.
    echo Verifica:
    echo   1. Que MySQL esté instalado y en el PATH
    echo   2. Que la base de datos '%DB_NAME%' exista
    echo   3. Que el usuario '%DB_USER%' tenga permisos
    echo   4. Que la contraseña sea correcta
    echo.
)

pause



