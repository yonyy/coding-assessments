/*
 * Mock One — File Browser: Frontend Logic
 *
 * State:
 *   sortField   — "name" | "size" | "modifiedAt"
 *   sortOrder   — "asc" | "desc"
 *
 * DISCUSSION — Why re-fetch on every filter change instead of filtering client-side?
 *   Re-fetching keeps filtering logic in one place (the server) and works correctly
 *   with server-side pagination. Client-side filtering is faster (no network RTT)
 *   but requires all data upfront — breaks with pagination.
 */

const API_BASE = 'http://localhost:3000/api';

// ── State ─────────────────────────────────────────────────────────────────────
let sortField = 'name';
let sortOrder = 'asc';

// ── DOM refs ──────────────────────────────────────────────────────────────────
const searchInput     = document.getElementById('search-input');
const typeFilter      = document.getElementById('type-filter');
const fileList        = document.getElementById('file-list');
const statusLine      = document.getElementById('status-line');
const loadingState    = document.getElementById('loading-state');
const emptyState      = document.getElementById('empty-state');
const sortButtons     = document.querySelectorAll('.sort-btn');
const clearFiltersBtn = document.getElementById('clear-filters-btn');

// ── Utilities ─────────────────────────────────────────────────────────────────

/**
 * Format bytes as a human-readable string: "4.2 MB", "320 KB", "800 B"
 *
 * DISCUSSION — Should formatting happen here (client) or on the server?
 *   Client-side: server stays clean and unit-agnostic; client can localize.
 *   Server-side: simpler client, but formatting is now a concern for the API layer.
 *
 * @param {number} bytes
 * @returns {string}
 */
function formatBytes(bytes) {
  // TODO: implement
  // HINT: use 1024 as the divisor (kibibytes), not 1000
  // 0 B, 1023 B, 1.0 KB, 1.5 MB, 2.3 GB
}

/**
 * Format an ISO date string as a short readable date: "Apr 28, 2026"
 * @param {string} isoString
 * @returns {string}
 */
function formatDate(isoString) {
  // TODO: use Intl.DateTimeFormat({ month: 'short', day: 'numeric', year: 'numeric' })
}

/**
 * Debounce: returns a wrapper that delays invoking fn until after
 * delay ms have elapsed since the last call.
 *
 * DISCUSSION — Why debounce and not throttle for a search box?
 *   Debounce fires AFTER the user stops typing — one request per "burst".
 *   Throttle fires on a schedule, potentially mid-burst (wasted requests, stale results).
 *
 * @param {Function} fn
 * @param {number} delay
 * @returns {Function}
 */
function debounce(fn, delay) {
  // TODO: implement
  // HINT: use a closure over a timer variable; clearTimeout + setTimeout on each call
}

// ── Rendering ─────────────────────────────────────────────────────────────────

/**
 * Build and return a single <li> row for a file record.
 * @param {{ id: string, name: string, extension: string, sizeBytes: number, modifiedAt: string, owner: string }} file
 * @returns {HTMLLIElement}
 */
function buildFileRow(file) {
  // TODO: create <li class="file-row">, populate with:
  //   <span class="file-row__ext">{extension}</span>
  //   <span class="file-row__name">{name}.{extension}</span>
  //   <div class="file-row__meta">{owner}<br>{formatBytes(sizeBytes)}<br>{formatDate(modifiedAt)}</div>
  // HINT: createElement + textContent (safer than innerHTML for user data)
}

/**
 * Render the file list into the DOM.
 * @param {Array} files
 * @param {number} total
 */
function renderFiles(files, total) {
  fileList.innerHTML = '';

  if (files.length === 0) {
    emptyState.hidden = false;
    fileList.hidden = true;
    statusLine.textContent = '';
    return;
  }

  emptyState.hidden = true;
  fileList.hidden = false;

  // TODO: build rows and append to fileList
  // HINT: use a DocumentFragment to batch DOM insertions (better performance)

  statusLine.textContent = `Showing ${files.length} of ${total} files`;
}

/**
 * Populate the type-filter dropdown from the unique extensions in the fetched data.
 * Preserve the current selection if it still exists after re-fetch.
 * @param {Array} files
 */
function populateTypeFilter(files) {
  // TODO: collect unique extensions, sort them, rebuild <option> elements
  // HINT: preserve typeFilter.value before clearing, then re-apply if still present
}

// ── Data fetching ─────────────────────────────────────────────────────────────

/**
 * Fetch files from the API with current filter/sort state, then render.
 */
async function fetchFiles() {
  // TODO: Step 1 — show loading state, hide file list and empty state
  loadingState.hidden = false;
  fileList.hidden = true;
  emptyState.hidden = true;
  statusLine.textContent = 'Loading…';

  try {
    // TODO: Step 2 — build query string from searchInput.value, typeFilter.value,
    //   sortField, sortOrder using URLSearchParams

    // TODO: Step 3 — fetch from API_BASE + '/files?' + params.toString()

    // TODO: Step 4 — parse JSON, call populateTypeFilter(files) then renderFiles(files, total)

  } catch (err) {
    // TODO: Step 5 — show an error in statusLine; hide loading state
    console.error(err);
    statusLine.textContent = 'Failed to load files. Is the server running?';
  } finally {
    // TODO: Step 6 — always hide loadingState
    loadingState.hidden = true;
  }
}

// ── Event wiring ──────────────────────────────────────────────────────────────

// TODO: searchInput — 'input' event → debounced 300ms → fetchFiles()

// TODO: typeFilter — 'change' event → fetchFiles()

// TODO: sortButtons — 'click' event on each button:
//   If the clicked button is already active: toggle sortOrder (asc ↔ desc)
//   Otherwise: set sortField to button's data-sort attribute, reset sortOrder to 'asc'
//   Update .active class on buttons, then fetchFiles()

// TODO: clearFiltersBtn — 'click' event:
//   Reset searchInput.value = '', typeFilter.value = ''
//   Reset sortField = 'name', sortOrder = 'asc', update sort button classes
//   Call fetchFiles()

// ── Init ──────────────────────────────────────────────────────────────────────
fetchFiles();
