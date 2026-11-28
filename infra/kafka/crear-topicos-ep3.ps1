# Crea tópicos EP3 (3 particiones, RF=1) en el broker local.
# Requisito: docker compose -f infra/kafka/compose.yaml up -d
#
#   .\infra\kafka\crear-topicos-ep3.ps1

$ErrorActionPreference = "Stop"

$Container = "mesatech-kafka"
$Bootstrap = "localhost:29092"

$topics = @(
    "mesatech.solicitud.creada",
    "mesatech.solicitud.estado",
    "mesatech.catalogo.prioridad",
    "mesatech.dlq.eventos"
)

$running = docker inspect -f '{{.State.Running}}' $Container 2>$null
if (-not $running -or $running -ne "true") {
    Write-Error "Contenedor '$Container' no está en ejecución. Levante Kafka con: docker compose -f infra/kafka/compose.yaml up -d"
}

foreach ($name in $topics) {
    Write-Host "Tópico: $name (3 particiones)"
    docker exec $Container kafka-topics `
        --bootstrap-server $Bootstrap `
        --create `
        --if-not-exists `
        --topic $name `
        --partitions 3 `
        --replication-factor 1
}

Write-Host ""
Write-Host "Listado:"
docker exec $Container kafka-topics --bootstrap-server $Bootstrap --list