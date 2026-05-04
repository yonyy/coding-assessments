package channelpatterns

import (
	"testing"
)

// TestSendOnClosedChannelPanics demonstrates "send on closed channel" panic.
// This IS a recoverable panic (unlike the mutex fatal), so we can catch it.
func TestSendOnClosedChannelPanics(t *testing.T) {
	defer func() {
		if r := recover(); r != nil {
			t.Logf("CONFIRMED BUG: send on closed channel panicked: %v", r)
		}
	}()

	ch := make(chan string, 1)
	close(ch)
	ch <- "oops" // panic: send on closed channel
	t.Error("should have panicked before reaching here")
}

// TestReceiveFromClosedChannelIsSafe shows reading from a closed channel
// does NOT panic — it returns the zero value and false immediately.
// This is how range-over-channel knows when to stop.
func TestReceiveFromClosedChannelIsSafe(t *testing.T) {
	ch := make(chan string, 2)
	ch <- "first"
	ch <- "second"
	close(ch)

	// Range drains the buffered items then stops — this is the idiomatic consumer pattern.
	var received []string
	for msg := range ch {
		received = append(received, msg)
	}
	if len(received) != 2 {
		t.Errorf("expected 2 messages, got %d", len(received))
	}

	// After range finishes, receiving from closed returns zero value + false.
	v, ok := <-ch
	if ok || v != "" {
		t.Errorf("expected ('', false) from exhausted closed channel, got (%q, %v)", v, ok)
	}
	t.Log("OK: receive from closed channel returns zero value + false — safe, not a panic")
}

// TestProducerPattern shows the correct producer-closes-channel pattern.
func TestProducerPattern(t *testing.T) {
	items := []string{"tunnel-A", "tunnel-B", "tunnel-C"}
	ch := producer(items)

	var got []string
	for msg := range ch { // range stops automatically when producer closes
		got = append(got, msg)
	}

	if len(got) != len(items) {
		t.Errorf("expected %d items, got %d", len(items), len(got))
	}
	t.Log("OK: producer closed channel after all items sent; consumer ranged cleanly")
}

// TestFanInFixed verifies multiple sources merge into one channel without panic.
func TestFanInFixed(t *testing.T) {
	sources := make([]chan string, 3)
	for i := range sources {
		sources[i] = make(chan string, 1)
		sources[i] <- "msg"
		close(sources[i]) // each source sends one message then closes
	}

	out := fanInFixed(sources)
	count := 0
	for range out {
		count++
	}
	if count != 3 {
		t.Errorf("expected 3 messages, got %d", count)
	}
	t.Log("OK: fan-in merged all sources; single closer prevented double-close panic")
}
