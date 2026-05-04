// Package typeassertions demonstrates unsafe type assertion patterns.
//
// CONCEPT: In Go, interfaces hold values of any type. A type assertion retrieves
// the underlying concrete value. There are two forms:
//
//	v := i.(T)        — panics if i doesn't hold a T ("interface conversion" panic)
//	v, ok := i.(T)    — safe: ok is false if the type doesn't match; no panic
//
// The same principle applies to type switches: always include a default case.
//
// COACHING PHRASES:
//   - "Single-return type assertion — this panics if err is anything other than
//     *net.OpError. In production you can't control what error type you get."
//   - "Use the comma-ok form: v, ok := i.(T). If ok is false, handle it gracefully."
//   - "Type switch without a default silently ignores unexpected types —
//     add a default that at least logs what came in."
//
// SPOT THE BUGS:
//  1. getOpError uses single-return assertion — panics on unexpected error types.
//  2. describeError type switch has no default — silently returns empty string.
package typeassertions

import (
	"fmt"
	"net"
)

// Bug: panics if err is not *net.OpError.
// In real code, err could be *url.Error, *os.PathError, or a plain errors.New().
func getOpError(err error) string {
	netErr := err.(*net.OpError) // panic: "interface conversion: *errors.errorString is not *net.OpError"
	return fmt.Sprintf("op=%s addr=%v", netErr.Op, netErr.Addr)
}

// Bug: no default case — silently ignores any error type not explicitly listed.
// Caller receives "" and has no idea the type was unhandled.
func describeError(err error) string {
	switch e := err.(type) {
	case *net.OpError:
		return fmt.Sprintf("network: op=%s", e.Op)
	case *net.DNSError:
		return fmt.Sprintf("dns: %s", e.Name)
	}
	return "" // Bug: any other error type silently produces empty string
}
