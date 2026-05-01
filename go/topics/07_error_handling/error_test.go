package errorhandling

import (
	"errors"
	"fmt"
	"net"
	"testing"
)

// TestSwallowedError shows the danger of ignoring errors.
func TestSwallowedError(t *testing.T) {
	conn := dial("localhost:1") // port 1 — almost certainly refused
	if conn == nil {
		t.Log("CONFIRMED BUG RISK: dial returned nil conn, no error propagated to caller")
		t.Log("Any use of conn will panic with nil pointer dereference")
	}
}

// TestWrappedErrorPreservesChain shows %w allows errors.As to traverse the chain.
func TestWrappedErrorPreservesChain(t *testing.T) {
	_, err := openTunnelFixed("tun-test")
	if err == nil {
		t.Skip("tunnel unexpectedly connected — not testing error path")
	}

	var netErr *net.OpError
	if errors.As(err, &netErr) {
		t.Logf("OK: errors.As found network error through wrapped chain: %v", netErr.Op)
	}

	msg := fmt.Sprintf("%v", err)
	if msg == "connection refused" {
		t.Error("BUG: raw error has no context about what operation failed")
	} else {
		t.Logf("OK: wrapped error has context: %v", err)
	}
}

// TestNilResourcePanic demonstrates the nil-resource-after-error bug.
func TestNilResourcePanic(t *testing.T) {
	defer func() {
		if r := recover(); r != nil {
			t.Logf("CONFIRMED BUG: panic from nil file handle: %v", r)
		}
	}()
	_ = logToFile("/nonexistent/path/file.log", "hello")
}

// TestFixedLogToFileReturnsError shows the fixed version returns an error
// instead of panicking.
func TestFixedLogToFileReturnsError(t *testing.T) {
	err := logToFileFixed("/nonexistent/path/file.log", "hello")
	if err == nil {
		t.Error("expected error for nonexistent path, got nil")
	} else {
		t.Logf("OK: returned error with context: %v", err)
	}
}
