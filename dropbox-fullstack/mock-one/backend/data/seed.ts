/*
 * Seed data — 20 realistic Dropbox-style file records.
 * Pre-populated so you can start building immediately without a real database.
 *
 * DISCUSSION — What does in-memory seed data lose vs a real DB?
 *   - Data resets on every server restart
 *   - Cannot be shared across multiple server instances
 *   - No durability (crash = data loss)
 *   - No querying (filtering is always a full linear scan)
 */

import { FileRecord } from '../server';

export const seedFiles: FileRecord[] = [
  { id: '1',  name: 'Q4 Budget',             extension: 'xlsx', sizeBytes: 204_800,    modifiedAt: '2026-04-01T09:00:00Z', owner: 'Alice',   path: '/finance/' },
  { id: '2',  name: 'Product Roadmap',        extension: 'pdf',  sizeBytes: 1_048_576,  modifiedAt: '2026-04-10T14:30:00Z', owner: 'Bob',     path: '/product/' },
  { id: '3',  name: 'Team Photo',             extension: 'jpg',  sizeBytes: 3_145_728,  modifiedAt: '2026-03-15T11:00:00Z', owner: 'Carol',   path: '/photos/' },
  { id: '4',  name: 'Meeting Notes',          extension: 'docx', sizeBytes: 51_200,     modifiedAt: '2026-04-28T16:00:00Z', owner: 'Alice',   path: '/docs/' },
  { id: '5',  name: 'Architecture Diagram',   extension: 'png',  sizeBytes: 819_200,    modifiedAt: '2026-04-20T10:00:00Z', owner: 'Dave',    path: '/eng/' },
  { id: '6',  name: 'Q1 Budget',              extension: 'xlsx', sizeBytes: 188_416,    modifiedAt: '2026-01-15T08:00:00Z', owner: 'Alice',   path: '/finance/' },
  { id: '7',  name: 'Design Mockups',         extension: 'fig',  sizeBytes: 5_242_880,  modifiedAt: '2026-04-25T13:00:00Z', owner: 'Eve',     path: '/design/' },
  { id: '8',  name: 'Onboarding Guide',       extension: 'pdf',  sizeBytes: 2_097_152,  modifiedAt: '2026-02-10T09:30:00Z', owner: 'Frank',   path: '/hr/' },
  { id: '9',  name: 'Sprint Retrospective',   extension: 'docx', sizeBytes: 73_728,     modifiedAt: '2026-04-18T17:00:00Z', owner: 'Bob',     path: '/eng/' },
  { id: '10', name: 'Logo Vector',            extension: 'svg',  sizeBytes: 24_576,     modifiedAt: '2026-03-01T12:00:00Z', owner: 'Eve',     path: '/design/' },
  { id: '11', name: 'User Research Report',   extension: 'pdf',  sizeBytes: 4_194_304,  modifiedAt: '2026-04-05T10:00:00Z', owner: 'Grace',   path: '/research/' },
  { id: '12', name: 'API Spec',               extension: 'yaml', sizeBytes: 32_768,     modifiedAt: '2026-04-22T11:00:00Z', owner: 'Dave',    path: '/eng/' },
  { id: '13', name: 'Holiday Party Photos',   extension: 'zip',  sizeBytes: 52_428_800, modifiedAt: '2025-12-20T18:00:00Z', owner: 'Carol',   path: '/photos/' },
  { id: '14', name: 'Investor Deck',          extension: 'pdf',  sizeBytes: 6_291_456,  modifiedAt: '2026-03-30T09:00:00Z', owner: 'Alice',   path: '/exec/' },
  { id: '15', name: 'Database Schema',        extension: 'sql',  sizeBytes: 16_384,     modifiedAt: '2026-04-12T14:00:00Z', owner: 'Dave',    path: '/eng/' },
  { id: '16', name: 'Employee Handbook',      extension: 'pdf',  sizeBytes: 3_145_728,  modifiedAt: '2026-01-02T08:00:00Z', owner: 'Frank',   path: '/hr/' },
  { id: '17', name: 'Test Plan',              extension: 'docx', sizeBytes: 98_304,     modifiedAt: '2026-04-15T10:30:00Z', owner: 'Grace',   path: '/qa/' },
  { id: '18', name: 'Conference Slides',      extension: 'pptx', sizeBytes: 8_388_608,  modifiedAt: '2026-04-08T08:00:00Z', owner: 'Bob',     path: '/marketing/' },
  { id: '19', name: 'Budget Forecast Model',  extension: 'xlsx', sizeBytes: 307_200,    modifiedAt: '2026-04-26T15:00:00Z', owner: 'Alice',   path: '/finance/' },
  { id: '20', name: 'Profile Photo',          extension: 'jpg',  sizeBytes: 512_000,    modifiedAt: '2026-02-14T10:00:00Z', owner: 'Carol',   path: '/photos/' },
];
