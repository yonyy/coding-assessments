// Package capture — two fixes that work in both pre- and post-1.22 Go.
package capture

import (
	"fmt"
	"sync"
)

// Fix 1: Shadow the loop variable inside the loop body.
// Creates a new local variable per iteration — classic pre-1.22 idiom.
func LaunchTunnelWorkersShadow(ids []string) []string {
	var mu sync.Mutex
	var results []string
	var wg sync.WaitGroup

	for _, id := range ids {
		id := id // shadow: new variable scoped to this iteration
		wg.Add(1)
		go func() {
			defer wg.Done()
			mu.Lock()
			results = append(results, fmt.Sprintf("worker for %s", id))
			mu.Unlock()
		}()
	}

	wg.Wait()
	return results
}

// Fix 2: Pass as function argument — evaluated at call time, not when goroutine runs.
func LaunchTunnelWorkersArg(ids []string) []string {
	var mu sync.Mutex
	var results []string
	var wg sync.WaitGroup

	for _, id := range ids {
		wg.Add(1)
		go func(tunnelID string) { // pass by value — not a closure over id
			defer wg.Done()
			mu.Lock()
			results = append(results, fmt.Sprintf("worker for %s", tunnelID))
			mu.Unlock()
		}(id)
	}

	wg.Wait()
	return results
}
