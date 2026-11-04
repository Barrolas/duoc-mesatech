# Despliega ms-catalogo.jar + ms-catalogo.env en EC2 A (EP2).
# Uso:
#   .\infra\scripts\deploy-ms-catalogo-ep2.ps1 -PemPath "C:\ruta\mesatech-ep2.pem"
param(
    [Parameter(Mandatory = $true)]
    [string] $PemPath,
    [string] $HostIp = "3.85.18.107",
    [string] $User = "ubuntu"
)

$ErrorActionPreference = "Stop"
$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$Jar = Join-Path $RepoRoot "infra\out\ms-catalogo.jar"
$EnvFile = Join-Path $RepoRoot "infra\ec2\ms-catalogo.env"

if (-not (Test-Path $PemPath)) { throw "No existe PEM: $PemPath" }
if (-not (Test-Path $Jar)) {
    Write-Host "Compilando JARs..."
    & (Join-Path $PSScriptRoot "build-jars.ps1")
}
if (-not (Test-Path $EnvFile)) { throw "Falta $EnvFile (credenciales EP2)" }

$remote = "${User}@${HostIp}"
Write-Host ">>> Subiendo a $remote ..."
ssh -i $PemPath -o StrictHostKeyChecking=accept-new $remote "mkdir -p ~/apps"
scp -i $PemPath $Jar "${remote}:~/apps/ms-catalogo.jar"
scp -i $PemPath $EnvFile "${remote}:~/apps/ms-catalogo.env"
ssh -i $PemPath $remote "sed -i 's/\r$//' ~/apps/ms-catalogo.env 2>/dev/null || sed -i '' 's/\r$//' ~/apps/ms-catalogo.env"

Write-Host ">>> Reiniciando ms-catalogo ..."
$startScript = Join-Path $RepoRoot "infra\ec2\start-ms-catalogo.sh"
if (Test-Path $startScript) {
    scp -i $PemPath $startScript "${remote}:~/apps/start-ms-catalogo.sh"
    ssh -i $PemPath $remote "chmod +x ~/apps/start-ms-catalogo.sh && bash ~/apps/start-ms-catalogo.sh"
} else {
    # Una sola línea evita CRLF de PowerShell al pasar el comando por SSH.
    ssh -i $PemPath $remote "bash -lc 'set -a && source ~/apps/ms-catalogo.env && set +a && pkill -f ms-catalogo.jar 2>/dev/null || true; sleep 1; nohup java -jar ~/apps/ms-catalogo.jar > ~/apps/ms-catalogo.log 2>&1 & sleep 2; tail -n 30 ~/apps/ms-catalogo.log'"
}
Write-Host "Listo. Probar: curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8082/v1/catalogo (con JWT si aplica)."
