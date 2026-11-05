# Dónde va cada `.env` — EP2 (sin secretos en Git)

Los archivos con **contraseñas reales** están en `.gitignore`. En el repo solo existe **`env.example`** (plantilla ficticia).

## Mapa de archivos (repo local de Nicolás)

| Archivo | ¿Tiene claves AWS? | Quién lo usa |
| --- | --- | --- |
| `.env` (raíz del monorepo) | No (local Docker) | Desarrollo en PC: BFF, MS, React vía compose/scripts |
| `frontend/.env` | No | Solo **React** (MSAL + URL API) |
| `infra/ec2/ms-catalogo.env` | **Sí** | **Nicolás** — JAR `ms-catalogo` en **EC2 A** |
| `infra/ec2/ms-solicitudes.env` | **Sí** | **Skarlett** — JAR `ms-solicitudes` en **EC2 B** |
| `env.example` | No | Referencia para todos (versionado en Git) |

Ruta raíz del repo en PC de Nico (ejemplo):

`e:\CODE\LEARNING\DUOC - INGENIERIA EN INFORMATICA\Cloud Native\duoc-mesatech\`

---

## 1. Desarrollo en tu PC (local)

### `.env` → raíz del repo

**Ruta:** `duoc-mesatech\.env`

**Para qué:** Postgres y Rabbit en **localhost** (`infra/docker-compose`), BFF en `:8080`, MS en `:8081`/`:8082`.

**Cómo usarlo:** Spring **no lee** `.env` solo. Opciones:

- Exportar variables antes de arrancar (PowerShell): cargar manualmente o script del equipo.
- IDE: Run Configuration con env file apuntando a `.env`.
- Scripts `infra/scripts/run-local-stack.ps1` (si el equipo los usa con env cargado).

### `frontend/.env` → carpeta React

**Ruta:** `duoc-mesatech\frontend\.env`

**Para qué:** Create React App (`REACT_APP_*`). Debe tener **valores literales** (no `${ENTRA_TENANT_ID}`).

**Cuándo cambiar:** Demo cloud → `REACT_APP_API_BASE_URL` = Invoke URL del API Gateway (ver `.env-v2` o diagrama EP1).

---

## 2. Cloud AWS — EC2 A (Catálogo + publicador)

| Dato | Valor |
| --- | --- |
| Rol | EC2 A · `ms-catalogo` |
| SSH (pública) | `ubuntu@3.85.18.107` |
| Archivo en PC | `infra/ec2/ms-catalogo.env` |
| **Destino en servidor** | **`/home/ubuntu/apps/ms-catalogo.env`** |

**Pasos:**

1. Compilar: `.\infra\scripts\build-jars.ps1` → `infra/out/ms-catalogo.jar`
2. Copiar:
   ```powershell
   scp -i <mesatech-ep2.pem> infra/out/ms-catalogo.jar ubuntu@3.85.18.107:~/apps/
   scp -i <mesatech-ep2.pem> infra/ec2/ms-catalogo.env ubuntu@3.85.18.107:~/apps/ms-catalogo.env
   ```
3. En la EC2:
   ```bash
   set -a && source ~/apps/ms-catalogo.env && set +a
   nohup java -jar ~/apps/ms-catalogo.jar > ~/apps/ms-catalogo.log 2>&1 &
   ```

**Variables clave (nombres, no valores):** `CATALOGO_DATASOURCE_URL`, `POSTGRES_*`, `RABBITMQ_*` (host **privado** broker), `ENTRA_*`.

---

## 3. Cloud AWS — EC2 B (Solicitudes + consumidor)

| Dato | Valor |
| --- | --- |
| Rol | EC2 B · `ms-solicitudes` |
| SSH (pública) | `ubuntu@98.91.199.82` |
| Archivo en PC | `infra/ec2/ms-solicitudes.env` |
| **Destino en servidor** | **`/home/ubuntu/apps/ms-solicitudes.env`** |

**Responsable:** Skarlett (misma idea: `source` + `java -jar ms-solicitudes.jar`).

Usuario Rabbit distinto: `solicitudes_consumidor` (no usar el env de Catálogo en B).

---

## 4. Qué NO va en Git

- `.env`, `frontend/.env`, `infra/ec2/ms-catalogo.env`, `infra/ec2/ms-solicitudes.env`
- `claves_rabbitmq.txt` u otros txt con passwords

---

## 5. PR y código

Publicador EP2: PR **#10** → `feature/nico-ec2-publicador` → `dev`.

Consumidor: rama **`feature/skarlett-consumidor`** (merge pendiente a `dev`).
