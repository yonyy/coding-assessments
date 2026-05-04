/*
 * Mock Four — Activity Feed: Frontend Logic
 *
 * Polling strategy:
 *   1. Initial fetch: GET /api/activity?limit=20  (no cursor)
 *      Store nextCursor from response.
 *   2. Every 5 seconds: GET /api/activity?cursor=<nextCursor>
 *      If new events arrive, prepend them and update nextCursor.
 *      Do NOT auto-scroll — show "N new events" banner instead.
 *
 * Filter strategy:
 *   All events stay in the DOM; filter buttons add/remove .event-row--hidden.
 *   This avoids re-fetching on filter change and is fast for small feeds.
 *   Trade-off: with 10k events, hiding/showing 10k DOM nodes is slow.
 *   Fix: server-side filtering (?type=upload) + re-fetch on filter change.
 *
 * DISCUSSION — Polling vs SSE:
 *   Polling: simple, works everywhere, but opens a new TCP connection every 5s.
 *   SSE (text/event-stream): one persistent connection, server pushes events.
 *   For a read-only feed, SSE is the better production choice.
 *   Stretch goal: replace setInterval + fetch with EventSource('/api/activity/stream').
 */

const API_BASE = 'http://localhost:3000/api';
const POLL_INTERVAL_MS = 5000;

// ── State ─────────────────────────────────────────────────────────────────────
let nextCursor = null;        // eventId of the newest event we've seen
let pendingNewEvents = [];    // events received via polling, waiting for user to reveal
let activeFilter = '';        // '' = all, or an ActivityType string
let pollTimer = null;

// ── DOM refs ──────────────────────────────────────────────────────────────────
const eventFeed       = document.getElementById('event-feed');
const loadingState    = document.getElementById('loading-state');
const emptyState      = document.getElementById('empty-state');
const newEventsBanner = document.getElementById('new-events-banner');
const newEventsCount  = document.getElementById('new-events-count');
const showNewBtn      = document.getElementById('show-new-btn');
const filterButtons   = document.querySelectorAll('.filter-btn');
const simulateBtn     = document.getElementById('simulate-btn');

// ── Utilities ─────────────────────────────────────────────────────────────────

/** Map activity type to an emoji icon. */
const TYPE_ICONS = {
  upload:   '⬆️',
  download: '⬇️',
  edit:     '✏️',
  share:    '🔗',
  delete:   '🗑️',
};

/** Map activity type to a human-readable past-tense action. */
const TYPE_LABELS = {
  upload:   'uploaded',
  download: 'downloaded',
  edit:     'edited',
  share:    'shared',
  delete:   'deleted',
};

/**
 * Format a timestamp as a relative string: "just now", "3 min ago", "2 hrs ago".
 *
 * DISCUSSION — Why not format on the server?
 *   Relative time depends on "now" — which is always changing.
 *   If the server formatted it, the string would be stale the moment it arrived.
 *   This is a UI state problem: the same timestamp means different relative strings
 *   at different moments. Solution: store the ISO timestamp, reformat on demand.
 *
 * @param {string} isoString
 * @returns {string}
 */
function relativeTime(isoString) {
  // TODO: compute Math.floor((Date.now() - new Date(isoString)) / 1000) = secondsAgo
  // < 10s   → "just now"
  // < 60s   → "Xs ago"
  // < 3600s → "X min ago"
  // < 86400s → "X hr ago"
  // else    → use Intl.DateTimeFormat for the date
}

// ── Rendering ─────────────────────────────────────────────────────────────────

/**
 * Build a single <li class="event-row"> for one ActivityEvent.
 * @param {{ eventId, type, fileName, filePath, user, timestamp, metadata? }} event
 * @param {boolean} isNew — if true, adds the slide-in animation class
 * @returns {HTMLLIElement}
 */
function buildEventRow(event, isNew = false) {
  // TODO: create li.event-row (add .event-row--new if isNew, .event-row--hidden if filtered)
  //   data-type="{event.type}" for filter toggling
  //   data-event-id="{event.eventId}"
  //
  //   .event-row__icon: TYPE_ICONS[event.type]
  //   .event-row__body:
  //     .event-row__file: event.fileName
  //     .event-row__action: "{user} {TYPE_LABELS[type]}" + optional metadata
  //       for 'share': append " → {metadata.sharedWith}"
  //   .event-row__meta:
  //     user (line 1)
  //     relativeTime(event.timestamp) (line 2)
}

/**
 * Prepend an array of new events (already newest-first) to the top of the feed.
 * @param {Array} events — newest first
 */
function prependEvents(events) {
  const fragment = document.createDocumentFragment();
  // Build rows in reverse so the oldest of the batch ends up first in the feed
  // (the array is already newest-first; prepending in order puts the newest at top)
  for (const event of events) {
    fragment.appendChild(buildEventRow(event, true));
  }
  // TODO: eventFeed.prepend(fragment)
  // After prepending, remove .event-row--new class after animation duration (250ms)
  // so re-filtering doesn't re-trigger the animation
}

/**
 * Apply the current activeFilter to all rows in the DOM.
 * Adds/removes .event-row--hidden based on data-type attribute.
 */
function applyFilter() {
  // TODO: eventFeed.querySelectorAll('.event-row').forEach(row => {
  //   const match = !activeFilter || row.dataset.type === activeFilter;
  //   row.classList.toggle('event-row--hidden', !match);
  // });
  // Also toggle emptyState if all visible rows are hidden
}

// ── Data fetching ─────────────────────────────────────────────────────────────

/**
 * Fetch the initial batch of events on page load.
 */
async function fetchInitial() {
  loadingState.hidden = false;
  emptyState.hidden = true;

  try {
    // TODO: GET /api/activity?limit=20
    // Parse { events, nextCursor } from response
    // Store nextCursor
    // Render events: eventFeed.appendChild(fragment of buildEventRow(event) for each)
    // Show emptyState if events.length === 0
  } catch (err) {
    console.error('Failed to load activity:', err);
  } finally {
    loadingState.hidden = true;
  }
}

/**
 * Poll for new events since the last cursor. Called every POLL_INTERVAL_MS.
 */
async function poll() {
  if (!nextCursor) return;

  try {
    // TODO: GET /api/activity?cursor={nextCursor}&limit=20
    // If events.length > 0:
    //   Update nextCursor to the new nextCursor from response
    //   Accumulate into pendingNewEvents (newest first)
    //   Update banner: newEventsCount.textContent = pendingNewEvents.length
    //   newEventsBanner.hidden = false
  } catch (err) {
    console.error('Poll failed:', err);
  }
}

/**
 * Reveal pending new events — called when the user clicks the banner.
 */
function revealNewEvents() {
  if (pendingNewEvents.length === 0) return;

  prependEvents(pendingNewEvents);
  pendingNewEvents = [];
  newEventsBanner.hidden = true;
  applyFilter();

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ── Event wiring ──────────────────────────────────────────────────────────────

// "Show N new events" banner button
showNewBtn.addEventListener('click', revealNewEvents);

// Filter buttons
filterButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    filterButtons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    activeFilter = btn.dataset.type ?? '';
    applyFilter();
  });
});

// Simulate button — POST to generate events, then poll immediately
simulateBtn.addEventListener('click', async () => {
  try {
    await fetch(`${API_BASE}/activity/simulate`, { method: 'POST' });
    await poll();
  } catch (err) {
    console.error('Simulate failed:', err);
  }
});

// ── Init ──────────────────────────────────────────────────────────────────────
fetchInitial().then(() => {
  pollTimer = setInterval(poll, POLL_INTERVAL_MS);
});
