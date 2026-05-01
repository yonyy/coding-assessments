/*
 * POST /api/uploads          — receive a multipart file, store metadata
 * GET  /api/uploads          — list all uploads (newest first)
 * GET  /api/uploads/:uploadId — get one upload by ID
 * DELETE /api/uploads/:uploadId — remove an upload record
 *
 * PERFORMANCE — multer buffers the entire file in memory before this handler runs.
 *   req.file.buffer is the full content. We immediately discard it (don't store it)
 *   to keep heap usage minimal. Only metadata goes into uploadStore.
 *
 * ERROR HANDLING — multer throws a MulterError with code LIMIT_FILE_SIZE when the
 *   file exceeds the configured limit. Catch it in the error handler and return 413.
 */

import { Router, Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { upload, uploadStore, UploadRecord } from '../server';
import multer from 'multer';

export const uploadRouter = Router();

// POST /api/uploads
uploadRouter.post(
  '/',
  upload.single('file'),   // expects multipart field named "file"
  (req: Request, res: Response) => {
    // TODO: Step 1 — check req.file exists; 400 if not
    // if (!req.file) return res.status(400).json({ error: 'No file provided' });

    // TODO: Step 2 — build UploadRecord (do NOT store req.file.buffer)
    //   uploadId    = uuidv4()
    //   originalName = req.file.originalname
    //   mimeType    = req.file.mimetype
    //   sizeBytes   = req.file.size
    //   uploadedAt  = new Date().toISOString()
    //   status      = 'complete'

    // TODO: Step 3 — store in uploadStore, return 201
    // res.status(201).json({ uploadId, status: 'complete', file: record })
  }
);

// Multer error handler — must have 4 parameters to be recognized as error middleware
uploadRouter.use((err: unknown, _req: Request, res: Response, _next: NextFunction) => {
  if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
    return res.status(413).json({ error: 'File exceeds 10 MB limit' });
  }
  return res.status(500).json({ error: 'Upload failed' });
});

// GET /api/uploads
uploadRouter.get('/', (_req: Request, res: Response) => {
  // TODO: get all uploads from uploadStore, sort newest first, return { uploads }
  // HINT: sort by uploadedAt descending (ISO strings sort lexicographically)
});

// GET /api/uploads/:uploadId
uploadRouter.get('/:uploadId', (req: Request, res: Response) => {
  // TODO: look up uploadStore.get(req.params.uploadId)
  // 200 → { upload }  or  404 → { error: 'Upload not found' }
});

// DELETE /api/uploads/:uploadId
uploadRouter.delete('/:uploadId', (req: Request, res: Response) => {
  // TODO: look up, 404 if missing, uploadStore.delete(), return { message: 'Upload deleted' }
});
