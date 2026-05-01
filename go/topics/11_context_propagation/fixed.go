// Package ctxprop — corrected version.
//
// Rules:
//  1. Pass context as the FIRST parameter of every function that does I/O or blocks.
//  2. Never store context in a struct field — it belongs in function params.
//  3. Always defer cancel() immediately after WithTimeout/WithCancel.
//  4. Never create context.Background() inside a function that received a context.
package ctxprop

import (
	"context"
	"fmt"
	"net/http"
	"time"
)

// Fix 2: no context field — context flows through method parameters per request.
type TunnelHandlerFixed struct{}

func NewTunnelHandlerFixed() *TunnelHandlerFixed {
	return &TunnelHandlerFixed{}
}

// Fix 1: pass the caller's context straight through — never create a new one.
func (h *TunnelHandlerFixed) HandleRequest(ctx context.Context, tunnelID string) error {
	return queryTunnelFixed(ctx, tunnelID) // caller's deadline/cancel propagates
}

// Fix 3: always capture and defer cancel().
// Use http.NewRequestWithContext to attach context to HTTP calls.
func fetchMetricsFixed(tunnelID string) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel() // always — frees the timer goroutine immediately on return

	req, err := http.NewRequestWithContext(ctx, http.MethodGet,
		"http://metrics.internal/tunnel/"+tunnelID, nil)
	if err != nil {
		fmt.Println("error building request:", err)
		return
	}
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		fmt.Println("error:", err)
		return
	}
	fmt.Println(resp.Status)
}

func queryTunnelFixed(ctx context.Context, id string) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(10 * time.Millisecond):
		return nil
	}
}
