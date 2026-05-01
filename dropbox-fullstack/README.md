# Dropbox Full-Stack Interview Prep

Mock full-stack coding sessions styled after Dropbox's 60-minute feature-building interview.
Each mock is one complete feature implemented with vanilla HTML/CSS/JS on the frontend
and Node.js + TypeScript + Express on the backend.

## Structure

```
.
├── mock-one/     # File Browser — search, filter, sort a flat file listing
├── mock-two/     # Shared Link Manager — create/expire shareable file links
├── mock-three/   # File Upload — multi-file drag-and-drop with progress bars
├── mock-four/    # Activity Feed — sync status dashboard with cursor polling
├── README.md
└── LEARNINGS.md
```

## Interview Format

| Format      | Detail                                                                  |
|-------------|-------------------------------------------------------------------------|
| Duration    | 60 minutes                                                              |
| Frontend    | Vanilla HTML, CSS, JavaScript — no frameworks                           |
| Backend     | Node.js with TypeScript + Express                                       |
| Focus       | Code clarity, reasoning aloud, trade-off discussion, feature completeness |

## Running Any Mock

Each mock is a self-contained npm project. From any mock directory:

```bash
npm install
npm run dev      # starts backend (ts-node-dev) + frontend (serve) concurrently
```

Or start separately:

```bash
npm run server   # backend only  — http://localhost:3000
npm run client   # frontend only — http://localhost:5173
```

Type-check the backend without running:

```bash
npm run typecheck
```

## Mock Sessions

| Mock      | Feature                         | Difficulty    | Key Concepts                                |
|-----------|---------------------------------|---------------|---------------------------------------------|
| mock-one  | File Browser + Search/Filter    | Medium        | REST, filtering, in-memory Map, debounce    |
| mock-two  | Shared Link Manager + TTL       | Medium        | Token gen, expiration, 410 vs 404, audit    |
| mock-three| File Upload + Progress Bars     | Medium-Hard   | XHR progress, multer, upload state machine  |
| mock-four | Activity Feed + Polling         | Medium-Hard   | Cursor pagination, polling vs SSE, ring buf |

## Full-Stack Patterns Covered

See `LEARNINGS.md` for detailed notes after each session.

1. REST API design — resource-oriented routes, status codes, error shapes
2. In-memory data stores — Map vs array, O(1) vs O(n) lookup, trade-offs vs DB
3. Pagination — offset vs cursor, why cursor is stable with live data
4. Expiration / TTL — time-based invalidation, read-time vs background sweep
5. Polling vs Server-Sent Events vs WebSockets — when to use each
6. File handling — multipart/form-data, memory vs disk storage, streaming
7. Filtering/search — linear scan vs index, server-side vs client-side
8. Frontend state management — vanilla DOM without a framework
