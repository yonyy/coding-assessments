package mutex

import (
	"sync"
	"testing"
)

// TestUnguardedReadRace triggers the data race in IsHealthy().
// Run with: go test -race ./topics/02_mutex/
//
// The race detector will print something like:
//
//	DATA RACE
//	Read at 0x... by goroutine ...:
//	  mutex.(*TunnelManager).IsHealthy(...)
//	Previous write at 0x... by goroutine ...:
//	  mutex.(*TunnelManager).SetHealthy(...)
func TestUnguardedReadRace(t *testing.T) {
	m := NewTunnelManager()
	var wg sync.WaitGroup

	for i := 0; i < 100; i++ {
		wg.Add(2)
		go func() {
			defer wg.Done()
			m.SetHealthy(true)
		}()
		go func() {
			defer wg.Done()
			_ = m.IsHealthy()
		}()
	}
	wg.Wait()
}

// TestFixedNoRace shows the corrected version is clean under -race.
func TestFixedNoRace(t *testing.T) {
	m := NewTunnelManagerFixed()
	var wg sync.WaitGroup

	for i := 0; i < 100; i++ {
		wg.Add(2)
		go func() {
			defer wg.Done()
			m.SetHealthy(true)
		}()
		go func() {
			defer wg.Done()
			_ = m.IsHealthy()
		}()
	}
	wg.Wait()
}
