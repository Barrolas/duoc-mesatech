# Levanta BFF + MS en segundo plano (misma sesion PowerShell: variables de proceso).
# Requisitos: docker compose en infra/, JARs en infra/out/.
#
# Con Entra del equipo (recomendado):
#   $env:ENTRA_ISSUER_URI="https://login.microsoftonline.com/<TENANT>/v2.0"
#   $env:ENTRA_AUDIENCE="<API_CLIENT_ID>"
#   .\infra\scripts\run-local-stack.ps1
#
# Detener: Get-Job | Stop-Job; Get-Job | Remove-Job; Get-Process java -ErrorAction SilentlyContinue | Stop-Process

$ErrorActionPreference = "Stop"
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$LogDir = Join-Path $RepoRoot "infra\out\logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

if (-not $env:ENTRA_ISSUER_URI) {
    Write-Warning "ENTRA_ISSUER_URI no definido - issuer common (solo arranque local)."
    $env:ENTRA_ISSUER_URI = "https://login.microsoftonline.com/common/v2.0"
}
if (-not $env:ENTRA_AUDIENCE) {
    Write-Warning "ENTRA_AUDIENCE no definido - placeholder."
    $env:ENTRA_AUDIENCE = "00000000-0000-0000-0000-000000000001"
}
if (-not $env:POSTGRES_USER) { $env:POSTGRES_USER = "mesatech" }
if (-not $env:POSTGRES_PASSWORD) { $env:POSTGRES_PASSWORD = "mesatech" }
if (-not $env:MS_SOLICITUDES_URL) { $env:MS_SOLICITUDES_URL = "http://localhost:8081" }
if (-not $env:MS_CATALOGO_URL) { $env:MS_CATALOGO_URL = "http://localhost:8082" }
if (-not $env:CATALOGO_DATASOURCE_URL) {
    $env:CATALOGO_DATASOURCE_URL = "jdbc:postgresql://localhost:5433/mesatech_catalogo"
}
if (-not $env:SOLICITUDES_DATASOURCE_URL) {
    $env:SOLICITUDES_DATASOURCE_URL = "jdbc:postgresql://localhost:5433/mesatech_solicitudes"
}
if (-not $env:RABBITMQ_HOST) { $env:RABBITMQ_HOST = "localhost" }
if (-not $env:RABBITMQ_PORT) { $env:RABBITMQ_PORT = "5672" }
if (-not $env:RABBITMQ_VHOST) { $env:RABBITMQ_VHOST = "mesatech" }
if (-not $env:RABBITMQ_USER) { $env:RABBITMQ_USER = "mesatech" }
if (-not $env:RABBITMQ_PASSWORD) { $env:RABBITMQ_PASSWORD = "mesatech" }
if (-not $env:RABBITMQ_DECLARE) { $env:RABBITMQ_DECLARE = "true" }

function Start-MesatechJob {
    param([string]$Name, [string]$Jar)
    $jarPath = Join-Path $RepoRoot "infra\out\$Jar"
    if (-not (Test-Path $jarPath)) { throw "Falta $jarPath - ejecuta .\infra\scripts\build-jars.ps1" }
    $log = Join-Path $LogDir "$Name.log"
    $script = {
        param($Root, $JarPath, $LogPath, $EnvMap)
        Set-Location $Root
        foreach ($k in $EnvMap.Keys) { Set-Item -Path "Env:$k" -Value $EnvMap[$k] }
        & java -jar $JarPath *> $LogPath
    }
    $envMap = @{
        ENTRA_ISSUER_URI           = $env:ENTRA_ISSUER_URI
        ENTRA_AUDIENCE             = $env:ENTRA_AUDIENCE
        POSTGRES_USER              = $env:POSTGRES_USER
        POSTGRES_PASSWORD          = $env:POSTGRES_PASSWORD
        MS_SOLICITUDES_URL         = $env:MS_SOLICITUDES_URL
        MS_CATALOGO_URL            = $env:MS_CATALOGO_URL
        CORS_ALLOWED_ORIGINS       = $env:CORS_ALLOWED_ORIGINS
        CATALOGO_DATASOURCE_URL    = $env:CATALOGO_DATASOURCE_URL
        SOLICITUDES_DATASOURCE_URL = $env:SOLICITUDES_DATASOURCE_URL
        RABBITMQ_HOST              = $env:RABBITMQ_HOST
        RABBITMQ_PORT              = $env:RABBITMQ_PORT
        RABBITMQ_VHOST             = $env:RABBITMQ_VHOST
        RABBITMQ_USER              = $env:RABBITMQ_USER
        RABBITMQ_PASSWORD          = $env:RABBITMQ_PASSWORD
        RABBITMQ_DECLARE           = $env:RABBITMQ_DECLARE
    }
    $job = Start-Job -Name $Name -ScriptBlock $script -ArgumentList $RepoRoot, $jarPath, $log, $envMap
    Write-Host "Job $Name (Id $($job.Id)) log: $log"
}

Start-MesatechJob "ms-solicitudes" "ms-solicitudes.jar"
Start-Sleep -Seconds 12
Start-MesatechJob "ms-catalogo" "ms-catalogo.jar"
Start-Sleep -Seconds 12
Start-MesatechJob "bff" "bff.jar"
Write-Host "Espera ~20s. Prueba: curl.exe -i http://127.0.0.1:8080/public/hola"
Write-Host "Jobs: Get-Job | Format-Table"
