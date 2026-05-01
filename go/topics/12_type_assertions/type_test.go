package typeassertions

import (
	"errors"
	"fmt"
	"net"
	"testing"
)

// TestSingleReturnAssertionPanics shows the "interface conversion" panic.
func TestSingleReturnAssertionPanics(t *testing.T) {
	defer func() {
		if r := recover(); r != nil {
			t.Logf("CONFIRMED BUG: type assertion panicked: %v", r)
		}
	}()

	err := errors.New("plain error — not a *net.OpError")
	_ = getOpError(err) // panic: interface conversion
	t.Error("should have panicked before reaching here")
}

// TestCommaOkFormIsSafe shows the comma-ok form handles any error type gracefully.
func TestCommaOkFormIsSafe(t *testing.T) {
	// Works fine with the expected type
	netErr := &net.OpError{Op: "dial", Net: "tcp"}
	result := getOpErrorFixed(netErr)
	if result == "" {
		t.Error("expected non-empty result for *net.OpError")
	}
	t.Logf("OK with expected type: %q", result)

	// Also handles unexpected type gracefully — no panic
	plainErr := errors.New("something went wrong")
	result = getOpErrorFixed(plainErr)
	if result == "" {
		t.Error("expected descriptive fallback for unexpected type")
	}
	t.Logf("OK with unexpected type: %q", result)
}

// TestTypeSwitchDefaultCase shows the default case catches unhandled types.
func TestTypeSwitchDefaultCase(t *testing.T) {
	plainErr := fmt.Errorf("some other error")

	buggy := describeError(plainErr)
	if buggy == "" {
		t.Log("CONFIRMED BUG: type switch silently returned '' for unhandled error type")
	}

	fixed := describeErrorFixed(plainErr)
	if fixed == "" {
		t.Error("fixed version should not return empty string for any error")
	}
	t.Logf("OK: default case produced: %q", fixed)
}

// TestErrorsAsUnwrapsChain shows errors.As is better than type assertions for errors
// because it traverses wrapped errors automatically.
func TestErrorsAsUnwrapsChain(t *testing.T) {
	inner := &net.OpError{Op: "connect", Net: "tcp"}
	wrapped := fmt.Errorf("tunnel failed: %w", inner) // wrapped with %w

	// Direct type assertion fails — wrapped is *fmt.wrapError, not *net.OpError.
	_, directOk := wrapped.(*net.OpError)
	if directOk {
		t.Error("unexpected: direct assertion succeeded on wrapped error")
	}

	// errors.As unwraps the chain and finds the *net.OpError inside.
	result := getOpErrorBest(wrapped)
	if result == "" {
		t.Error("expected errors.As to find *net.OpError through wrapper")
	}
	t.Logf("OK: errors.As found wrapped *net.OpError: %q", result)
}
