// Package deferbehavior — corrected version.
//
// FIX for defer-in-loop: extract the loop body into a helper so defer fires
// per call, or use explicit unlock (no defer) within the loop body.
package deferbehavior

import (
	"fmt"
	"sync"
)

type RequestProcessorFixed struct {
	mu       sync.Mutex
	requests []string
}

// Fix A: Extract to helper — defer fires when processOne returns (each iteration).
func (p *RequestProcessorFixed) processRequests() {
	for _, req := range p.requests {
		p.processOne(req)
	}
}

func (p *RequestProcessorFixed) processOne(req string) {
	p.mu.Lock()
	defer p.mu.Unlock()
	fmt.Println("processing", req)
}

// Fix B: Explicit unlock — no defer, releases each iteration.
func (p *RequestProcessorFixed) processRequestsExplicit() {
	for _, req := range p.requests {
		p.mu.Lock()
		fmt.Println("processing", req)
		p.mu.Unlock()
	}
}

// Fix for nested lock: only deferred unlock — no manual unlock.
func (p *RequestProcessorFixed) processWithNestedLock(secondary *sync.Mutex) {
	p.mu.Lock()
	defer p.mu.Unlock()

	secondary.Lock()
	defer secondary.Unlock() // only one unlock — the deferred one
}
