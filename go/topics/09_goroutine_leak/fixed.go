// Package goroutineleak — corrected version.
//
// FIX: Every goroutine needs an exit path. The idiomatic way is:
//   - Accept a context.Context and select on ctx.Done()
//   - Use a buffered channel of size 1 so the sender never blocks
//
// Rule: if you start a goroutine, you are responsible for being able to stop it.
package goroutineleak

import (
	"context"
	"fmt"
	"time"
)

type TunnelMonitorFixed struct {
	tunnelID string
}

// Fix: accept context — caller controls the goroutine's lifetime.
// time.NewTicker + select is the idiomatic "do work on interval until cancelled" pattern.
func (m *TunnelMonitorFixed) StartHealthCheck(ctx context.Context) {
	go func() {
		ticker := time.NewTicker(time.Second)
		defer ticker.Stop() // always stop the ticker to free its goroutine
		for {
			select {
			case <-ctx.Done():
				return // clean exit when caller cancels
			case <-ticker.C:
				fmt.Printf("checking %s\n", m.tunnelID)
			}
		}
	}()
}

// Fix: buffered channel of size 1 — goroutine can always send, even if
// caller returns early. Also select on ctx.Done() for timeout/cancel.
func (m *TunnelMonitorFixed) CollectMetric(ctx context.Context) (int, error) {
	ch := make(chan int, 1) // buffered: sender never blocks
	go func() {
		ch <- doWorkFixed()
	}()
	select {
	case result := <-ch:
		return result, nil
	case <-ctx.Done():
		return 0, ctx.Err() // caller cancelled or timed out — goroutine still exits cleanly
	}
}

func doWorkFixed() int { return 42 }
