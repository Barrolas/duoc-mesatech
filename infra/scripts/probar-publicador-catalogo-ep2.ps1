# Prueba N2: POST categoría en ms-catalogo (EC2 A) con JWT de Entra.
# Uso: .\infra\scripts\probar-publicador-catalogo-ep2.ps1 -PemPath "E:\DOWNLOADS\mesatech-ep2.pem"
param(
    [Parameter(Mandatory = $true)]
    [string] $PemPath,
    [string] $HostIp = "3.87.142.122",
    [string] $User = "ubuntu",
    [string] $NombreCategoria = "Evidencia-Nico-EP2"
)

$ErrorActionPreference = "Stop"
$token = Read-Host "Pega el Access Token (Bearer, sin la palabra Bearer) y Enter"
if ([string]::IsNullOrWhiteSpace($token)) { throw "Token vacío" }
$token = $token.Trim()
if ($token.StartsWith("Bearer ", "OrdinalIgnoreCase")) {
    $token = $token.Substring(7).Trim()
}

$remote = "${User}@${HostIp}"
$body = "{`"nombre`":`"$NombreCategoria`"}"
$escapedBody = $body -replace "'", "'\\''"

Write-Host ">>> POST categoría en EC2 A (localhost:8082) ..."
$curlCmd = @"
curl -s -w '\nHTTP_CODE:%{http_code}\n' \
  -H 'Authorization: Bearer $token' \
  -H 'Content-Type: application/json' \
  -d '$escapedBody' \
  http://127.0.0.1:8082/v1/catalogo/categorias
"@

ssh -i $PemPath $remote $curlCmd

Write-Host "`n>>> Últimas líneas del log (buscar 'Evento' publicado) ..."
ssh -i $PemPath $remote "tail -n 25 ~/apps/ms-catalogo.log | grep -E 'Evento|publicado|ERROR|Exception' || tail -n 15 ~/apps/ms-catalogo.log"

Write-Host "`nListo. Siguiente: capturas NIC-03 y NIC-04 (ver CHECKLIST-NICO.md)."
