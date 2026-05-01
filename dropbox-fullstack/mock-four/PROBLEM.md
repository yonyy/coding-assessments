# Mock Four — Activity Feed / Sync Status Dashboard
**Format:** 60 Minutes · Vanilla JS + Node/TypeScript/Express
**Difficulty:** Medium-Hard | **Estimated Time:** 60 min | **Tags:** Polling, Cursor Pagination, Real-time UX, DOM Animation, Relative Timestamps

---

## Scenario

Dropbox shows users a live feed of file activity: uploads, downloads, edits, shares, and
deletions. You are building the activity feed page. The backend generates a continuous stream
of synthetic events. The frontend polls for new events every 5 seconds and renders them
incrementally — without reloading the full list.

---

## Requirements (build these in 60 minutes)

### Backend
1. `GET /api/activity` — paginated activity events
   - `?limit=20` (default 20, max 100)
   - `?cursor=evt_0042` — return only events **newer** than the cursor event
   - If no cursor: return the most recent `limit` events (newest first)
   - Response includes `nextCursor` (the ID of the newest event returned)
2. `GET /api/activity/:eventId` — get one event by ID; 404 if not found
3. `POST /api/activity/simulate` — generate 1–5 random new events immediately
4. Background: auto-generate one new event every 10 seconds via `setInterval`

### Frontend
1. On page load, fetch the initial batch and render newest-first
2. Poll `GET /api/activity?cursor=<lastSeenId>` every 5 seconds
3. **Prepend** new events to the top of the list with a CSS slide-in animation
4. A "N new events" banner at the top — clicking it scrolls to top
   **Do not auto-scroll** the user (they might be reading older events)
5. A filter bar: All / Uploads / Downloads / Edits / Shares / Deletions
6. Each event row: icon/emoji for type, file name, user, relative timestamp ("2 min ago")

---

## Data Model

```typescript
type ActivityType = 'upload' | 'download' | 'edit' | 'share' | 'delete';

interface ActivityEvent {
  eventId: string;        // "evt_0001", "evt_0002", ... (monotonically increasing)
  type: ActivityType;
  fileName: string;
  filePath: string;
  user: string;
  timestamp: string;      // ISO 8601
  metadata?: Record<string, string>; // e.g. { sharedWith: 'alice@example.com' }
}
```

---

## API Contract

### GET /api/activity
**Request:**
```
GET /api/activity?limit=20
GET /api/activity?cursor=evt_0042&limit=20
```
**Response 200:**
```json
{
  "events": [ /* ActivityEvent[], newest first */ ],
  "nextCursor": "evt_0050",
  "hasMore": false
}
```

### GET /api/activity/:eventId
**Response 200:** `{ "event": ActivityEvent }`
**Response 404:** `{ "error": "Event not found" }`

### POST /api/activity/simulate
**Response 201:**
```json
{ "generated": 3, "events": [ /* the new ActivityEvent[] */ ] }
```

---

## Stretch Goals (if time remains)

- Replace polling with Server-Sent Events: `GET /api/activity/stream` using `text/event-stream`
- Add backend filtering: `?type=upload,edit`
- Relative timestamps that update themselves every 60 seconds without a page reload
- Export the feed as CSV: `GET /api/activity/export?format=csv`
- Virtual scrolling for feeds with 1000+ events in the DOM

---

## Performance & Trade-off Discussion Points

1. **Polling vs SSE vs WebSockets:** For a read-only feed, SSE is simpler than WebSockets
   (unidirectional, HTTP-native, automatic reconnect). When would WebSockets be necessary?
   *(Bidirectional: chat, collaborative editing.)* What does polling cost that SSE doesn't?
   *(A new TCP connection + full headers every 5 seconds.)*

2. **Cursor vs offset pagination:** Offset pagination (`?page=2`) breaks when new events
   are added at the top — items shift, causing duplicates or gaps. Cursor pagination
   ("give me events after ID X") is stable. What does the cursor require?
   *(A stable, monotonically increasing key.)*

3. **In-memory event log growth:** Events accumulate forever. What eviction policy would
   you add? *(Ring buffer: keep only the last N events. LRU cache.)* At what event rate
   does 5-second polling stop feeling real-time enough to a user?

4. **DOM performance with a long feed:** If 10,000 events are in the DOM, scrolling
   performance degrades. What is virtual scrolling (windowed rendering)? At what count
   does the browser start struggling? *(Typically 1,000–5,000 complex nodes.)*

5. **Relative timestamps ("2 minutes ago"):** These go stale without client-side updates.
   What is the right update cadence? *(< 1 min: update every 10s. Hours: every 5 min.)*
   Why is this a UI state problem and not a data problem?
