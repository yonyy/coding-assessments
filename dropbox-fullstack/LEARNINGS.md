# Full-Stack Learnings — Patterns & Trade-offs

Fill this in after each mock session. One section per pattern you encounter.

---

## 1. REST API Design

### Resource-Oriented Routes
- Nouns, not verbs: `GET /api/files` not `GET /api/getFiles`
- Use HTTP methods for the action: GET (read), POST (create), DELETE (remove), PATCH (update)
- Nested routes for ownership: `GET /api/users/:id/files` — but flatten when the child has its own ID

### Status Codes to Know
| Code | When to use |
|------|-------------|
| 200  | Successful GET, DELETE, or PATCH |
| 201  | Successful POST that created a resource |
| 400  | Client sent bad input (missing field, invalid type) |
| 404  | Resource not found |
| 409  | Conflict (duplicate, version mismatch) |
| 410  | Gone — resource existed but was permanently removed (use for expired links) |
| 413  | Payload too large (file size limit exceeded) |
| 500  | Unexpected server error |

### Error Response Shape
Always return a consistent shape:
```json
{ "error": "Human-readable message" }
```
Never leak internal details (stack traces, SQL errors) to the client.

---

## 2. In-Memory Store Design

### Map vs Array as Primary Store
| | Map | Array |
|---|---|---|
| Lookup by ID | O(1) | O(n) |
| List all | O(n) iteration | O(n) iteration |
| Memory overhead | Slightly higher | Lower |
| Insert | O(1) amortized | O(1) amortized |
| Delete by ID | O(1) | O(n) (splice) |

**Rule:** Use a `Map<id, record>` whenever you need lookups by ID. Use an array when
you only ever iterate (e.g., append-only event log).

### What In-Memory Loses vs a Real DB
- **Persistence:** data resets on every server restart
- **Multi-process:** two Node processes can't share the same Map
- **Durability:** crash = data loss
- **Querying:** no indexes, no joins — everything is a linear scan

**First architectural change:** add a database (SQLite for simplicity, PostgreSQL for production).

---

## 3. Pagination

### Offset Pagination (`?page=2&limit=20`)
- Simple to implement: `array.slice(page * limit, (page + 1) * limit)`
- **Breaks with live data:** if items are added/removed between requests, items shift →
  page 2 shows duplicates or skips items
- Fine for static or rarely-changing data

### Cursor Pagination (`?cursor=<lastSeenId>&limit=20`)
- Stable: "give me items after ID X" — inserts/deletes before that ID don't affect you
- Requires a monotonically increasing key (timestamp, sequential ID, UUID v7)
- Harder to implement: must find the cursor position, then slice
- **Use for:** live feeds, notifications, any data that changes between requests

---

## 4. Expiration / TTL

### Read-Time Check vs Background Sweep
| Approach | Pros | Cons |
|----------|------|------|
| Compute status on each read (`Date.now() > expiresAt`) | Simple, no background process | Expired records accumulate in memory |
| `setInterval` sweep that marks/deletes expired records | Keeps store clean, enforces hard deletion | More code, timing edge cases |

**Rule:** for low-volume, soft-expiration (status only), read-time is fine. For hard deletion
or high volume, add a sweep.

### Timestamps vs Booleans for State Transitions
Prefer:
```typescript
revokedAt: string | null   // timestamp
```
Over:
```typescript
revoked: boolean
```
Why: a timestamp is also an audit trail — "when did this happen?" is almost always
needed for support, compliance, or debugging.

---

## 5. Polling vs SSE vs WebSockets

| | Polling | Server-Sent Events (SSE) | WebSockets |
|---|---|---|---|
| Direction | Client → Server (repeated) | Server → Client (push) | Bidirectional |
| Protocol | HTTP | HTTP | WS (upgrade) |
| Reconnect | Manual (setInterval) | Automatic | Manual |
| Use when | Simple, infrequent updates | Read-only live feed | Bidirectional: chat, collab editing |
| Cost | New TCP + headers every N seconds | One persistent connection | One persistent connection |

**SSE is underused.** For read-only feeds (activity, notifications), SSE is simpler than
WebSockets and works over HTTP/1.1.

---

## 6. File Handling

### `multer` Storage Options
| Storage | How it works | Max safe size | Use when |
|---------|-------------|---------------|----------|
| `memoryStorage` | Buffer in Node heap | ~50 MB total across all requests | Dev, small files |
| `diskStorage` | Write to filesystem | Limited by disk | Intermediate processing |
| S3 stream | Pipe directly to S3 | Unlimited | Production |

### XHR vs fetch for Upload Progress
- `fetch` does not expose upload progress (Streams API for request bodies is not finalized)
- `XMLHttpRequest.upload.onprogress` is the reliable cross-browser option
- Use `xhr.upload.addEventListener('progress', (e) => { if (e.lengthComputable) ... })`

---

## 7. Search and Filtering

### Linear Scan vs Index
| Scale | Approach |
|-------|----------|
| < 10k records | Linear O(n) scan is fast enough (< 1ms) |
| 10k–1M | Add a sorted index or inverted index per search field |
| > 1M | Full-text search engine (Elasticsearch, Typesense) |

### Server-Side vs Client-Side Filtering
| | Server-side | Client-side |
|---|---|---|
| Payload | Smaller (filtered) | Larger (all data upfront) |
| Works with pagination | Yes | No — must load all pages first |
| Network round-trip | Yes | No (after initial load) |
| **Use when** | Large datasets, paginated | Small datasets loaded once |

### Debounce vs Throttle for Search Boxes
- **Debounce:** fires AFTER the user stops typing for N ms — one request per "burst"
- **Throttle:** fires at most once every N ms — may fire mid-burst
- **Always debounce search boxes** (not throttle) — you want to wait until the user pauses

---

## 8. Frontend State Without a Framework

### The Pattern
```javascript
// 1. State lives in module-level variables
let items = [];
let filter = '';

// 2. One render function — always rebuilds from state
function render() {
  const filtered = items.filter(/* ... */);
  list.innerHTML = '';   // clear
  filtered.forEach(item => list.appendChild(buildRow(item)));
}

// 3. Event handlers mutate state, then call render()
searchInput.addEventListener('input', debounce(() => {
  filter = searchInput.value;
  render();
}, 300));
```

**Never mutate the DOM directly to reflect a state change — always go through state → render.**

### DOM Performance Notes
- `innerHTML = ''` + rebuild is fine for < 500 items
- For > 1000 items, use `DocumentFragment` to batch DOM inserts
- For > 10000 items, use virtual scrolling (render only the visible window)
