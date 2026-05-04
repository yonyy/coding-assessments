# Mock One — File Browser with Search & Filter
**Format:** 60 Minutes · Vanilla JS + Node/TypeScript/Express
**Difficulty:** Medium | **Estimated Time:** 60 min | **Tags:** REST API, Filtering, Sorting, In-Memory Data, DOM Manipulation

---

## Scenario

You are building a core Dropbox feature: the file browser. Users land on a page that shows
their files in a flat list. They can search by name, filter by file type, and sort by name,
size, or modification date. The backend serves the file metadata; the frontend renders the
results.

---

## Requirements (build these in 60 minutes)

### Backend
1. `GET /api/files` — returns all files as JSON
   - Query params:
     - `?search=<string>` — case-insensitive substring match on `name`
     - `?type=<ext>` — exact match on `extension` (e.g. `pdf`)
     - `?sort=name|size|modifiedAt` — default: `name`
     - `?order=asc|desc` — default: `asc`
2. `GET /api/files/:id` — returns a single file by ID; 404 if not found
3. Seed the server with 20+ realistic in-memory file records on startup

### Frontend
1. On page load, fetch and display all files in a row layout
2. A search box that re-fetches as the user types (debounce: 300 ms)
3. A dropdown to filter by file type
4. Sort buttons (name / size / date) — clicking the active button toggles asc/desc
5. Show a loading skeleton while the fetch is in-flight
6. Show an empty state ("No files found") when results are empty

---

## Data Model

```typescript
interface FileRecord {
  id: string;           // "1", "2", etc.
  name: string;         // "Q4 Budget" (no extension in name)
  extension: string;    // "xlsx" (lowercase, no dot)
  sizeBytes: number;    // raw bytes
  modifiedAt: string;   // ISO 8601
  owner: string;        // display name
  path: string;         // "/documents/finance/"
}
```

---

## API Contract

### GET /api/files
**Request:**
```
GET /api/files?search=budget&type=xlsx&sort=size&order=desc
```
**Response 200:**
```json
{
  "files": [ /* FileRecord[] */ ],
  "total": 3
}
```

### GET /api/files/:id
**Response 200:** `{ "file": FileRecord }`
**Response 404:** `{ "error": "File not found" }`

---

## Stretch Goals (if time remains)

- `DELETE /api/files/:id` with a confirmation dialog in the UI
- Pagination: add `?page=1&limit=10`; implement page controls in the UI
- Sort state persisted to `localStorage` so it survives page refresh
- File size formatted as "4.2 MB" in the UI (format on the frontend, not backend)
- Multi-select checkboxes with a bulk delete action

---

## Performance & Trade-off Discussion Points

1. **Search implementation:** The current approach does an O(n) scan on every request.
   At what file count does that become a problem? What data structure would you add?
   *(Trie, inverted index, or a sorted array with binary search on name)*

2. **In-memory store vs database:** What breaks first as you scale?
   *(No persistence on restart, single-process only, no concurrent write safety.)*
   What would you replace it with first?

3. **Debounce vs throttle on the search box:** Why debounce and not throttle?
   What does debounce guarantee that throttle does not?

4. **Sort stability:** JavaScript's `Array.sort()` is stable in all modern engines.
   Does that matter here? When would sort instability cause a visible bug?

5. **Server-side vs client-side filtering:** We re-fetch on every filter change.
   When would it be better to fetch all data once and filter on the client?
   *(Trade-off: initial payload size vs network round-trips per filter.)*
