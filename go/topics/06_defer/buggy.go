// Package deferbehavior demonstrates two defer pitfalls.
//
// COACHING PHRASES:
//   - "Defer in a loop defers until the function returns, not the iteration —
//     you hold the lock for the entire loop duration."
//   - "Never manually unlock something you also defer-unlock — double unlock panics."
//
// SPOT THE BUGS:
//  1. processRequests() defers mu.Unlock() inside a loop. Every iteration
//     stacks another deferred unlock; none fire until the function returns.
//     This holds the lock for the whole loop, not releasing between iterations.
//  2. processWithNestedLock() manually unlocks secondary and also defers
//     secondary.Unlock() — the deferred call fires again on a released lock.
package deferbehavior

import (
	"fmt"
	"sync"
)

type RequestProcessor struct {
	mu       sync.Mutex
	requests []string
}

// Bug: defer inside a loop — the lock is held for ALL iterations.
func (p *RequestProcessor) processRequests() {
	for _, req := range p.requests {
		p.mu.Lock()
		defer p.mu.Unlock() // defers stack up; none fire until function ends
		fmt.Println("processing", req)
	}
}

// Bug: double unlock — defer fires after the manual Unlock, panicking.
func (p *RequestProcessor) processWithNestedLock(secondary *sync.Mutex) {
	p.mu.Lock()
	defer p.mu.Unlock()

	secondary.Lock()
	defer secondary.Unlock()
	secondary.Unlock() // Bug: manual unlock + deferred unlock = double unlock panic
}
