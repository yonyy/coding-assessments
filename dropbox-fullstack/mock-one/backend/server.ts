/*
 * Mock One — File Browser: Backend Entry Point
 *
 * Architecture:
 *   Express app with JSON body parsing and CORS.
 *   In-memory store: Map<id, FileRecord> initialized from seed data.
 *   Routes mounted at /api/files.
 *
 * DISCUSSION — Why Map and not an array?
 *   Map gives O(1) lookup by ID. Array lookup by ID is O(n).
 *   For listing all files we iterate Map.values() — same cost as an array.
 *   Trade-off: Map has slightly higher per-entry memory overhead.
 */

import express from 'express';
import cors from 'cors';
import { fileRouter } from './routes/files';
import { seedFiles } from './data/seed';

export interface FileRecord {
  id: string;
  name: string;
  extension: string;
  sizeBytes: number;
  modifiedAt: string;
  owner: string;
  path: string;
}

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());

// DISCUSSION — In-memory store trade-offs:
//   No persistence (resets on restart), single-process only, no durability.
//   First upgrade: add SQLite or PostgreSQL.
export const fileStore = new Map<string, FileRecord>(
  seedFiles.map(f => [f.id, f])
);

app.use('/api/files', fileRouter);

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
  console.log(`Seeded ${fileStore.size} files`);
});
