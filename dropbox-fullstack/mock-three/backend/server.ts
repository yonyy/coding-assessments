/*
 * Mock Three — File Upload: Backend Entry Point
 *
 * Architecture:
 *   multer with memoryStorage — file buffer lives in Node's heap.
 *   In-memory store: Map<uploadId, UploadRecord> (no buffer stored — just metadata).
 *
 * DISCUSSION — memoryStorage trade-offs:
 *   The full file is buffered in RAM before your handler runs.
 *   Max safe size: ~50 MB total across concurrent requests before OOM risk.
 *   Alternatives:
 *     diskStorage   → write to filesystem first (better for large files)
 *     S3 stream     → pipe directly to object storage (production approach)
 *
 * DISCUSSION — Why discard req.file.buffer after saving metadata?
 *   We only need metadata (name, size, type). Holding the buffer wastes heap.
 *   After storing metadata, the buffer is garbage-collected automatically when
 *   the request ends — we just don't store a reference to it.
 */

import express from 'express';
import cors from 'cors';
import multer from 'multer';
import { uploadRouter } from './routes/uploads';

export interface UploadRecord {
  uploadId: string;
  originalName: string;
  mimeType: string;
  sizeBytes: number;
  uploadedAt: string;
  status: 'complete' | 'failed';
}

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());

// 10 MB size limit — multer will throw a LIMIT_FILE_SIZE error above this
export const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 },
});

export const uploadStore = new Map<string, UploadRecord>();

app.use('/api/uploads', uploadRouter);

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
