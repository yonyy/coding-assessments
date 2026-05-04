/*
 * Mock Two — Shared Link Manager: Frontend Logic
 *
 * State:
 *   links — the current list from GET /api/links (includes computed status)
 *
 * Key APIs used:
 *   navigator.clipboard.writeText()  — Clipboard API for "Copy" button
 *
 * DISCUSSION — Clipboard API requires HTTPS or localhost.
 *   In production this is fine. On a non-localhost HTTP origin, use the older
 *   document.execCommand('copy') fallback (deprecated but still works).
 */

const API_BASE = 'http://localhost:3000/api';

// ── State ─────────────────────────────────────────────────────────────────────
let links = [];

// ── DOM refs ──────────────────────────────────────────────────────────────────
const createForm      = document.getElementById('create-form');
const fileSelect      = document.getElementById('file-select');
const labelInput      = document.getElementById('label-input');
const ttlSelect       = document.getElementById('ttl-select');
const customTtlField  = document.getElementById('custom-ttl-field');
const customSeconds   = document.getElementById('custom-seconds');
const formError       = document.getElementById('form-error');
const submitBtn       = document.getElementById('submit-btn');
const linkList        = document.getElementById('link-list');
const linksLoading    = document.getElementById('links-loading');
const linksEmpty      = document.getElementById('links-empty');

// ── Utilities ─────────────────────────────────────────────────────────────────

/**
 * Format an ISO date string as a readable datetime: "Apr 28, 2026 at 3:00 PM"
 * @param {string} isoString
 * @returns {string}
 */
function formatDateTime(isoString) {
  // TODO: use Intl.DateTimeFormat with { month: 'short', day: 'numeric',
  //   year: 'numeric', hour: 'numeric', minute: '2-digit' }
}

/**
 * Copy text to the clipboard; update the button label temporarily.
 * @param {string} text
 * @param {HTMLButtonElement} btn
 */
async function copyToClipboard(text, btn) {
  // TODO: navigator.clipboard.writeText(text)
  // On success: btn.textContent = 'Copied!' then restore after 1500ms
  // On failure: alert('Failed to copy — please copy manually: ' + text)
  //
  // DISCUSSION — Why might clipboard access fail?
  //   Permissions-Policy can disable clipboard; page must be focused; Safari
  //   requires the copy to happen in a user gesture handler (which this is).
}

// ── Rendering ─────────────────────────────────────────────────────────────────

/**
 * Build and return a .link-card element for one link.
 * @param {{ token, fileName, label, shareUrl, createdAt, expiresAt, revokedAt, status }} link
 * @returns {HTMLDivElement}
 */
function buildLinkCard(link) {
  // TODO: create .link-card with:
  //   Header row: .link-card__filename, .badge--{status}, optional .link-card__label
  //   URL row: .link-card__url (truncated), "Copy" button → copyToClipboard(link.shareUrl)
  //   Meta row: "Created {formatDateTime(createdAt)}", "Expires {expiresAt ? formatDateTime(expiresAt) : 'Never'}"
  //   Actions: if status === 'active', show "Revoke" button → revokeLink(link.token)
}

/**
 * Render all links into the DOM.
 */
function renderLinks() {
  linkList.innerHTML = '';

  if (links.length === 0) {
    linksEmpty.hidden = false;
    return;
  }

  linksEmpty.hidden = true;
  // TODO: build a card for each link and append to linkList
}

// ── API calls ─────────────────────────────────────────────────────────────────

/**
 * Fetch the current link list and re-render.
 */
async function fetchLinks() {
  linksLoading.hidden = false;
  linkList.innerHTML = '';
  linksEmpty.hidden = true;

  try {
    // TODO: GET /api/links → parse { links } → store in links → renderLinks()
  } catch (err) {
    console.error('Failed to load links:', err);
  } finally {
    linksLoading.hidden = true;
  }
}

/**
 * POST to create a new link, then refresh the list.
 * @param {{ fileId: string, expiresInSeconds?: number, label?: string }} payload
 */
async function createLink(payload) {
  // TODO: POST /api/links with JSON body
  // On 201: call fetchLinks()
  // On 400: show formError with the error message from the response
  // On error: show generic message in formError
}

/**
 * DELETE a link by token (soft revoke), then refresh the list.
 * @param {string} token
 */
async function revokeLink(token) {
  // TODO: DELETE /api/links/:token
  // On success: call fetchLinks()
  // On error: alert with message
}

// ── Event wiring ──────────────────────────────────────────────────────────────

// TTL select — show/hide custom seconds field
ttlSelect.addEventListener('change', () => {
  // TODO: customTtlField.hidden = (ttlSelect.value !== 'custom')
});

// Form submit
createForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  formError.hidden = true;

  const fileId = fileSelect.value;
  if (!fileId) {
    formError.textContent = 'Please select a file.';
    formError.hidden = false;
    return;
  }

  // TODO: build expiresInSeconds from ttlSelect.value / customSeconds.value
  // TODO: build payload { fileId, expiresInSeconds?, label? }
  // TODO: disable submitBtn, call createLink(payload), re-enable submitBtn
  // TODO: reset form fields on success
});

// ── Init ──────────────────────────────────────────────────────────────────────
fetchLinks();
