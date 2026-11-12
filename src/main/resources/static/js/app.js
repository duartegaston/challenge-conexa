import { apiFetch, clearSession, errorMessage, getToken, getUsername, hasRole } from "/js/auth.js";

if (!getToken()) {
  window.location.href = "/";
}

const $ = (id) => document.getElementById(id);
const statusEl = $("status");
const resultsEl = $("results");
const detailEl = $("detail");
const pageInfo = $("page-info");
const prevBtn = $("prev");
const nextBtn = $("next");
const filterInput = $("filter");

const state = { resource: "people", page: 1, limit: 10, filter: "", hasNext: false };

// Cada carga lleva un número; si llega una respuesta vieja (cambio rápido de tab, cold start lento) se descarta.
let latestRequest = 0;

$("who").textContent = getUsername() || "piloto";

$("logout").addEventListener("click", () => {
  clearSession();
  window.location.href = "/";
});

if (hasRole("ADMIN")) {
  const clearCache = $("clear-cache");
  clearCache.classList.remove("hidden");
  clearCache.addEventListener("click", async () => {
    try {
      const response = await apiFetch("/api/v1/admin/cache", { method: "DELETE" });
      setStatus(response.ok ? "Cachés vaciadas." : await errorMessage(response), !response.ok);
    } catch (err) {
      setStatus(err.message, true);
    }
  });
}

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((t) => t.setAttribute("aria-selected", String(t === tab)));
    state.resource = tab.dataset.resource;
    state.page = 1;
    detailEl.classList.add("hidden");
    loadList();
  });
});

function applyFilter() {
  state.filter = filterInput.value.trim();
  state.page = 1;
  loadList();
}
$("search-btn").addEventListener("click", applyFilter);
filterInput.addEventListener("keydown", (event) => event.key === "Enter" && applyFilter());

$("limit").addEventListener("change", (event) => {
  state.limit = Number(event.target.value);
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
  const request = ++latestRequest;
  setStatus("Consultando archivos galácticos…");
  resultsEl.replaceChildren();

  const params = new URLSearchParams({ page: state.page, limit: state.limit });
  if (state.filter) {
    params.set(state.resource === "films" ? "title" : "name", state.filter);
  }

  try {
    const response = await apiFetch(`/api/v1/${state.resource}?${params}`);
    if (!response.ok) {
      throw new Error(await errorMessage(response, "Fallo al cargar"));
    }
    const body = await response.json();
    if (request !== latestRequest) {
      return;
    }

    state.hasNext = Boolean(body.hasNext);
    pageInfo.textContent = `Página ${body.page} · total ${body.total}`;
    prevBtn.disabled = state.page <= 1;
    nextBtn.disabled = !state.hasNext;

    if (body.content.length === 0) {
      setStatus("Sin resultados en este sector.");
      return;
    }
    setStatus(`${body.content.length} registros en esta página.`);
    body.content.forEach((item) => resultsEl.appendChild(renderRow(item)));
  } catch (err) {
    if (request === latestRequest) {
      setStatus(err.message || "Fallo al cargar", true);
    }
  }
}

function renderRow(item) {
  const li = document.createElement("li");
  li.tabIndex = 0;
  li.setAttribute("role", "button");
  li.append(element("span", "name", item.name || "(sin nombre)"), element("span", "id", `#${item.id || "?"}`));
  li.addEventListener("click", () => loadDetail(item.id));
  li.addEventListener("keydown", (event) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      loadDetail(item.id);
    }
  });
  return li;
}

async function loadDetail(id) {
  setStatus("Abriendo ficha…");
  try {
    const response = await apiFetch(`/api/v1/${state.resource}/${encodeURIComponent(id)}`);
    if (!response.ok) {
      throw new Error(await errorMessage(response, "No se pudo abrir el detalle"));
    }
    renderDetail(await response.json());
    setStatus("");
  } catch (err) {
    setStatus(err.message || "No se pudo abrir el detalle", true);
  }
}

const HIDDEN_FIELDS = new Set(["name", "title", "url", "created", "edited", "uid", "properties"]);

function renderDetail(item) {
  const list = element("dl", "fields");
  Object.entries(item.fields || {})
    .filter(([key]) => !HIDDEN_FIELDS.has(key))
    .forEach(([key, value]) => {
      const row = document.createElement("div");
      row.append(element("dt", "", key), element("dd", "", formatValue(value)));
      list.appendChild(row);
    });

  detailEl.replaceChildren(
    element("h2", "", item.name || "Detalle"),
    element("p", "sub", `${state.resource} · id ${item.id || ""}`),
    list,
  );
  detailEl.classList.remove("hidden");
  detailEl.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function formatValue(value) {
  if (value == null) return "—";
  if (Array.isArray(value)) return value.length ? value.join(", ") : "—";
  if (typeof value === "object") return JSON.stringify(value);
  return String(value);
}

function setStatus(message, isError = false) {
  statusEl.textContent = message;
  statusEl.classList.toggle("error", isError);
}

/** Crea un elemento con texto plano (textContent): nada de HTML armado a mano, sin riesgo de inyección. */
function element(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

loadList();
