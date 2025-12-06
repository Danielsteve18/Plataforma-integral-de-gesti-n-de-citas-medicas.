# =============================================================================
# SCRIPT PARA INSERTAR ESPECIALIDADES MÉDICAS EN LA BASE DE DATOS
# Sistema: MediPac
# =============================================================================

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  INSERTAR ESPECIALIDADES MÉDICAS - MEDIPAC" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Configuración de la base de datos (ajustar según tu configuración)
$DB_NAME = "gestion_citas_medicas"
$DB_USER = "root"
$DB_PASSWORD = "daniel"
$SQL_FILE = "insertar_especialidades.sql"

Write-Host "Verificando conexión a la base de datos..." -ForegroundColor Yellow
Write-Host "Base de datos: $DB_NAME" -ForegroundColor Gray
Write-Host "Usuario: $DB_USER" -ForegroundColor Gray
Write-Host ""

# Verificar si existe el archivo SQL
if (-not (Test-Path $SQL_FILE)) {
    Write-Host "[ERROR] No se encuentra el archivo: $SQL_FILE" -ForegroundColor Red
    Write-Host ""
    Read-Host "Presiona Enter para salir"
    exit 1
}

# Verificar si MySQL está disponible
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlPath) {
    Write-Host "[ERROR] MySQL no está instalado o no está en el PATH" -ForegroundColor Red
    Write-Host "Asegúrate de tener MySQL instalado y agregado al PATH del sistema" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host "Ejecutando script SQL..." -ForegroundColor Yellow
Write-Host ""

# Ejecutar el script SQL
$env:MYSQL_PWD = $DB_PASSWORD
$result = & mysql -u $DB_USER $DB_NAME -e "source $SQL_FILE" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Green
    Write-Host "  ESPECIALIDADES INSERTADAS EXITOSAMENTE" -ForegroundColor Green
    Write-Host "============================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Las especialidades médicas han sido insertadas en la base de datos." -ForegroundColor Green
    Write-Host ""
    
    # Mostrar las especialidades insertadas
    Write-Host "Consultando especialidades en la base de datos..." -ForegroundColor Yellow
    & mysql -u $DB_USER $DB_NAME -e "SELECT id, nombre FROM especialidades ORDER BY nombre;" 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        & mysql -u $DB_USER $DB_NAME -e "SELECT id, nombre FROM especialidades ORDER BY nombre;"
    }
} else {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host "  ERROR AL INSERTAR ESPECIALIDADES" -ForegroundColor Red
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifica:" -ForegroundColor Yellow
    Write-Host "  1. Que MySQL esté instalado y en el PATH" -ForegroundColor Gray
    Write-Host "  2. Que la base de datos '$DB_NAME' exista" -ForegroundColor Gray
    Write-Host "  3. Que el usuario '$DB_USER' tenga permisos" -ForegroundColor Gray
    Write-Host "  4. Que la contraseña sea correcta" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Error: $result" -ForegroundColor Red
}

# Limpiar variable de entorno
Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue

Write-Host ""
Read-Host "Presiona Enter para salir"
