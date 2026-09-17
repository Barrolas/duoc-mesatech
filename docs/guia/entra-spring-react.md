# Configuración Cloud + Backend

**Spring Security y OAuth2 Resource Server · Microsoft Entra ID (IDaaS)**

> Hoja de ruta por fases: `[instructivo-maestro-ev1.md](instructivo-maestro-ev1.md)`. Índice: `[../README.md](../README.md)`.

Guía paso a paso reconstruida desde la PPT del profesor y **cada captura de pantalla**. Esta es la configuración de identidad que usará MesaTech Cloud: **Microsoft Entra ID de Azure**, no Cognito.

Esta PPT entrega las bases para estudiar lo presentado y aplicarlo en el proyecto a evaluar.

> Los ID de inquilino y de aplicación de las capturas son del tenant del profesor. En el proyecto hay que registrar **los propios** y reemplazarlos en React, Spring Boot y API Gateway.

Hay **dos registros de aplicación** en el mismo tenant:


| Registro                                     | Para qué sirve                                                      |
| -------------------------------------------- | ------------------------------------------------------------------- |
| SPA React (ya existe, ej. `Login con React`) | Login/logout con MSAL. Pide el Access Token.                        |
| API (`api-cloud-native`)                     | Expone el scope. Es el **audience** del JWT que valida Spring Boot. |


Repositorios de ejemplo (hay que poner el ID de inquilino y el de aplicación del equipo):

- Backend: [https://github.com/pauloberrios44/backend-springboot-jwt.git](https://github.com/pauloberrios44/backend-springboot-jwt.git)
- Front: [https://github.com/pauloberrios44/dfs-s3-login-1.git](https://github.com/pauloberrios44/dfs-s3-login-1.git)

---



## Resumen de lo que hay que lograr

1. En el tenant, registrar una nueva aplicación web llamada `api-cloud-native`.
2. Exponer un ámbito `access_as_user` y dejar el access token en **versión 2**.
3. Dar permiso y consentimiento para que la SPA React pueda pedir ese scope.
4. Crear un Spring Boot Resource Server que valide `iss` y `aud` del JWT de Entra ID.
5. En React, pedir el token con MSAL y llamar a la API con Axios (`Authorization: Bearer`).

---



# Parte A — Microsoft Entra ID (portal de Azure)

Portal: [https://portal.azure.com](https://portal.azure.com)  
Ruta habitual: **Microsoft Entra ID → Registros de aplicaciones**.

---



## Paso 1. Abrir Registros de aplicaciones

![Paso 1](../assets/pptx/slide03_img01.png)

1. Entrar al [portal de Azure](https://portal.azure.com) con la cuenta del tenant del equipo.
2. Abrir **Microsoft Entra ID** (identidad de la organización).
3. En el menú izquierdo, ir a **Registros de aplicaciones**.
4. Comprobar que ya existe el registro de la SPA (en la captura aparece `Login con React`). **No se reemplaza**: se crea uno nuevo para la API.
5. Anotar, si hace falta, el **Id. de aplicación (cliente)** de la SPA. Más adelante esa SPA pedirá permiso sobre `api-cloud-native`.

---



## Paso 2. Registrar la aplicación `api-cloud-native`

![Paso 2](../assets/pptx/slide04_img02.png)

1. En **Registros de aplicaciones**, pulsar **Nuevo registro**.
2. Completar el formulario **Registrar una aplicación**:
  1. **Nombre:** `api-cloud-native`.
  2. **Tipos de cuenta compatibles:** *Solo cuentas de este directorio organizativo* (un solo inquilino).
  3. **URI de redirección:** dejarlo vacío. Esta app es la API, no el login del navegador. La redirect URI vive en el registro de React.
3. Pulsar **Registrar**.
4. Al terminar, Azure abre la ficha de `api-cloud-native`. Copiar y guardar:
  - **Id. de aplicación (cliente)** → será el `aud` / audience.
  - **Id. de directorio (inquilino)** → irá en el `issuer-uri`.

---



## Paso 3. Abrir “Exponer una API”

![Paso 3](../assets/pptx/slide05_img03.png)

1. Con `api-cloud-native` abierto, revisar la hoja **Información general** (ahí están cliente, objeto e inquilino).
2. En el menú izquierdo, dentro de **Administrar**, entrar a **Exponer una API**.
3. Todavía no hay URI de aplicación ni ámbitos. El siguiente paso crea el Application ID URI.

---



## Paso 4. Crear el URI de id. de aplicación

![Paso 4](../assets/pptx/slide06_img04.png)

1. En **Exponer una API**, buscar **URI de id. de aplicación**.
2. Pulsar **Agregar**.
3. Aceptar el valor propuesto, del tipo:
  `api://<ID_DE_APLICACION_CLIENTE>`
4. **Guardar**.
5. Ese URI es el prefijo de los scopes. El scope completo quedará:
  `api://<ID_DE_APLICACION_CLIENTE>/access_as_user`

---



## Paso 5. Crear el ámbito `access_as_user`



### 5.1 Abrir el formulario

![Paso 5.1](../assets/pptx/slide07_img05.png)

1. En la misma pantalla **Exponer una API**, comprobar que el URI ya quedó (`api://...`).
2. En **Ámbitos definidos por esta API**, pulsar **+ Agregar un ámbito**.
3. La lista debe decir *No se definió ningún ámbito* hasta completar el formulario.



### 5.2 Completar el ámbito y agregarlo

![Paso 5.2](../assets/pptx/slide08_img06.png)

1. Rellenar **Agregar un ámbito** así:

  | Campo                                                        | Valor a usar                       |
  | ------------------------------------------------------------ | ---------------------------------- |
  | **Nombre de ámbito**                                         | `access_as_user`                   |
  | URI resultante (solo lectura)                                | `api://<CLIENT_ID>/access_as_user` |
  | **¿Quién puede dar el consentimiento?**                      | **Administradores y usuarios**     |
  | **Nombre para mostrar del consentimiento del administrador** | `acceso de usuario`                |
  | **Descripción del consentimiento del administrador**         | `acceso de usuario`                |
  | **Nombre para mostrar del consentimiento del usuario**       | `acceso de usuario`                |
  | **Descripción del consentimiento del usuario**               | `acceso de usuario`                |
  | **Estado**                                                   | Habilitado                         |

2. Pulsar **Agregar ámbito**.
3. Volver a **Exponer una API** y verificar que el ámbito `access_as_user` aparece en la lista.

Este claim llega en el JWT como `"scp": "access_as_user"`. Spring lo mapea a `SCOPE_access_as_user`.

---



## Paso 6. Agregarse como propietario de la API



### 6.1 Ir a Propietarios

![Paso 6.1](../assets/pptx/slide09_img07.png)

1. En el menú de `api-cloud-native`, abrir **Propietarios**.
2. Si dice *No se ha asignado ningún propietario*, pulsar **Agregar propietarios**.



### 6.2 Elegir la cuenta principal

![Paso 6.2](../assets/pptx/slide10_img08.png)

1. En el panel **Propietarios**, buscar la cuenta **principal del equipo** (la del tenant / correo institucional).
2. Marcar el checkbox de esa cuenta. Queda en **Seleccionado (1)**.
3. Pulsar **Seleccionar**.



### 6.3 Confirmar

![Paso 6.3](../assets/pptx/slide11_img09.png)

1. Debe aparecer el aviso **Propietarios actualizados** / *el propietario se ha agregado correctamente*.
2. La tabla lista nombre, correo y tipo (Member).
3. Con esto el equipo puede editar el registro, el manifiesto y los permisos.

---



## Paso 7. Dar permiso a la API expuesta para recibir JWT

El registro `api-cloud-native` debe poder usar el scope que ella misma expone (permiso delegado). Luego se hace lo mismo en la SPA (paso 10).

### 7.1 Abrir Permisos de API

![Paso 7.1](../assets/pptx/slide12_img10.png)

1. En `api-cloud-native`, menú izquierdo → **Permisos de API**.
2. Puede aparecer solo **Microsoft Graph → User.Read** (delegado).
3. Pulsar **+ Agregar un permiso**.



### 7.2 Elegir “Mis API”

![Paso 7.2](../assets/pptx/slide13_img11.png)

1. En **Solicitud de permisos de API**, ir a la pestaña **Mis API**.
2. Seleccionar `api-cloud-native`.
3. Se ve el **Id. de aplicación (cliente)** de la API.



### 7.3 Tipo de permiso: delegados

![Paso 7.3](../assets/pptx/slide14_img12.png)

1. En **¿Qué tipo de permiso necesita la aplicación?**, elegir **Permisos delegados**.
2. Significa: la app accede a la API **como el usuario que inició sesión** (no como daemon).
3. No usar *Permisos de aplicación* para este ejercicio.



### 7.4 Marcar `access_as_user` y agregar

![Paso 7.4](../assets/pptx/slide15_img13.png)

1. En **Seleccionar permisos**, expandir **Permisos**.
2. Marcar el checkbox de `access_as_user` (descripción: *acceso de usuario*).
3. **Se necesita el consentimiento del administrador** = No (igual hay que otorgarlo en el siguiente paso).
4. Pulsar **Agregar permisos**.

---



## Paso 8. Activar el consentimiento de administrador



### 8.1 Conceder consentimiento

![Paso 8.1](../assets/pptx/slide16_img14.png)

1. Volver a `api-cloud-native` **→ Permisos de API**.
2. En la lista debe aparecer:
  - `api-cloud-native` → `access_as_user` (delegada)
  - `Microsoft Graph` → `User.Read` (delegada)
3. Pulsar **Conceder consentimiento de administrador para organización**.
4. Confirmar el cuadro de diálogo.



### 8.2 Verificar estado “Concedido”

![Paso 8.2](../assets/pptx/slide17_img15.png)

1. Sale el aviso **Otorgar consentimiento** / *el consentimiento se ha otorgado correctamente*.
2. En la columna **Estado**, ambos permisos quedan con el tilde verde **Concedido para organización**.
3. Sin este tilde, MSAL o el usuario se quedan pidiendo consentimiento y el token no sale limpio.

---



## Paso 9. Cambiar el access token a versión 2 (manifiesto)

Spring Boot de esta guía valida tokens **v2.0**. Si `requestedAccessTokenVersion` queda en `null`, el token no calza con el `issuer-uri` `.../v2.0`.

### 9.1 Editar el manifiesto

![Paso 9.1](../assets/pptx/slide18_img16.png)

1. En `api-cloud-native`, menú izquierdo → **Manifiesto**.
2. Usar la pestaña **Manifiesto de aplicación de Microsoft Graph** (el nuevo).
3. Buscar, dentro de `"api"`, la clave `requestedAccessTokenVersion`.
4. Cambiar el valor de `null` a `2`:
  ```json
   "requestedAccessTokenVersion": 2
  ```
5. Pulsar **Guardar**.



### 9.2 Confirmar el manifiesto

![Paso 9.2](../assets/pptx/slide19_img17.png)

1. Debe aparecer **Actualizar el manifiesto de aplicación** / *se ha actualizado correctamente*.
2. Recargar y comprobar que sigue en `2` (no volvió a `null`).

---



## Paso 10. Permisos también en la SPA de React

La API ya expone el scope. Ahora la **aplicación React** tiene que estar autorizada a pedirlo. En la captura el registro se llama `Login con una aplicación React DEMO`.

![Paso 10](../assets/pptx/slide20_img18.png)

1. Volver a **Entra ID → Registros de aplicaciones**.
2. Abrir el registro de la **SPA React** (no `api-cloud-native`).
3. Ir a **Permisos de API**.
4. **+ Agregar un permiso** → pestaña **Mis API** → `api-cloud-native`.
5. **Permisos delegados** → marcar `access_as_user` → **Agregar permisos**.
6. Pulsar **Conceder consentimiento de administrador para organización**.
7. Verificar tilde verde:
  - `api-cloud-native` / `access_as_user` → Concedido
  - `Microsoft Graph` / `User.Read` → Concedido

Sin este paso, React pide un scope que la SPA no tiene asignado y no obtiene Access Token usable.

---



## Paso 11. Rechequear token v2 en `api-cloud-native`

![Paso 11](../assets/pptx/slide21_img19.png)

1. Reabrir `api-cloud-native` **→ Manifiesto**.
2. Confirmar otra vez:
  ```json
   "requestedAccessTokenVersion": 2
  ```
3. El `issuer` del JWT quedará:
  `https://login.microsoftonline.com/<TENANT_ID>/v2.0`

---



## Checkpoint Entra ID

Con los pasos 1–11 ya se puede usar el **Id. de aplicación** de la API en React y en Spring Boot.

- El Spring Boot usa **token versión 2.0** → el manifiesto **tiene** que estar en `2`.
- Guardar en un lugar seguro (no en git):

  | Dato                            | Dónde se usa                                             |
  | ------------------------------- | -------------------------------------------------------- |
  | Tenant ID (Id. de directorio)   | `issuer-uri`, MSAL `authority`                           |
  | Client ID de la **SPA**         | MSAL `clientId`                                          |
  | Client ID de `api-cloud-native` | `audiences` en Spring y scope `api://.../access_as_user` |
  | Redirect URI de la SPA          | Registro de React (no de la API)                         |


---



# Parte B — Spring Boot (Resource Server)



## Paso 12. Crear el proyecto en Spring Initializr

![Paso 12](../assets/pptx/slide23_img20.png)

1. Ir a [https://start.spring.io](https://start.spring.io).
2. Configurar el proyecto:

  | Campo         | Valor de la captura                                      |
  | ------------- | -------------------------------------------------------- |
  | Project       | **Maven**                                                |
  | Language      | **Java**                                                 |
  | Spring Boot   | la estable que muestre el Initializr (en la PPT: 4.1.1)  |
  | Group         | `cl.duoc`                                                |
  | Artifact      | `api-cloud-native`                                       |
  | Package name  | `cl.duoc.apicloudnative` (o el que genere el Initializr) |
  | Packaging     | **Jar**                                                  |
  | Configuration | **Properties** (no YAML)                                 |
  | Java          | **17**                                                   |

3. **Add Dependencies** y agregar exactamente:
  - **Spring Web**
  - **MySQL Driver** (ejemplo de la PPT; en MesaTech usamos **PostgreSQL**)
  - **Spring Data JPA**
  - **Lombok**
  - **Spring Security**
  - **OAuth2 Resource Server** ← obligatorio para validar el JWT de Entra ID
4. **Generate**, descomprimir y abrir el proyecto en el IDE.

La PPT de clases usa **MySQL Driver**. En MesaTech la persistencia es **PostgreSQL 16 en Docker** (`infra/`), subida a EC2. El Resource Server (JWT) no cambia: solo el conector JPA.

---



## Paso 13. `application.properties` (iss y aud)

Spring verifica dos claims del JWT:

```text
iss  →  token emitido por nuestro tenant Entra ID
aud  →  token destinado a api-cloud-native
```

```properties
server.port=8080

# Id. de directorio (inquilino) — el del equipo, no el de la PPT
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/<TENANT_ID>/v2.0

# Id. de aplicación (cliente) de api-cloud-native (Exponer una API)
spring.security.oauth2.resourceserver.jwt.audiences=<API_CLIENT_ID>
```

1. Reemplazar `<TENANT_ID>` por **Id. de directorio** de Entra ID.
2. Reemplazar `<API_CLIENT_ID>` por el **Id. de aplicación** de `api-cloud-native` (no el de React).
3. El `/v2.0` del issuer tiene que coincidir con `requestedAccessTokenVersion: 2`.

Documento de persistencia de ejemplo del profesor (clases usan MySQL; MesaTech usa PostgreSQL):

[https://docs.google.com/document/d/1GCu7jJi-_xCzwbUTPQzGzqG9ETramHho1bvVawHBW0I/edit?usp=sharing](https://docs.google.com/document/d/1GCu7jJi-_xCzwbUTPQzGzqG9ETramHho1bvVawHBW0I/edit?usp=sharing)

Valores **solo de referencia** que aparecen en la PPT (no copiar al repo del equipo):

- tenant: `ba9c3522-adab-4c57-ac78-dc6131594e07`
- audience: `c639b2a3-10c3-4611-b3c8-8acb71c2afc6`

---



## Paso 14. Crear `SecurityConfig.java`

1. Crear el package `config` (en la PPT: `Config`).
2. Crear la clase `SecurityConfig.java`.
3. Partir del código del profesor:
  [https://docs.google.com/document/d/13jhQRcs3-ZLInbuYMxPujhQXx9PykdXE-Oe1XsBHjRI/edit?usp=sharing](https://docs.google.com/document/d/13jhQRcs3-ZLInbuYMxPujhQXx9PykdXE-Oe1XsBHjRI/edit?usp=sharing)

El JWT de Entra ID trae:

```json
{ "scp": "access_as_user" }
```

Spring Security lo convierte en la authority `SCOPE_access_as_user`.

![Paso 14](../assets/pptx/slide25_img21.png)

Idea de la captura (rutas `/api/**` protegidas):

```java
.requestMatchers("/api/**").hasAuthority("SCOPE_access_as_user")
```

Subpasos de la config mínima:

1. Declarar la clase `@Configuration` / `@EnableWebSecurity`.
2. Exponer un `SecurityFilterChain`.
3. Dejar públicas las rutas de prueba (`/public/**`).
4. Exigir JWT + `SCOPE_access_as_user` en `/api/**`.
5. Activar `oauth2ResourceServer(jwt -> ...)`.
6. Issuer y audience salen de `application.properties` (paso 13).

---



## Paso 15. Controllers: uno público y uno protegido

1. Crear `PublicController`:
  - Ruta: `GET /public/hola`
  - Respuesta: `Map<String, String>`
  - Sin JWT.
2. Crear `UsuarioController`:
  - Ruta: `GET /api/usuario` (o la que usen)
  - **Protegida** por el `SecurityConfig`.
  - Puede leer claims del `Jwt` (nombre, upn, scp, oid, roles).
3. Código de ejemplo del profesor:
  [https://docs.google.com/document/d/1JHuMEo99-aJNk1paKJt3iVwkEuYk-Ja1u3LnpSS3yPM/edit?usp=sharing](https://docs.google.com/document/d/1JHuMEo99-aJNk1paKJt3iVwkEuYk-Ja1u3LnpSS3yPM/edit?usp=sharing)

En MesaTech este patrón se replica en el **BFF**: público solo lo mínimo; `/v1/`** y `/v2/`** detrás de JWT. El frontend no pega a este host: pega a **API Gateway**.

---



# Parte C — React (MSAL + Axios)



## Paso 16. Instalar Axios

```bash
npm install axios
```

También deben estar `@azure/msal-browser` y `@azure/msal-react` (ejercicio de clases).

---



## Paso 17. Pedir el scope de la API en `authConfig.js`

1. Abrir `authConfig.js` (o el archivo de configuración MSAL del front).
2. Dejar `clientId` = Id. de la **SPA**.
3. Dejar `authority` = `https://login.microsoftonline.com/<TENANT_ID>`.
4. Agregar el request de la API expuesta (Client ID de `api-cloud-native`):

```js
export const apiRequest = {
  scopes: [
    "api://<API_CLIENT_ID>/access_as_user",
  ],
};
```

1. Ese string tiene que ser **idéntico** al ámbito de **Exponer una API**.

---



## Paso 18. Obtener el token y llamar a la API

1. Usar `useMsal()` para leer `instance` y `accounts`.
2. Con `useEffect`, cuando exista cuenta autenticada:
  1. Llamar `instance.acquireTokenSilent({ ...apiRequest, account })`.
  2. Guardar el `accessToken` en estado (`useState`).
3. Con Axios, invocar la API **a través de API Gateway**:
  ```js
   axios.get(urlDelGateway, {
     headers: { Authorization: `Bearer ${accessToken}` },
   });
  ```
4. Guardar en estado la respuesta (datos de Entra ID y de la API).
5. Mostrar nombre, correo y al menos un claim del token (exigencia de la EP1).

---



## Paso 19. Flujo que ejecuta lo anterior

![Paso 19](../assets/pptx/slide28_img22.png)

1. El usuario abre React e inicia sesión con **Microsoft Entra ID**.
2. MSAL deja el usuario autenticado en `accounts`.
3. `useEffect` detecta el cambio y dispara la siguiente acción.
4. `acquireTokenSilent()` pide el Access Token (sin popup, si la sesión sigue viva).
5. Queda el access token con audience `api-cloud-native` y `scp=access_as_user`.
6. `axios.get(...)` envía `Authorization: Bearer <access_token>` al **API Gateway**.
7. Gateway valida el JWT; el BFF Spring Boot lo vuelve a validar, autoriza y responde.

Si se llama **directo a EC2** (sin Gateway) → **401**.

---



## Código en GitHub

Front de ejemplo:

[https://github.com/pauloberrios44/dfs-s3-login-1](https://github.com/pauloberrios44/dfs-s3-login-1)

![QR al repositorio](../assets/pptx/slide29_img23.png)

---



## Checklist rápido (Entra ID + backend)

- [x] Existe el registro SPA (React + MSAL + redirect URI).
- [x] Existe el registro `api-cloud-native`.
- [x] Application ID URI: `api://<API_CLIENT_ID>`.
- [x] Ámbito `access_as_user` creado (admin + usuarios).
- [x] Hay un propietario del registro API.
- [x] Permiso delegado `access_as_user` en la API **y** en la SPA.
- [x] Consentimiento de administrador concedido (tilde verde).
- [x] Manifiesto: `requestedAccessTokenVersion` = **2**.
- [ ] Spring Boot con OAuth2 Resource Server; `issuer-uri` y `audiences` del equipo.
- [ ] `/api/**` exige `SCOPE_access_as_user`.
- [ ] React pide `api://<API_CLIENT_ID>/access_as_user` y manda el Bearer al Gateway.