// Package nilmap — two correct approaches.
//
// Option A (preferred): Constructor that calls make(). Zero value is not valid;
// document this clearly. Simple and obvious.
//
// Option B: Lazy initialization — makes zero value safe to use. More code per
// method; only use when zero-value usability is important (e.g., embedding).
package nilmap

import "sync"

// Option A: Constructor pattern

type TunnelRegistryFixed struct {
	mu      sync.Mutex
	tunnels map[string]string
}

// NewTunnelRegistry is the only correct way to create a TunnelRegistryFixed.
func NewTunnelRegistry() *TunnelRegistryFixed {
	return &TunnelRegistryFixed{
		tunnels: make(map[string]string),
	}
}

func (r *TunnelRegistryFixed) Register(id, addr string) {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.tunnels[id] = addr
}

func (r *TunnelRegistryFixed) Lookup(id string) (string, bool) {
	r.mu.Lock()
	defer r.mu.Unlock()
	addr, ok := r.tunnels[id]
	return addr, ok
}

// Option B: Lazy initialization (zero value safe)

type TunnelRegistryLazy struct {
	mu      sync.Mutex
	tunnels map[string]string
}

func (r *TunnelRegistryLazy) Register(id, addr string) {
	r.mu.Lock()
	defer r.mu.Unlock()
	if r.tunnels == nil {
		r.tunnels = make(map[string]string)
	}
	r.tunnels[id] = addr
}

func (r *TunnelRegistryLazy) Lookup(id string) (string, bool) {
	r.mu.Lock()
	defer r.mu.Unlock()
	addr, ok := r.tunnels[id]
	return addr, ok
}
