// Package mutex demonstrates reading shared state without holding the mutex.
//
// COACHING PHRASE: "Locks protect reads too, not just writes. An unguarded
// read while another goroutine writes is a data race."
//
// SPOT THE BUGS:
//  1. IsHealthy() reads m.healthy without acquiring the mutex.
//  2. ActiveTunnelCount() reads the map length without locking.
package mutex

import "sync"

type TunnelManager struct {
	mu      sync.Mutex
	healthy bool
	tunnels map[string]struct{}
}

func NewTunnelManager() *TunnelManager {
	return &TunnelManager{tunnels: make(map[string]struct{})}
}

func (m *TunnelManager) SetHealthy(h bool) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.healthy = h
}

func (m *TunnelManager) Register(id string) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.tunnels[id] = struct{}{}
}

// Bug: reads m.healthy without the lock — data race if SetHealthy runs concurrently.
func (m *TunnelManager) IsHealthy() bool {
	return m.healthy
}

// Bug: reads map without lock — data race.
func (m *TunnelManager) ActiveTunnelCount() int {
	return len(m.tunnels)
}
