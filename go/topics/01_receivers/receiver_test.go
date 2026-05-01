package receivers

import "testing"

// TestValueReceiverDropsLogs shows that Log() on the buggy type silently
// discards every log entry because it appends to a copy.
func TestValueReceiverDropsLogs(t *testing.T) {
	l := TunnelLogger{level: "INFO"}
	l.Log("tunnel opened")
	l.Log("tunnel closed")

	entries := l.Entries()
	if len(entries) != 0 {
		t.Logf("surprisingly got %d entries (Go version may differ)", len(entries))
	} else {
		t.Log("CONFIRMED BUG: 0 entries — value receiver discards all writes")
	}
}

// TestValueReceiverDropsSetLevel shows SetLevel is a no-op.
func TestValueReceiverDropsSetLevel(t *testing.T) {
	l := TunnelLogger{level: "DEBUG"}
	l.SetLevel("ERROR")
	if l.level != "DEBUG" {
		t.Errorf("expected level to stay DEBUG, got %s", l.level)
	}
	t.Log("CONFIRMED BUG: SetLevel had no effect — value receiver discards the write")
}

// TestPointerReceiverWorks shows the fixed version works correctly.
func TestPointerReceiverWorks(t *testing.T) {
	l := &TunnelLoggerFixed{level: "INFO"}
	l.Log("tunnel opened")
	l.Log("tunnel closed")

	entries := l.Entries()
	if len(entries) != 2 {
		t.Errorf("expected 2 entries, got %d", len(entries))
	}

	l.SetLevel("ERROR")
	if l.level != "ERROR" {
		t.Errorf("expected level ERROR, got %s", l.level)
	}
}
