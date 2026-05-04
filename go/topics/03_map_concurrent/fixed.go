// Package mapconcurrent — two correct approaches.
//
// Option A: sync.RWMutex + plain map (good default, simple to reason about).
// Option B: sync.Map (good when: many goroutines, keys written once then read
//           many times, or disjoint key sets per goroutine).
//
// In most service code, Option A is clearer. Use sync.Map only when
// benchmarks show contention on the mutex.
package mapconcurrent

import "sync"

// Option A: Mutex-guarded map

type RequestRouterFixed struct {
	mu     sync.RWMutex
	routes map[string]string
}

func NewRequestRouterFixed() *RequestRouterFixed {
	return &RequestRouterFixed{routes: make(map[string]string)}
}

func (r *RequestRouterFixed) AddRoute(tunnelID, addr string) {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.routes[tunnelID] = addr
}

func (r *RequestRouterFixed) Route(tunnelID string) (string, bool) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	addr, ok := r.routes[tunnelID]
	return addr, ok
}

func (r *RequestRouterFixed) Remove(tunnelID string) {
	r.mu.Lock()
	defer r.mu.Unlock()
	delete(r.routes, tunnelID)
}

// Option B: sync.Map

type RequestRouterSyncMap struct {
	routes sync.Map // key: string tunnelID, value: string addr
}

func (r *RequestRouterSyncMap) AddRoute(tunnelID, addr string) {
	r.routes.Store(tunnelID, addr)
}

func (r *RequestRouterSyncMap) Route(tunnelID string) (string, bool) {
	v, ok := r.routes.Load(tunnelID)
	if !ok {
		return "", false
	}
	return v.(string), true
}

func (r *RequestRouterSyncMap) Remove(tunnelID string) {
	r.routes.Delete(tunnelID)
}
