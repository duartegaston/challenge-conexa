const TOKEN_KEY = "conexa_access_token";
const USER_KEY = "conexa_username";

function getToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

function getUsername() {
  return sessionStorage.getItem(USER_KEY);
}

function saveSession(token, username) {
  sessionStorage.setItem(TOKEN_KEY, token);
  sessionStorage.setItem(USER_KEY, username);
}

function clearSession() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_KEY);
}

async function apiFetch(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (!headers.has("Content-Type") && options.body) {
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
    throw new Error("Unauthorized");
  }
  return response;
}
