# Evidencias Ari — EP3

**Archivo de capturas:** [`Evidencia EP3 - Ari.docx`](Evidencia%20EP3%20-%20Ari.docx)  
**Actualizado:** 2026-11-28 (America/Santiago)

| ID | Qué capturar | Estado | Dónde está la evidencia |
| --- | --- | --- | --- |
| ARI3-01 | EIP asociadas a las 4 EC2 | **Cubierto** | Word: asignación de las 4 EIP y listado de instancias |
| ARI3-02 | SG (matriz de puertos) | **Cubierto** | Word: reglas de entrada de A, B, C, D y de los 2 RDS |
| ARI3-03 | Rabbit `docker compose ps` + binding | **Cubierto** | Word: `docker compose ps`, `rabbitmqctl list_bindings -p mesatech` y UI (`catalogo.exchange` → `solicitudes.catalogo.queue`, routing key `categoria.creada`) |
| ARI3-04 | Kafka compose healthy + tópicos con 3 particiones | **Cubierto** | Word: `docker compose ps` en D y `kafka-topics --describe` (`PartitionCount: 3`, RF=1) |
| ARI3-05 | RDS sin acceso público | **Cubierto** | Word: ambos RDS con «Accesible públicamente: No», SG y endpoints |
| ARI3-L01 | Consumer group lag (`describe`) para el informe | **Pendiente** | Se hace con Skarlett en la prueba de lag (antes, durante y después; concurrencia 1 y 3) |
