/*
 * GET  /api/activity          — cursor-paginated event list
 * GET  /api/activity/:eventId — single event
 * POST /api/activity/simulate — generate 1–5 new events on demand
 *
 * CURSOR PAGINATION — How it works:
 *   eventLog is sorted oldest-first (index 0 = oldest).
 *   "newest first" for the response = reverse order.
 *   cursor = eventId of the last event the client saw.
 *   "newer than cursor" = events that appear AFTER the cursor in the log.
 *
 *   Algorithm for GET /api/activity?cursor=evt_0010&limit=5:
 *     1. Find cursorIndex = eventLog.findIndex(e => e.eventId === cursor)
 *     2. If not found: return 400
 *     3. newer = eventLog.slice(cursorIndex + 1)   // events after cursor
 *     4. Reverse newer (newest first), slice to limit
 *     5. nextCursor = newer[newer.length - 1].eventId  (most recent returned)
 *
 * DISCUSSION — Why cursor and not offset (?page=2)?
 *   Offset breaks with live data. If 3 new events arrive between page 1 and page 2
 *   requests, page 2 shows the same 3 events that were at the bottom of page 1.
 *   Cursor is stable: "give me events after ID X" is unaffected by new arrivals.
 */

import { Router, Request, Response } from 'express';
import { eventLog, incrementCounter, ActivityType, ActivityEvent } from '../server';

export const activityRouter = Router();

const SAMPLE_FILES = [
  { name: 'Q4 Budget.xlsx',           path: '/finance/' },
  { name: 'Product Roadmap.pdf',       path: '/product/' },
  { name: 'Team Photo.jpg',            path: '/photos/' },
  { name: 'Architecture Diagram.png',  path: '/eng/' },
  { name: 'Meeting Notes.docx',        path: '/docs/' },
  { name: 'Design Mockups.fig',        path: '/design/' },
  { name: 'Onboarding Guide.pdf',      path: '/hr/' },
  { name: 'API Spec.yaml',             path: '/eng/' },
  { name: 'Conference Slides.pptx',    path: '/marketing/' },
  { name: 'Employee Handbook.pdf',     path: '/hr/' },
];

const SAMPLE_USERS = ['Alice', 'Bob', 'Carol', 'Dave', 'Eve', 'Frank'];
const ACTIVITY_TYPES: ActivityType[] = ['upload', 'download', 'edit', 'share', 'delete'];

/**
 * Generate one synthetic ActivityEvent. Exported so server.ts can use it for seeding.
 * @param {string} [timestamp] — override timestamp (for seeding historical events)
 */
export function generateEvent(timestamp?: string): ActivityEvent {
  const id = incrementCounter();
  const file = SAMPLE_FILES[Math.floor(Math.random() * SAMPLE_FILES.length)];
  const type = ACTIVITY_TYPES[Math.floor(Math.random() * ACTIVITY_TYPES.length)];
  const user = SAMPLE_USERS[Math.floor(Math.random() * SAMPLE_USERS.length)];

  const event: ActivityEvent = {
    eventId: `evt_${String(id).padStart(4, '0')}`,
    type,
    fileName: file.name,
    filePath: file.path,
    user,
    timestamp: timestamp ?? new Date().toISOString(),
  };

  if (type === 'share') {
    const recipient = SAMPLE_USERS.filter(u => u !== user)[Math.floor(Math.random() * (SAMPLE_USERS.length - 1))];
    event.metadata = { sharedWith: `${recipient.toLowerCase()}@example.com` };
  }

  return event;
}

// GET /api/activity
activityRouter.get('/', (req: Request, res: Response) => {
  const limitParam = parseInt(req.query['limit'] as string ?? '20', 10);
  const limit = isNaN(limitParam) ? 20 : Math.min(limitParam, 100);
  const cursor = req.query['cursor'] as string | undefined;

  // TODO: Step 1 — if no cursor, return the most recent `limit` events (newest first)
  // HINT: eventLog.slice(-limit).reverse()
  // nextCursor = the eventId of the first element in the returned array (most recent)
  // hasMore = eventLog.length > limit

  // TODO: Step 2 — if cursor provided, find its index
  // const cursorIndex = eventLog.findIndex(e => e.eventId === cursor);
  // if (cursorIndex === -1) return res.status(400).json({ error: 'Unknown cursor' });

  // TODO: Step 3 — slice events newer than cursor, reverse, limit
  // const newer = eventLog.slice(cursorIndex + 1).reverse().slice(0, limit);
  // nextCursor = newer[0]?.eventId ?? cursor  (if no new events, cursor stays the same)
  // hasMore = eventLog.slice(cursorIndex + 1).length > limit

  // TODO: Step 4 — return { events, nextCursor, hasMore }
});

// GET /api/activity/:eventId
// NOTE: this route must be declared BEFORE POST /simulate to avoid route conflicts
activityRouter.get('/:eventId', (req: Request, res: Response) => {
  // TODO: find event in eventLog by eventId
  // 200 → { event }  or  404 → { error: 'Event not found' }
});

// POST /api/activity/simulate
activityRouter.post('/simulate', (_req: Request, res: Response) => {
  // TODO: generate a random number of events (1–5)
  // const count = Math.floor(Math.random() * 5) + 1;
  // const newEvents = Array.from({ length: count }, () => generateEvent());
  // newEvents.forEach(e => eventLog.push(e));
  // return 201 with { generated: count, events: newEvents }
});
