/*
 * Mock Two — Shared Link Manager: Backend Entry Point
 *
 * Architecture:
 *   In-memory store: Map<token, ShareLink>
 *   Status ("active" | "expired" | "revoked") is derived on each read — not stored.
 *
 * DISCUSSION — Why compute status on read instead of storing it?
 *   Storing status means it goes stale: a link stored as "active" becomes "expired"
 *   at some point in the future without any code running to update it.
 *   Derived status is always accurate. The trade-off: O(n) status computation on
 *   every GET /api/links request. Fine at small scale; add a sweep at large scale.
 */

import express from 'express';
import cors from 'cors';
import { linkRouter } from './routes/links';

export interface ShareLink {
  token: string;
  fileId: string;
  fileName: string;
  label: string;
  shareUrl: string;
  createdAt: string;
  expiresAt: string | null;
  revokedAt: string | null;
}

export type LinkStatus = 'active' | 'expired' | 'revoked';

export function getLinkStatus(link: ShareLink): LinkStatus {
  if (link.revokedAt !== null) return 'revoked';
  if (link.expiresAt !== null && new Date(link.expiresAt) < new Date()) return 'expired';
  return 'active';
}

// Hardcoded files available for sharing (simulates a user's Dropbox)
export const availableFiles: Record<string, string> = {
  'file-001': 'Q4 Budget.xlsx',
  'file-002': 'Product Roadmap.pdf',
  'file-003': 'Team Photo.jpg',
  'file-004': 'Architecture Diagram.png',
  'file-005': 'Onboarding Guide.pdf',
};

export const linkStore = new Map<string, ShareLink>();

const app = express();
const PORT = 3000;

app.use(cors());
app.use(express.json());

app.use('/api/links', linkRouter);

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
