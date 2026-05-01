package waitgroup

import (
	"sync"
	"testing"
	"time"
)

// TestAddBeforeGoroutineIsRequired shows the correct pattern and explains the race.
func TestAddBeforeGoroutineIsRequired(t *testing.T) {
	t.Log("BUG: wg.Add inside goroutine races with wg.Wait().")
	t.Log("If Wait() is reached before some goroutines call Add, Wait returns early.")
	t.Log("If a goroutine calls Done before Add, the counter goes negative — panic.")

	// Demonstrate the correct pattern:
	var wg sync.WaitGroup
	results := make(chan string, 3)

	ids := []string{"tun-1", "tun-2", "tun-3"}
	for _, id := range ids {
		wg.Add(1) // Add BEFORE go — this is the rule
		go func(id string) {
			defer wg.Done()
			results <- id
		}(id)
	}
	wg.Wait()
	close(results)

	count := 0
	for range results {
		count++
	}
	if count != 3 {
		t.Errorf("expected 3 results, got %d", count)
	}
	t.Log("OK: all goroutines completed — Add was called before each launch")
}

// TestMissingDoneHangs shows that a missing Done causes Wait to hang forever.
// We use a timeout to avoid actually hanging the test suite.
func TestMissingDoneHangs(t *testing.T) {
	t.Log("BUG: processTunnelsWithError returns early without calling Done.")
	t.Log("wg.Wait() blocks forever when any goroutine omits Done.")

	done := make(chan struct{})
	go func() {
		processTunnelsWithError([]string{"tun-1", "bad-tunnel", "tun-2"})
		close(done)
	}()

	select {
	case <-done:
		t.Log("UNEXPECTED: buggy version completed — goroutines may have raced past the guard")
	case <-time.After(200 * time.Millisecond):
		t.Log("CONFIRMED BUG: Wait() hung — bad-tunnel goroutine returned without Done()")
	}
}

// TestFixedNeverHangs shows the fixed version always completes.
func TestFixedNeverHangs(t *testing.T) {
	done := make(chan struct{})
	go func() {
		processTunnelsWithErrorFixed([]string{"tun-1", "bad-tunnel", "tun-2"})
		close(done)
	}()

	select {
	case <-done:
		t.Log("OK: fixed version completed — defer wg.Done() fired on all paths")
	case <-time.After(time.Second):
		t.Error("TIMEOUT: fixed version hung — check defer wg.Done()")
	}
}

// TestWaitGroupByPointer demonstrates the by-pointer pattern.
func TestWaitGroupByPointer(t *testing.T) {
	var wg sync.WaitGroup
	ids := []string{"tun-1", "tun-2", "tun-3"}

	for _, id := range ids {
		wg.Add(1)
		go runWorkerFixed(&wg, id) // pointer: Done() decrements the real counter
	}

	done := make(chan struct{})
	go func() {
		wg.Wait()
		close(done)
	}()

	select {
	case <-done:
		t.Log("OK: all workers done — pointer receiver updated the correct WaitGroup")
	case <-time.After(time.Second):
		t.Error("TIMEOUT: WaitGroup hung — may be passing by value somewhere")
	}
}
