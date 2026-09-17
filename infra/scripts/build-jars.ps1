# Compila BFF y microservicios. Salida: infra/out/
$ErrorActionPreference = "Stop"
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$OutDir = Join-Path (Join-Path $RepoRoot "infra") "out"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$modules = @("bff", "ms-solicitudes", "ms-catalogo")
foreach ($m in $modules) {
    Write-Host ">>> mvn package -DskipTests ($m)"
    Push-Location (Join-Path $RepoRoot $m)
    mvn -q -DskipTests package
    $jar = Get-ChildItem target\*.jar | Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
    Copy-Item $jar.FullName (Join-Path $OutDir "$m.jar")
    Pop-Location
}
Write-Host "Listo: $OutDir"
