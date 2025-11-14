# Script PowerShell para verificar usuarios en MediPac
# Ejecutar desde PowerShell como administrador

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "    VERIFICACION USUARIOS MEDIPAC          " -ForegroundColor Cyan  
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Configuración de conexión
$servidor = "localhost"
$baseDatos = "gestion_citas_medicas"
$usuario = "root"

# Solicitar contraseña de forma segura
$password = Read-Host "Ingresa la contraseña de MySQL" -AsSecureString
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

try {
    Write-Host "Conectando a MySQL..." -ForegroundColor Yellow
    
    # Queries para ejecutar
    $queries = @(
        "SELECT COUNT(*) as total_usuarios FROM usuarios;",
        "SELECT id, username, email, nombre_completo, rol, fecha_creacion FROM usuarios ORDER BY id DESC LIMIT 5;",
        "SELECT rol, COUNT(*) as cantidad FROM usuarios GROUP BY rol;"
    )
    
    foreach ($query in $queries) {
        Write-Host "`nEjecutando consulta..." -ForegroundColor Green
        Write-Host $query -ForegroundColor Gray
        
        $command = "mysql -h$servidor -u$usuario -p$plainPassword -D$baseDatos -e `"$query`""
        Invoke-Expression $command
    }
    
    Write-Host "`n✅ Verificación completada exitosamente!" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error al conectar a MySQL: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nPresiona Enter para continuar..."
Read-Host