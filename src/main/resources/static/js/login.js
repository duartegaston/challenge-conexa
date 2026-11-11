(() => {
  if (getToken()) {
    window.location.href = "/app.html";
    return;
  }

  const form = document.getElementById("login-form");
  const errorEl = document.getElementById("login-error");
  const button = document.getElementById("login-btn");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorEl.textContent = "";
    button.disabled = true;

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    try {
      const response = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const body = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(body.message || "No se pudo iniciar sesión");
      }
      saveSession(body.accessToken, username);
      window.location.href = "/app.html";
    } catch (err) {
      errorEl.textContent = err.message || "Error de autenticación";
    } finally {
      button.disabled = false;
    }
  });
})();
