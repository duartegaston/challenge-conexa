import { errorMessage, getToken, saveSession } from "/js/auth.js";

if (getToken()) {
  window.location.href = "/app.html";
}

const form = document.getElementById("login-form");
const errorEl = document.getElementById("login-error");
const button = document.getElementById("login-btn");
const toggle = document.getElementById("toggle-mode");
const toggleText = document.getElementById("toggle-text");
const passwordInput = document.getElementById("password");

let registerMode = false;

toggle.addEventListener("click", () => {
  registerMode = !registerMode;
  button.textContent = registerMode ? "Crear cuenta" : "Entrar";
  toggleText.textContent = registerMode ? "¿Ya tenés cuenta?" : "¿No tenés cuenta?";
  toggle.textContent = registerMode ? "Entrar" : "Crear una";
  passwordInput.autocomplete = registerMode ? "new-password" : "current-password";
  errorEl.textContent = "";
});

async function post(path, payload) {
  return fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  errorEl.textContent = "";
  button.disabled = true;

  const username = document.getElementById("username").value.trim();
  const password = passwordInput.value;

  try {
    if (registerMode) {
      const created = await post("/api/v1/auth/register", { username, password });
      if (!created.ok) {
        throw new Error(await errorMessage(created, "No se pudo crear la cuenta"));
      }
    }
    const response = await post("/api/v1/auth/login", { username, password });
    if (!response.ok) {
      throw new Error(await errorMessage(response, "No se pudo iniciar sesión"));
    }
    saveSession(await response.json(), username.toLowerCase());
    window.location.href = "/app.html";
  } catch (err) {
    errorEl.textContent = err.message || "Error de autenticación";
  } finally {
    button.disabled = false;
  }
});
