// Package ctxprop demonstrates context propagation mistakes.
//
// CONCEPT: context.Context carries a deadline, cancellation signal, and
// request-scoped values across API boundaries. Every blocking or long-running
// operation should accept a context so callers can cancel it.
//
// Think of context like a "cancel button" passed down the call stack. If you
// create a new context.Background() inside a function, you throw away the
// caller's cancel button — the work continues even after the caller gives up.
//
// COACHING PHRASES:
//   - "Creating context.Background() here throws away the caller's deadline —
//     if the HTTP request times out, the DB query will keep running."
//   - "Context belongs in function parameters, not struct fields. A struct
//     field context has no clear lifetime and leaks."
//   - "WithCancel/WithTimeout returns a cancel func — not calling it leaks a
//     timer goroutine until the parent context is done."
//
// SPOT THE BUGS:
//  1. HandleRequest ignores the passed ctx and creates a fresh one.
//  2. TunnelHandler stores a context in a struct field.
//  3. fetchMetrics creates a WithTimeout context but discards the cancel func.
package ctxprop

import (
	"context"
	"fmt"
	"net/http"
	"time"
)

// Bug 2: context stored in struct field — wrong lifetime, not request-scoped.
type TunnelHandler struct {
	ctx context.Context // Bug: should be a function parameter, not a field
}

func NewTunnelHandler() *TunnelHandler {
	return &TunnelHandler{
		ctx: context.Background(), // long-lived, never cancelled
	}
}

// Bug 1: ignores the caller's context. Creates a fresh Background() context,
// so the caller's deadline/cancellation has zero effect on queryTunnel.
func (h *TunnelHandler) HandleRequest(ctx context.Context, tunnelID string) error {
	freshCtx := context.Background() // Bug: throws away caller's context
	return queryTunnel(freshCtx, tunnelID)
}

// Bug 3: _ discards the cancel function from WithTimeout.
// The timer goroutine backing the timeout is never freed until the background
// context (never cancelled) is done — i.e., until the process exits.
func fetchMetrics(tunnelID string) {
	ctx, _ := context.WithTimeout(context.Background(), 5*time.Second) // _ leaks!
	resp, err := http.Get("http://metrics.internal/tunnel/" + tunnelID)
	if err != nil {
		fmt.Println("error:", err)
		_ = ctx
		return
	}
	fmt.Println(resp.Status)
}

func queryTunnel(ctx context.Context, id string) error {
	// Simulates a DB/network call that respects context cancellation.
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(10 * time.Millisecond):
		return nil
	}
}
