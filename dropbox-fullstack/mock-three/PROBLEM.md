# Mock Three — File Upload with Progress Tracking
**Format:** 60 Minutes · Vanilla JS + Node/TypeScript/Express
**Difficulty:** Medium-Hard | **Estimated Time:** 60 min | **Tags:** File Upload, Multipart, XHR Progress, State Machine, UX

---

## Scenario

Users need to upload files to Dropbox. You are building the upload flow: a drag-and-drop or
click-to-select UI that shows per-file upload progress, then stores file metadata on the
backend. The backend receives uploads via multipart form-data using multer.

---

## Requirements (build these in 60 minutes)

### Backend
1. `POST /api/uploads` — receive a multipart file upload (use `multer`)
   - Store metadata in memory (not on disk — `multer.memoryStorage()`)
   - Enforce 10 MB size limit; return 413 if exceeded
   - Return `{ uploadId, status: "complete", file: UploadRecord }`
2. `GET /api/uploads` — list all uploads, newest first
3. `GET /api/uploads/:uploadId` — get one upload by ID; 404 if not found
4. `DELETE /api/uploads/:uploadId` — remove an upload record; 404 if not found

### Frontend
1. A drop zone: click-to-open file picker OR drag files onto it
2. Support selecting multiple files at once
3. Per-file progress bar using `XMLHttpRequest`'s `upload.onprogress` event
   — **do NOT use `fetch`** (fetch does not expose upload progress in browsers)
4. Per-file status: Queued / Uploading / Complete / Failed
5. An "×" button to remove a file from the queue before it starts uploading
6. After all uploads complete, show the full list from `GET /api/uploads`

---

## Data Model

```typescript
// Stored on the server
interface UploadRecord {
  uploadId: string;       // uuid
  originalName: string;   // file.originalname from multer
  mimeType: string;       // file.mimetype
  sizeBytes: number;      // file.size
  uploadedAt: string;     // ISO 8601
  status: 'complete' | 'failed';
}

// Frontend only — not sent to the server
interface UploadQueueItem {
  id: string;             // local uuid for keying DOM nodes
  file: File;             // browser File object
  status: 'queued' | 'uploading' | 'complete' | 'failed';
  progress: number;       // 0–100
  uploadId?: string;      // set after server responds with 201
  error?: string;
}
```

---

## API Contract

### POST /api/uploads
**Request:** `multipart/form-data` with field name `file`
**Response 201:**
```json
{
  "uploadId": "abc-123",
  "status": "complete",
  "file": {
    "uploadId": "abc-123",
    "originalName": "report.pdf",
    "mimeType": "application/pdf",
    "sizeBytes": 204800,
    "uploadedAt": "2026-04-29T10:00:00.000Z",
    "status": "complete"
  }
}
```
**Response 400:** `{ "error": "No file provided" }`
**Response 413:** `{ "error": "File exceeds 10 MB limit" }`

### GET /api/uploads
**Response 200:** `{ "uploads": UploadRecord[] }` (newest first)

### GET /api/uploads/:uploadId
**Response 200:** `{ "upload": UploadRecord }`
**Response 404:** `{ "error": "Upload not found" }`

### DELETE /api/uploads/:uploadId
**Response 200:** `{ "message": "Upload deleted" }`
**Response 404:** `{ "error": "Upload not found" }`

---

## Stretch Goals (if time remains)

- Queue uploads sequentially (one at a time) instead of all concurrently
- File type validation: only allow images and PDFs; reject others with a friendly error in the UI
- Duplicate detection: if a file with the same name and size already exists, show a warning before uploading
- A "Retry" button for failed uploads
- Show total transfer speed (bytes/sec) across all active uploads

---

## Performance & Trade-off Discussion Points

1. **`XMLHttpRequest` vs `fetch` for progress:** `fetch` cannot expose upload progress
   in current browsers (the Streams API for request bodies is not yet finalized in browsers).
   XHR's `upload.onprogress` is the reliable, widely-supported option. When might `fetch`
   eventually replace XHR for this use case?

2. **`memoryStorage` vs `diskStorage` vs streaming to S3:** `memoryStorage` buffers the
   entire file in Node's heap. What is the maximum safe size per request? What happens
   when 50 users upload 10 MB files concurrently? *(500 MB in heap — likely OOM.)*
   How does `diskStorage` differ? How would you stream directly to S3?

3. **Sequential vs concurrent uploads:** Concurrent uploads finish faster in aggregate but
   compete for bandwidth. How would you decide the right concurrency limit?
   *(Analogy: connection pool size in databases.)*

4. **Progress bar granularity:** Browsers fire `progress` events at their own cadence,
   not per-byte. At what file size does the progress bar feel smooth vs jumping?
   Does chunked upload (multipart in S3 terms) help? Why?

5. **Memory growth on the server:** The `buffer` field inside multer's `memoryStorage`
   result holds the entire file in Node's heap. If you don't need the content (just
   metadata), what should you do with it? *(Delete `req.file.buffer` after saving metadata,
   or switch to `diskStorage` and immediately unlink the temp file.)*
