import React from "react";
import ReactDOM from "react-dom/client";
import { PublicClientApplication } from "@azure/msal-browser";
import { MsalProvider } from "@azure/msal-react";
import "bootstrap/dist/css/bootstrap.min.css";
import "./App.css";
import { entraConfigurado, msalConfig } from "./authConfig";
import App from "./App";
import VistaPrevia from "./VistaPrevia";

const root = ReactDOM.createRoot(document.getElementById("root"));

if (entraConfigurado) {
  const msalInstance = new PublicClientApplication(msalConfig);
  root.render(
    <React.StrictMode>
      <MsalProvider instance={msalInstance}>
        <App />
      </MsalProvider>
    </React.StrictMode>
  );
} else {
  root.render(
    <React.StrictMode>
      <VistaPrevia />
    </React.StrictMode>
  );
}
