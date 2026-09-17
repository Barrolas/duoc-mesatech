const tenantId = process.env.REACT_APP_ENTRA_TENANT_ID;
const clientId = process.env.REACT_APP_ENTRA_CLIENT_ID;
const apiScope = process.env.REACT_APP_ENTRA_API_SCOPE;

export const msalConfig = {
  auth: {
    clientId,
    authority: `https://login.microsoftonline.com/${tenantId}`,
    redirectUri: window.location.origin + "/",
  },
  cache: {
    cacheLocation: "sessionStorage",
  },
};

export const loginRequest = {
  scopes: ["openid", "profile", "email"],
};

export const apiRequest = {
  scopes: apiScope ? [apiScope] : [],
};

export const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

export const entraConfigurado = Boolean(clientId && tenantId);
