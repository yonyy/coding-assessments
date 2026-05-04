/*
 * GET /api/files    — list files with optional search, type filter, and sort
 * GET /api/files/:id — get one file by ID
 *
 * Query params for GET /api/files:
 *   search  — case-insensitive substring match on name
 *   type    — exact match on extension (e.g. "pdf")
 *   sort    — "name" | "size" | "modifiedAt"  (default: "name")
 *   order   — "asc" | "desc"                  (default: "asc")
 *
 * PERFORMANCE — All filtering and sorting happens in-process on every request.
 *   O(n) per request. Fine for hundreds of files; add a DB index at 10k+.
 *
 * DISCUSSION — Why sort server-side and not client-side?
 *   Server-side sort works correctly with pagination (sort before slicing).
 *   Client-side sort requires all data to be in memory — breaks with pagination.
 */

import { Router, Request, Response } from 'express';
import { fileStore, FileRecord } from '../server';

export const fileRouter = Router();

// GET /api/files
fileRouter.get('/', (req: Request, res: Response) => {
  const { search, type, sort = 'name', order = 'asc' } = req.query as Record<string, string>;

  // TODO: Step 1 — get all files from fileStore as an array
  let files: FileRecord[] = [];

  // TODO: Step 2 — apply search filter (case-insensitive substring match on name)
  // HINT: .toLowerCase() + .includes()

  // TODO: Step 3 — apply type filter (exact match on extension)

  // TODO: Step 4 — sort by the requested field
  // sort === 'name'       → compare file.name lexicographically
  // sort === 'size'       → compare file.sizeBytes numerically
  // sort === 'modifiedAt' → compare ISO strings (lexicographic == chronological)
  // DISCUSSION: What should happen if sort is an unexpected value? 400 or silently default?

  // TODO: Step 5 — reverse the array if order === 'desc'

  res.json({ files, total: files.length });
});

// GET /api/files/:id
fileRouter.get('/:id', (req: Request, res: Response) => {
  // TODO: look up fileStore.get(req.params.id)
  // 200 → res.json({ file })
  // 404 → res.status(404).json({ error: 'File not found' })
});
