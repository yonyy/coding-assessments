/*
 * POST /api/links          — create a share link
 * GET  /api/links          — list all links with computed status
 * GET  /api/links/:token   — resolve a token (410 if expired, 404 if unknown)
 * DELETE /api/links/:token — revoke a link (soft delete: set revokedAt)
 *
 * PERFORMANCE — Status is derived on read (O(1) per link, O(n) for the list).
 *   At millions of links you would add a background sweep that marks expired links
 *   so the GET /api/links query can filter by a stored status field.
 *
 * SECURITY — Token generation uses crypto.randomBytes (CSPRNG) not Math.random.
 *   Math.random is not cryptographically secure: tokens could be predicted/enumerated.
 */

import { Router, Request, Response } from 'express';
import { randomBytes } from 'crypto';
import { linkStore, availableFiles, getLinkStatus, ShareLink } from '../server';

export const linkRouter = Router();

function generateToken(): string {
  // 9 random bytes → 12 base64url characters (no padding, URL-safe)
  return randomBytes(9).toString('base64url');
}

// POST /api/links
linkRouter.post('/', (req: Request, res: Response) => {
  const { fileId, expiresInSeconds, label } = req.body as {
    fileId?: string;
    expiresInSeconds?: number;
    label?: string;
  };

  // TODO: Step 1 — validate fileId is present and exists in availableFiles
  // 400 → res.status(400).json({ error: 'fileId is required' })
  // 400 → res.status(400).json({ error: 'Unknown fileId' })

  // TODO: Step 2 — build the ShareLink object
  //   token       = generateToken()
  //   fileName    = availableFiles[fileId]  (denormalized)
  //   shareUrl    = `http://localhost:${PORT}/s/${token}`  — hardcode port 3000
  //   createdAt   = new Date().toISOString()
  //   expiresAt   = expiresInSeconds
  //                   ? new Date(Date.now() + expiresInSeconds * 1000).toISOString()
  //                   : null
  //   revokedAt   = null
  //   label       = label?.trim() ?? ''

  // TODO: Step 3 — store in linkStore, return 201 with { link }
});

// GET /api/links
linkRouter.get('/', (_req: Request, res: Response) => {
  // TODO: iterate linkStore.values(), attach status via getLinkStatus(link)
  // Sort: newest first (compare createdAt descending)
  // Return 200 with { links: Array<ShareLink & { status }> }
});

// GET /api/links/:token
linkRouter.get('/:token', (req: Request, res: Response) => {
  // TODO: look up linkStore.get(req.params.token)
  // 404 → { error: 'Link not found' }
  // Compute status:
  //   'revoked' → 404 { error: 'Link has been revoked' }
  //   'expired' → 410 { error: 'Link has expired' }
  //   'active'  → 200 { link }
  //
  // DISCUSSION — Why 410 for expired and not 404?
  //   410 Gone means "this resource existed but is permanently gone."
  //   CDNs will cache 410 responses; they typically do not cache 404.
});

// DELETE /api/links/:token
linkRouter.delete('/:token', (req: Request, res: Response) => {
  // TODO: look up the link; 404 if not found
  // Set link.revokedAt = new Date().toISOString()
  // Return 200 with { message: 'Link revoked' }
  //
  // DISCUSSION — Why soft-delete (revokedAt) instead of hard-delete (linkStore.delete)?
  //   revokedAt is an audit trail. "When was this revoked?" is needed for support logs.
  //   General principle: prefer timestamps to booleans for state transitions.
});
