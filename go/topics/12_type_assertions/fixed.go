// Package typeassertions — corrected version.
//
// Rules:
//  1. Always use the comma-ok form when the type is not guaranteed.
//  2. Type switches should always have a default case.
//  3. For error inspection, prefer errors.As over type assertions — it traverses
//     wrapped errors automatically.
package typeassertions

import (
	"errors"
	"fmt"
	"net"
)

// Fix: comma-ok form — safe, never panics, handles unexpected types gracefully.
func getOpErrorFixed(err error) string {
	netErr, ok := err.(*net.OpError)
	if !ok {
		// Graceful fallback: report what we actually got
		return fmt.Sprintf("unexpected error type %T: %v", err, err)
	}
	return fmt.Sprintf("op=%s addr=%v", netErr.Op, netErr.Addr)
}

// Fix: always include a default case in type switches.
func describeErrorFixed(err error) string {
	switch e := err.(type) {
	case *net.OpError:
		return fmt.Sprintf("network: op=%s", e.Op)
	case *net.DNSError:
		return fmt.Sprintf("dns: %s", e.Name)
	default:
		// Default handles everything else — never silently ignores.
		return fmt.Sprintf("error (%T): %v", e, e)
	}
}

// BestPractice: use errors.As instead of type assertions for errors.
// errors.As unwraps the error chain — works even if the error was wrapped with %w.
func getOpErrorBest(err error) string {
	var netErr *net.OpError
	if errors.As(err, &netErr) {
		return fmt.Sprintf("op=%s addr=%v", netErr.Op, netErr.Addr)
	}
	return fmt.Sprintf("not a network error: %v", err)
}
