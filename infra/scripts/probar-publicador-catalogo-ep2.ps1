# Prueba N2: POST categoría en ms-catalogo (EC2 A) con JWT de Entra.
# Uso: .\infra\scripts\probar-publicador-catalogo-ep2.ps1 -PemPath "E:\DOWNLOADS\mesatech-ep2.pem"
param(
    [Parameter(Mandatory = $true)]
    [string] $PemPath,
    [string] $HostIp = "3.85.18.107",
    [string] $User = "ubuntu",
    [string] $NombreCategoria = "Evidencia-Nico-EP2"
)

$ErrorActionPreference = "Stop"
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$token = Read-Host "Pega el Access Token (Bearer, sin la palabra Bearer) y Enter"
if ([string]::IsNullOrWhiteSpace($token)) { throw "Token vacío" }
$token = $token.Trim()
if ($token.StartsWith("Bearer ", "OrdinalIgnoreCase")) {
    $token = $token.Substring(7).Trim()
}

$remote = "${User}@${HostIp}"
$tokenB64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($token))
$nombreJson = ($NombreCategoria -replace '\\', '\\\\' -replace '"', '\"')

$remoteSh = @"
#!/usr/bin/env bash
set -euo pipefail
TOKEN=`$(echo '$tokenB64' | base64 -d)
curl -s -w '\nHTTP_CODE:%{http_code}\n' \
  -H "Authorization: Bearer `$TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"nombre\":\"$nombreJson\"}" \
  'http://127.0.0.1:8082/v1/catalogo/categorias'
"@

$localSh = Join-Path $RepoRoot "infra\out\post-categoria-remote.sh"
$outDir = Split-Path $localSh -Parent
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$remoteShUnix = ($remoteSh -replace "`r`n", "`n") -replace "`r", ""
[System.IO.File]::WriteAllText($localSh, $remoteShUnix)

Write-Host ">>> POST categoría en EC2 A (localhost:8082) ..."
scp -i $PemPath $localSh "${remote}:/tmp/post-categoria.sh"
ssh -i $PemPath $remote "chmod +x /tmp/post-categoria.sh && bash /tmp/post-categoria.sh; rm -f /tmp/post-categoria.sh"

Write-Host "`n>>> Últimas líneas del log (buscar 'Evento' publicado) ..."
ssh -i $PemPath $remote "tail -n 40 ~/apps/ms-catalogo.log | grep -E 'Evento|publicado|ERROR|Exception' || tail -n 15 ~/apps/ms-catalogo.log"

Write-Host "`nListo. Siguiente: capturas NIC-03 y NIC-04 (ver CHECKLIST-NICO.md)."
