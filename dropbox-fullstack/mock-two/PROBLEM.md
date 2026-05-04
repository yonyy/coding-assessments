# Mock Two — Shared Link Manager with Expiration
**Format:** 60 Minutes · Vanilla JS + Node/TypeScript/Express
**Difficulty:** Medium | **Estimated Time:** 60 min | **Tags:** TTL, Expiration, Token Generation, REST API, State Management

---

## Scenario

Dropbox allows users to share files via a link. Each link has an optional expiration time and
an optional label. You are building the share link management UI and API: users can create a
link for a file, see all active links, revoke a link, and links become invalid after their TTL
expires.

---

## Requirements (build these in 60 minutes)

### Backend
1. `POST /api/links` — create a new share link
   - Body: `{ fileId, expiresInSeconds?: number, label?: string }`
   - Generate a unique 12-character alphanumeric token
   - Return the full link record including the share URL
2. `GET /api/links` — list all links; include computed `status` field (`"active"` | `"expired"` | `"revoked"`)
3. `GET /api/links/:token` — resolve a token; 410 Gone if expired, 404 if unknown
4. `DELETE /api/links/:token` — revoke a link (set `revokedAt`, do not delete)

### Frontend
1. A form: select a file (from a hardcoded dropdown of 5 files), optional label, optional TTL
   (None / 1 hour / 24 hours / 7 days / Custom seconds)
2. On submit: POST to create the link, then refresh the list
3. Link list shows: file name, label, share URL (copyable), created time, expiration time
   (or "Never"), status badge (Active / Expired / Revoked)
4. A "Revoke" button per active link
5. A "Copy" button that copies the share URL to the clipboard

---

## Data Model

```typescript
interface ShareLink {
  token: string;            // random 12-char alphanumeric
  fileId: string;
  fileName: string;         // denormalized for display
  label: string;            // defaults to ""
  shareUrl: string;         // "http://localhost:3000/s/:token"
  createdAt: string;        // ISO 8601
  expiresAt: string | null; // null = never expires
  revokedAt: string | null; // null = not revoked
}

// Derived on each read — NOT stored:
// status: "active" | "expired" | "revoked"
```

---

## API Contract

### POST /api/links
**Request body:**
```json
{ "fileId": "abc123", "expiresInSeconds": 3600, "label": "For Alice" }
```
**Response 201:**
```json
{ "link": ShareLink }
```
**Response 400:** `{ "error": "fileId is required" }`

### GET /api/links
**Response 200:**
```json
{
  "links": [
    { ...ShareLink, "status": "active" | "expired" | "revoked" }
  ]
}
```

### GET /api/links/:token
**Response 200:** `{ "link": ShareLink }`
**Response 410:** `{ "error": "Link has expired" }`
**Response 404:** `{ "error": "Link not found" }`

### DELETE /api/links/:token
**Response 200:** `{ "message": "Link revoked" }`
**Response 404:** `{ "error": "Link not found" }`

---

## Stretch Goals (if time remains)

- Auto-refresh the link list every 30 seconds so status updates without reload
- Show a countdown timer for links expiring within the next hour
- Filter the list: All / Active / Expired / Revoked
- `GET /api/links/:token/preview` that returns the linked file metadata

---

## Performance & Trade-off Discussion Points

1. **Expiration check on read vs background sweep:** The current approach computes `status`
   at read time by comparing `Date.now()` to `expiresAt`. What is the alternative?
   *(A `setInterval` sweep that marks or deletes expired links.)* When is each preferred?

2. **Token generation:** `Math.random()` is not cryptographically secure.
   What would you use in production? *(`crypto.randomBytes` in Node.)* Why does it matter
   for share links? *(Predictable tokens can be enumerated by an attacker.)*

3. **Denormalization (`fileName` on the link):** You stored `fileName` on `ShareLink` to
   avoid a join. What's the risk? *(File gets renamed → link shows a stale name.)*
   How would you handle that in a real system?

4. **410 vs 404 for expired links:** Why 410 Gone and not 404? What does the HTTP spec
   say about 410? *(Resource existed but is permanently gone.)* How does 410 help CDN
   caching? *(CDNs cache 410 responses; they do not cache 404 by default.)*

5. **`revokedAt` timestamp vs a `revoked: boolean` flag:** Why store a timestamp?
   *(Audit trail — "when was this revoked?" is needed for support and compliance.)*
   General principle: prefer timestamps to booleans for state transitions.
