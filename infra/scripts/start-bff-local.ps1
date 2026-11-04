# BFF local para React (localhost:8080). Entra desde variables de proceso o .env cargado antes.
$ErrorActionPreference = "Stop"
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$Jar = Join-Path $RepoRoot "infra\out\bff.jar"
$Log = Join-Path $RepoRoot "infra\out\logs\bff-local.log"

if (-not $env:ENTRA_ISSUER_URI) {
    throw "Define ENTRA_ISSUER_URI (tenant del equipo) antes de ejecutar este script."
}
if (-not $env:ENTRA_AUDIENCE) {
    throw "Define ENTRA_AUDIENCE (API client id) antes de ejecutar este script."
}
if (-not (Test-Path $Jar)) {
    throw "Falta $Jar - ejecuta .\infra\scripts\build-jars.ps1"
}

Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
    ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }

Write-Host "Iniciando BFF en :8080 (log: $Log)"
$errLog = Join-Path $RepoRoot "infra\out\logs\bff-local.err.log"
$javaCmd = "java -jar `"$Jar`""
Start-Process -FilePath "cmd.exe" -WorkingDirectory $RepoRoot -WindowStyle Hidden `
    -ArgumentList @("/c", "$javaCmd 1>> `"$Log`" 2>> `"$errLog`"")

Start-Sleep -Seconds 12
curl.exe -s -o NUL -w "public/hola HTTP %{http_code}`n" http://127.0.0.1:8080/public/hola
