package sliceinternals

import "testing"

// TestCallerMutatesInternalState shows the buggy Logs() lets a caller
// corrupt the logger's internal data.
func TestCallerMutatesInternalState(t *testing.T) {
	l := &TunnelLogger{}
	l.Log("tunnel opened")
	l.Log("request received")

	logs := l.Logs() // caller gets direct reference to backing array
	logs[0] = "CORRUPTED"

	internal := l.Logs()
	if internal[0] == "CORRUPTED" {
		t.Log("CONFIRMED BUG: caller mutated internal state through returned slice")
	} else {
		t.Log("not reproduced (backing array may have been reallocated)")
	}
}

// TestFixedIsolatesState shows the fixed version is immune to caller mutation.
func TestFixedIsolatesState(t *testing.T) {
	l := &TunnelLoggerFixed{}
	l.Log("tunnel opened")
	l.Log("request received")

	logs := l.Logs()
	logs[0] = "CORRUPTED"

	internal := l.Logs()
	if internal[0] == "CORRUPTED" {
		t.Error("BUG: fixed version should not allow mutation of internal state")
	} else {
		t.Log("OK: internal state isolated from caller")
	}
}
