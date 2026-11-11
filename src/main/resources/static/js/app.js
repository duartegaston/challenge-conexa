(() => {
  if (!getToken()) {
    window.location.href = "/";
    return;
  }

  const who = document.getElementById("who");
  const statusEl = document.getElementById("status");
  const resultsEl = document.getElementById("results");
  const detailEl = document.getElementById("detail");
  const pageInfo = document.getElementById("page-info");
  const prevBtn = document.getElementById("prev");
  const nextBtn = document.getElementById("next");
  const filterInput = document.getElementById("filter");
  const limitSelect = document.getElementById("limit");

  who.textContent = getUsername() || "piloto";

  const state = {
    resource: "people",
    page: 1,
    limit: 10,
    filter: "",
    hasNext: false,
  };

  document.getElementById("logout").addEventListener("click", () => {
    clearSession();
    window.location.href = "/";
  });

  document.querySelectorAll(".tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      document.querySelectorAll(".tab").forEach((t) => t.setAttribute("aria-selected", "false"));
      tab.setAttribute("aria-selected", "true");
      state.resource = tab.dataset.resource;
      state.page = 1;
      detailEl.classList.add("hidden");
      loadList();
    });
  });

  document.getElementById("search-btn").addEventListener("click", () => {
    state.filter = filterInput.value.trim();
    state.page = 1;
    loadList();
  });

  filterInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      state.filter = filterInput.value.trim();
      state.page = 1;
      loadList();
    }
  });

  limitSelect.addEventListener("change", () => {
    state.limit = Number(limitSelect.value);
    state.page = 1;
    loadList();
  });

  prevBtn.addEventListener("click", () => {
    if (state.page > 1) {
      state.page -= 1;
      loadList();
    }
  });

  nextBtn.addEventListener("click", () => {
    if (state.hasNext) {
      state.page += 1;
      loadList();
    }
  });

  async function loadList() {
    setStatus("Consultando archivos galácticos…");
    resultsEl.innerHTML = "";
    try {
      const params = new URLSearchParams({
        page: String(state.page),
        limit: String(state.limit),
      });
      if (state.filter) {
        if (state.resource === "films") {
          params.set("title", state.filter);
        } else {
          params.set("name", state.filter);
        }
      }
      const response = await apiFetch(`/api/v1/${state.resource}?${params}`);
      const body = await response.json();
      if (!response.ok) {
        throw new Error(body.message || `Error ${response.status}`);
      }

      state.hasNext = Boolean(body.hasNext);
      pageInfo.textContent = `Página ${body.page} · total ${body.total}`;
      prevBtn.disabled = state.page <= 1;
      nextBtn.disabled = !state.hasNext;

      if (!body.content || body.content.length === 0) {
        setStatus("Sin resultados en este sector.");
        return;
      }

      setStatus(`${body.content.length} registros en esta página.`);
      body.content.forEach((item) => {
        const li = document.createElement("li");
        li.innerHTML = `<span class="name">${escapeHtml(item.name || "(sin nombre)")}</span>
          <span class="id">#${escapeHtml(item.id || "?")}</span>`;
        li.addEventListener("click", () => loadDetail(item.id));
        resultsEl.appendChild(li);
      });
    } catch (err) {
      setStatus(err.message || "Fallo al cargar", true);
    }
  }

  async function loadDetail(id) {
    setStatus("Abriendo ficha…");
    try {
      const response = await apiFetch(`/api/v1/${state.resource}/${id}`);
      const body = await response.json();
      if (!response.ok) {
        throw new Error(body.message || `Error ${response.status}`);
      }
      renderDetail(body);
      setStatus("");
    } catch (err) {
      setStatus(err.message || "No se pudo abrir el detalle", true);
    }
  }

  function renderDetail(item) {
    const fields = item.fields || {};
    const skip = new Set(["name", "title", "url", "created", "edited"]);
    const entries = Object.entries(fields).filter(([key]) => !skip.has(key));
    detailEl.classList.remove("hidden");
    detailEl.innerHTML = `
      <h2>${escapeHtml(item.name || "Detalle")}</h2>
      <p class="sub">${escapeHtml(state.resource)} · id ${escapeHtml(item.id || "")}</p>
      <dl class="fields">
        ${entries
          .map(
            ([key, value]) => `
          <div>
            <dt>${escapeHtml(key)}</dt>
            <dd>${escapeHtml(formatValue(value))}</dd>
          </div>`
          )
          .join("")}
      </dl>`;
    detailEl.scrollIntoView({ behavior: "smooth", block: "nearest" });
  }

  function formatValue(value) {
    if (Array.isArray(value)) {
      return value.join(", ");
    }
    if (value == null) {
      return "—";
    }
    return String(value);
  }

  function setStatus(message, isError = false) {
    statusEl.textContent = message;
    statusEl.classList.toggle("error", Boolean(isError));
  }

  function escapeHtml(value) {
    return String(value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;");
  }

  loadList();
})();
