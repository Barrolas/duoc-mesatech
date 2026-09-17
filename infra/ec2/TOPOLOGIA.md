# Topología EC2 — decisión del equipo

**Opción elegida:** una sola instancia EC2 (Ubuntu 22.04 LTS) ejecutando:

- Contenedor Docker **PostgreSQL 16** (puerto 5432 enlazado a `127.0.0.1`).
- Procesos JVM: **BFF** (8080), **ms-solicitudes** (8081), **ms-catalogo** (8082).

**Justificación EV1:** costo y simplicidad operativa; cumple la rúbrica (BFF + 2 MS + persistencia en EC2) sin VPC Link ni balanceadores. Los microservicios no se publican a Internet; el único backend HTTP expuesto hacia el Gateway es el BFF en 8080, con JWT validado en Gateway y en BFF.

**Alternativa descartada para EV1:** tres instancias EC2 (sobrecarga de red y SG para el plazo del curso).
