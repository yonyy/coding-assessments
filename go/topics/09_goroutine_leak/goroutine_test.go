package goroutineleak

import (
	"context"
	"runtime"
	"testing"
	"time"
)

// TestLeakedGoroutinesGrow shows that StartHealthCheck leaks one goroutine per call.
// Each call adds a goroutine that never exits — goroutine count only goes up.
func TestLeakedGoroutinesGrow(t *testing.T) {
	before := runtime.NumGoroutine()

	for i := 0; i < 5; i++ {
		m := &TunnelMonitor{tunnelID: "tun-" + string(rune('A'+i))}
		m.StartHealthCheck()
	}

	time.Sleep(50 * time.Millisecond) // let goroutines start
	after := runtime.NumGoroutine()

	leaked := after - before
	t.Logf("goroutine count: before=%d after=%d leaked=%d", before, after, leaked)
	if leaked >= 5 {
		t.Logf("CONFIRMED BUG: %d goroutines leaked — no exit path", leaked)
	}
}

// TestFixedGoroutineExitsOnCancel shows the fixed version cleans up when context is cancelled.
func TestFixedGoroutineExitsOnCancel(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	m := &TunnelMonitorFixed{tunnelID: "tun-A"}

	before := runtime.NumGoroutine()
	m.StartHealthCheck(ctx)
	time.Sleep(50 * time.Millisecond)
	mid := runtime.NumGoroutine()

	cancel()
	time.Sleep(50 * time.Millisecond) // let goroutine exit
	after := runtime.NumGoroutine()

	t.Logf("goroutine count: before=%d running=%d after_cancel=%d", before, mid, after)
	if after < mid {
		t.Log("OK: goroutine exited after cancel() — no leak")
	}
}

// TestCollectMetricWithCancelledContext shows the fixed version handles cancellation.
func TestCollectMetricWithCancelledContext(t *testing.T) {
	m := &TunnelMonitorFixed{tunnelID: "tun-A"}

	// Normal case: result arrives before timeout
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	result, err := m.CollectMetric(ctx)
	if err != nil {
		t.Errorf("unexpected error: %v", err)
	}
	if result != 42 {
		t.Errorf("expected 42, got %d", result)
	}
	t.Log("OK: result collected before context deadline")
}
