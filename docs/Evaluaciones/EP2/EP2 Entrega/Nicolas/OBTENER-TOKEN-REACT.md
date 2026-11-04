# Obtener Access Token (Opción B) — solo en tu PC

1. Con React en `http://localhost:3000`, **Iniciar sesión** con Entra (admin demo: `administrador@estqbanquito2.onmicrosoft.com`).
2. Espera a que la app cargue (puede mostrar error de backend; **igual** MSAL guarda el token API).
3. `F12` → **Consola** → pega y Enter:

```javascript
(function () {
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i);
    if (!key.toLowerCase().includes("accesstoken")) continue;
    try {
      const obj = JSON.parse(sessionStorage.getItem(key));
      if (
        obj.credentialType === "AccessToken" &&
        obj.target &&
        obj.target.includes("access_as_user")
      ) {
        console.log("TOKEN (copia solo esto, no subas a Git):");
        console.log(obj.secret);
        return obj.secret;
      }
    } catch (_) {}
  }
  console.warn("No hay token API. Cierra sesión, vuelve a entrar y espera 5 s.");
  return null;
})();
```

4. Copia el JWT (`eyJ...`) y ejecuta:

```powershell
.\infra\scripts\probar-publicador-catalogo-ep2.ps1 -PemPath "E:\DOWNLOADS\mesatech-ep2.pem"
```

No commitees el token ni capturas con el Bearer completo.
