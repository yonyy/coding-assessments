/*
 * Mock Three — File Upload: Frontend Logic
 *
 * Key implementation notes:
 *   - Use XMLHttpRequest (NOT fetch) for uploads — XHR exposes upload.onprogress;
 *     fetch does not yet have a cross-browser upload progress API.
 *   - Each file in the queue has a local `id` (crypto.randomUUID) for keying DOM rows.
 *   - State is an array of UploadQueueItem; all DOM updates go through renderQueue().
 *
 * DISCUSSION — Why XMLHttpRequest for upload progress?
 *   The Fetch API's WritableStream support for request bodies is still being standardized.
 *   XHR's upload.onprogress fires as bytes are sent, giving reliable per-file progress.
 *   Use XHR for uploads; use fetch for everything else (GET, DELETE, etc.).
 */

const API_BASE = 'http://localhost:3000/api';

// ── State ─────────────────────────────────────────────────────────────────────
// queue: Array<{ id, file, status, progress, uploadId?, error? }>
let queue = [];

// ── DOM refs ──────────────────────────────────────────────────────────────────
const dropZone       = document.getElementById('drop-zone');
const fileInput      = document.getElementById('file-input');
const uploadQueue    = document.getElementById('upload-queue');
const queueList      = document.getElementById('queue-list');
const uploadsSection = document.getElementById('uploads-section');
const uploadsList    = document.getElementById('uploads-list');
const uploadsLoading = document.getElementById('uploads-loading');

// ── Utilities ─────────────────────────────────────────────────────────────────

/**
 * Format bytes as a human-readable string: "4.2 MB", "320 KB", "800 B"
 * @param {number} bytes
 * @returns {string}
 */
function formatBytes(bytes) {
  // TODO: implement (same as mock-one)
}

/**
 * Format an ISO date string: "Apr 29, 2026 at 10:00 AM"
 * @param {string} isoString
 * @returns {string}
 */
function formatDateTime(isoString) {
  // TODO: use Intl.DateTimeFormat
}

// ── Queue management ──────────────────────────────────────────────────────────

/**
 * Add files from a FileList to the queue (skip duplicates by name+size).
 * @param {FileList} fileList
 */
function addFilesToQueue(fileList) {
  // TODO: for each file in fileList:
  //   skip if queue already has an item with same name + size
  //   push { id: crypto.randomUUID(), file, status: 'queued', progress: 0 }
  // Then: renderQueue(), show uploadQueue section, start uploading
}

/**
 * Remove a queued (not-yet-started) item from the queue.
 * @param {string} id — local queue item id
 */
function removeFromQueue(id) {
  // TODO: filter queue, renderQueue()
  // Only allow removal of items with status === 'queued'
}

// ── Rendering ─────────────────────────────────────────────────────────────────

/**
 * Build a <li class="queue-item"> for one queue item.
 * @param {{ id, file, status, progress, error? }} item
 * @returns {HTMLLIElement}
 */
function buildQueueItem(item) {
  // TODO: create li.queue-item with:
  //   .queue-item__header: name, size, "×" remove button (only if status === 'queued')
  //   .progress-bar > .progress-bar__fill (width = item.progress + '%')
  //     add --complete or --failed modifier class based on status
  //   .queue-item__status: "Queued" / "Uploading X%" / "Complete" / "Failed: {error}"
  //     add modifier class based on status
}

/**
 * Render the full queue list from state.
 */
function renderQueue() {
  queueList.innerHTML = '';
  // TODO: buildQueueItem for each item and append
}

/**
 * Update a single queue item in the DOM without re-rendering the whole list.
 * @param {string} id
 */
function updateQueueItemInDOM(id) {
  // OPTIONAL OPTIMIZATION: instead of full renderQueue(),
  // find the existing <li data-id="{id}"> and update only its progress + status
  // This avoids flickering during rapid progress events.
  // Simpler path: just call renderQueue() (acceptable for the interview).
  renderQueue();
}

// ── Upload logic ──────────────────────────────────────────────────────────────

/**
 * Upload all queued items concurrently using XHR.
 * After all finish, fetch the completed uploads list.
 */
function startUploads() {
  const pending = queue.filter(item => item.status === 'queued');
  if (pending.length === 0) return;

  const uploadPromises = pending.map(item => uploadFile(item));

  Promise.allSettled(uploadPromises).then(() => {
    fetchUploadedFiles();
    uploadsSection.hidden = false;
  });
}

/**
 * Upload one file using XHR. Returns a Promise that resolves when the upload finishes
 * (either complete or failed — never rejects, so Promise.allSettled works cleanly).
 *
 * @param {{ id: string, file: File }} item
 * @returns {Promise<void>}
 *
 * DISCUSSION — Why XHR and not fetch here?
 *   fetch does not expose upload progress (no upload.onprogress equivalent).
 *   XHR's upload.addEventListener('progress', handler) fires as bytes are sent.
 */
function uploadFile(item) {
  return new Promise((resolve) => {
    // TODO: Step 1 — set item.status = 'uploading', item.progress = 0, updateQueueItemInDOM

    // TODO: Step 2 — build FormData with field name "file"
    // const formData = new FormData();
    // formData.append('file', item.file);

    // TODO: Step 3 — create and configure XHR
    // const xhr = new XMLHttpRequest();
    // xhr.open('POST', API_BASE + '/uploads');

    // TODO: Step 4 — wire up upload.onprogress
    // xhr.upload.addEventListener('progress', (e) => {
    //   if (e.lengthComputable) {
    //     item.progress = Math.round((e.loaded / e.total) * 100);
    //     updateQueueItemInDOM(item.id);
    //   }
    // });

    // TODO: Step 5 — wire up xhr.onload
    // xhr.addEventListener('load', () => {
    //   if (xhr.status === 201) {
    //     const data = JSON.parse(xhr.responseText);
    //     item.status = 'complete';
    //     item.progress = 100;
    //     item.uploadId = data.uploadId;
    //   } else {
    //     item.status = 'failed';
    //     item.error = JSON.parse(xhr.responseText).error ?? 'Upload failed';
    //   }
    //   updateQueueItemInDOM(item.id);
    //   resolve();
    // });

    // TODO: Step 6 — wire up xhr.onerror (network failure)
    // xhr.addEventListener('error', () => {
    //   item.status = 'failed';
    //   item.error = 'Network error';
    //   updateQueueItemInDOM(item.id);
    //   resolve();
    // });

    // TODO: Step 7 — send
    // xhr.send(formData);
  });
}

// ── Uploaded files list ───────────────────────────────────────────────────────

/**
 * Fetch completed uploads from the server and render the list below the queue.
 */
async function fetchUploadedFiles() {
  uploadsLoading.hidden = false;
  uploadsList.innerHTML = '';

  try {
    // TODO: GET /api/uploads → parse { uploads } → render upload rows
  } catch (err) {
    console.error('Failed to load uploads:', err);
  } finally {
    uploadsLoading.hidden = true;
  }
}

/**
 * Delete an upload record from the server, then re-fetch the list.
 * @param {string} uploadId
 */
async function deleteUpload(uploadId) {
  // TODO: DELETE /api/uploads/:uploadId, then fetchUploadedFiles()
}

/**
 * Build an <li class="upload-row"> for one completed UploadRecord.
 * @param {{ uploadId, originalName, mimeType, sizeBytes, uploadedAt }} record
 * @returns {HTMLLIElement}
 */
function buildUploadRow(record) {
  // TODO: li.upload-row with name, mime badge, size, uploadedAt, delete button
}

// ── Drop zone event wiring ────────────────────────────────────────────────────

// Click on drop zone → trigger hidden file input
dropZone.addEventListener('click', () => fileInput.click());

// Keyboard accessibility — Enter/Space activates the drop zone
dropZone.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); fileInput.click(); }
});

// File input change (classic browser dialog)
fileInput.addEventListener('change', () => {
  if (fileInput.files?.length) {
    addFilesToQueue(fileInput.files);
    fileInput.value = ''; // reset so same file can be re-added after removal
  }
});

// Drag and drop
dropZone.addEventListener('dragover', (e) => {
  e.preventDefault();
  dropZone.classList.add('drag-over');
});
dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'));
dropZone.addEventListener('drop', (e) => {
  e.preventDefault();
  dropZone.classList.remove('drag-over');
  // TODO: addFilesToQueue(e.dataTransfer.files)
});

// ── Init ──────────────────────────────────────────────────────────────────────
fetchUploadedFiles().then(() => { uploadsSection.hidden = false; });
