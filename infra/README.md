# Infraestructura MesaTech EV1

PostgreSQL 16 en Docker para **local** y **EC2**. Los microservicios se conectan a `localhost:5432`.

| Archivo | Uso |
| --- | --- |
| [`docker-compose.yml`](docker-compose.yml) | Desarrollo local (puerto 5432 publicado en la máquina) |
| [`docker-compose.ec2.yml`](docker-compose.ec2.yml) | EC2: Postgres **solo** en loopback (no expuesto a Internet) |
| [`init.sql`](init.sql) | Crea `mesatech_solicitudes` y `mesatech_catalogo` |
| [`ec2/README.md`](ec2/README.md) | Guía de despliegue Nico (C + D) |
| [`scripts/build-jars.sh`](scripts/build-jars.sh) | Compilar BFF + MS en Linux/macOS/Git Bash |
| [`scripts/build-jars.ps1`](scripts/build-jars.ps1) | Compilar en Windows |

Guía extendida: [`docs/equipo/guias/nico-ec2-seguridad-presentacion.md`](../docs/equipo/guias/nico-ec2-seguridad-presentacion.md).
