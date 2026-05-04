// Package mutex — corrected version.
//
// FIX: Every access to shared state — read or write — must be inside a lock.
// Prefer sync.RWMutex for read-heavy workloads: RLock/RUnlock for reads
// allows concurrent readers, Lock/Unlock for writes is exclusive.
package mutex

import "sync"

type TunnelManagerFixed struct {
	mu      sync.RWMutex
	healthy bool
	tunnels map[string]struct{}
}

func NewTunnelManagerFixed() *TunnelManagerFixed {
	return &TunnelManagerFixed{tunnels: make(map[string]struct{})}
}

func (m *TunnelManagerFixed) SetHealthy(h bool) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.healthy = h
}

func (m *TunnelManagerFixed) Register(id string) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.tunnels[id] = struct{}{}
}

// RLock allows multiple concurrent readers without blocking each other.
func (m *TunnelManagerFixed) IsHealthy() bool {
	m.mu.RLock()
	defer m.mu.RUnlock()
	return m.healthy
}

func (m *TunnelManagerFixed) ActiveTunnelCount() int {
	m.mu.RLock()
	defer m.mu.RUnlock()
	return len(m.tunnels)
}
