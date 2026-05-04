/*
 * Mock Four — Activity Feed: Backend Entry Point
 *
 * Architecture:
 *   eventLog: ActivityEvent[]  — append-only array, oldest at index 0.
 *   eventCounter: number       — used to generate monotonic IDs: "evt_0001"
 *   setInterval fires every 10s to simulate a background file activity stream.
 *
 * DISCUSSION — Why an array (not a Map) for the event log?
 *   Events are append-only and are queried by position (cursor = find by ID then slice).
 *   A Map would give O(1) ID lookup but sequential access would still be O(n).
 *   For this workload, an array is simpler and equally fast.
 *
 * DISCUSSION — Event log growth (memory leak risk):
 *   Events accumulate forever. At 1 event/10s, 1 million events = ~100 MB.
 *   Production fix: ring buffer (keep last N events) or offload to a DB.
 *   Simple ring buffer: if (eventLog.length > MAX_EVENTS) eventLog.shift()
 */

import express from 'express';
import cors from 'cors';
import { activityRouter, generateEvent } from './routes/activity';

export type ActivityType = 'upload' | 'download' | 'edit' | 'share' | 'delete';

export interface ActivityEvent {
  eventId: string;
  type: ActivityType;
  fileName: string;
  filePath: string;
  user: string;
  timestamp: string;
  metadata?: Record<string, string>;
}

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());

// Shared mutable state — exported so routes can read/write
export const eventLog: ActivityEvent[] = [];
export let eventCounter = 0;
export function incrementCounter(): number { return ++eventCounter; }

// Seed with 15 historical events on startup
for (let i = 0; i < 15; i++) {
  const secondsAgo = (15 - i) * 45;
  eventLog.push(generateEvent(new Date(Date.now() - secondsAgo * 1000).toISOString()));
}

// Auto-generate one new event every 10 seconds
setInterval(() => {
  eventLog.push(generateEvent());
  console.log(`[auto] event generated — total: ${eventLog.length}`);
}, 10_000);

app.use('/api/activity', activityRouter);

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
  console.log(`Seeded ${eventLog.length} events`);
});
