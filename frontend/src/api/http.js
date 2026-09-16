import Axios from "axios";
import { apiBaseUrl, apiRequest } from "../authConfig";

export async function apiGet(instance, account, path) {
  const tokenResponse = await instance.acquireTokenSilent({
    ...apiRequest,
    account,
  });

  const response = await Axios.get(`${apiBaseUrl}${path}`, {
    headers: {
      Authorization: `Bearer ${tokenResponse.accessToken}`,
    },
  });

  return response.data;
}
