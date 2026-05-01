package mapconcurrent

import (
	"sync"
	"testing"
)


// TestConcurrentMapWritesDocumented documents the concurrent map bug.
// NOTE: actually running the buggy code causes a non-recoverable runtime fatal:
// "concurrent map read and map write" — the test runner would crash.
// To see it: copy the body below into scratch/main.go and run make scratch.
//
// The bug: Go maps have no internal locking. Any concurrent write to any key
// while another goroutine reads OR writes is undefined behavior and will fatal.
func TestConcurrentMapWritesDocumented(t *testing.T) {
	t.Log("BUG: RequestRouter has no synchronization on its map.")
	t.Log("Concurrent AddRoute/Route goroutines cause: 'fatal: concurrent map read and map write'")
	t.Log("This is a runtime fatal — NOT a recoverable panic.")
	t.Log("Fix: guard with sync.RWMutex (RequestRouterFixed) or use sync.Map (RequestRouterSyncMap)")
}

// TestFixedRouterNoRace verifies the mutex-guarded version is clean.
func TestFixedRouterNoRace(t *testing.T) {
	r := NewRequestRouterFixed()
	var wg sync.WaitGroup

	for i := 0; i < 200; i++ {
		wg.Add(3)
		id := "tunnel-" + string(rune('A'+i%26))
		go func(id string) {
			defer wg.Done()
			r.AddRoute(id, "backend:9090")
		}(id)
		go func(id string) {
			defer wg.Done()
			_, _ = r.Route(id)
		}(id)
		go func(id string) {
			defer wg.Done()
			r.Remove(id)
		}(id)
	}
	wg.Wait()
}
