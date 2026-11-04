# EP2 Entrega — Nicolas

Publicador en `ms-catalogo`, RDS Catálogo, despliegue en EC2 A.

## Variables (sin secretos en Git)

- Local: `.env` raíz (Docker).
- AWS EC2 A (`3.87.142.122`): `infra/ec2/ms-catalogo.env` → copiar a `~/apps/ms-catalogo.env` en la instancia.

## Despliegue rápido

```powershell
.\infra\scripts\deploy-ms-catalogo-ep2.ps1 -PemPath "C:\ruta\mesatech-ep2.pem"
# Manual: build-jars.ps1 + scp JAR y ms-catalogo.env a ubuntu@3.87.142.122:~/apps/
```

En EC2 A:

```bash
set -a && source ~/apps/ms-catalogo.env && set +a
nohup java -jar ~/apps/ms-catalogo.jar > ~/apps/ms-catalogo.log 2>&1 &
```

## Coordinación Skarlett

Consumidor en rama `feature/skarlett-consumidor`. Mismo JSON (`CategoriaCreadaEvent`). Tras merge de ambas ramas a `dev`, probar P1–P8.

Capturas en `Evidencias/`.
