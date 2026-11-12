// Sesión y acceso a la API. sessionStorage: la sesión muere al cerrar la pestaña.
const TOKEN_KEY = "conexa_access_token";
const USER_KEY = "conexa_username";
const ROLES_KEY = "conexa_roles";

export const getToken = () => sessionStorage.getItem(TOKEN_KEY);
export const getUsername = () => sessionStorage.getItem(USER_KEY);
export const hasRole = (role) => (sessionStorage.getItem(ROLES_KEY) || "").split(",").includes(role);

export function saveSession({ accessToken, roles }, username) {
  sessionStorage.setItem(TOKEN_KEY, accessToken);
  sessionStorage.setItem(USER_KEY, username);
  sessionStorage.setItem(ROLES_KEY, (roles || []).join(","));
}

export function clearSession() {
  [TOKEN_KEY, USER_KEY, ROLES_KEY].forEach((key) => sessionStorage.removeItem(key));
}

/** fetch con el JWT; ante un 401 limpia la sesión y vuelve al login. */
export async function apiFetch(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const token = getToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(path, { ...options, headers });
  if (response.status === 401) {
    clearSession();
    window.location.href = "/";
    throw new Error("Sesión expirada");
  }
  return response;
}

/**
 * Mensaje legible de una respuesta de error. La API responde application/problem+json
 * ({ detail, errors }); un proxy (p. ej. cold start de Fly) puede responder HTML, por eso el fallback.
 */
export async function errorMessage(response, fallback) {
  const body = await response.json().catch(() => null);
  if (body?.errors) {
    return Object.entries(body.errors)
      .map(([field, message]) => `${field}: ${message}`)
      .join(" · ");
  }
  return body?.detail || fallback || `Error ${response.status}`;
}
