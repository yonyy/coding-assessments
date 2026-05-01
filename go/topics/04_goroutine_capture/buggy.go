// Package capture demonstrates the classic loop variable capture bug.
//
// COACHING PHRASE: "Classic goroutine closure capture — all goroutines close
// over the same loop variable, so they all see the last value of id when they
// finally run. Pre-Go 1.22 only; Go 1.22+ gives each iteration its own variable."
//
// SPOT THE BUGS:
//  1. The goroutine captures `id` by reference. By the time the goroutine
//     runs, the loop may have advanced and `id` holds the last value.
//  2. Under Go 1.22 (our go.mod) this is actually fixed — but the pattern is
//     still worth knowing because you'll see it in pre-1.22 codebases.
package capture

import (
	"fmt"
	"sync"
)

// LaunchTunnelWorkersBuggy starts a worker goroutine per tunnel ID.
// Pre-Go 1.22: all workers log the same ID (the last one) due to closure capture.
// Go 1.22+: each iteration has its own loop variable, so this works correctly.
func LaunchTunnelWorkersBuggy(ids []string) []string {
	var mu sync.Mutex
	var results []string
	var wg sync.WaitGroup

	for _, id := range ids {
		wg.Add(1)
		go func() { // Pre-1.22 bug: captures `id` from enclosing scope
			defer wg.Done()
			mu.Lock()
			results = append(results, fmt.Sprintf("worker for %s", id))
			mu.Unlock()
		}()
	}

	wg.Wait()
	return results
}
