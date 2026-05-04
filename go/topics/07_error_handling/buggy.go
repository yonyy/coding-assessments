// Package errorhandling demonstrates common Go error handling mistakes.
//
// COACHING PHRASES:
//   - "Swallowed error — if this fails silently, the caller has no idea and
//     will see confusing downstream behaviour."
//   - "Use fmt.Errorf with %w to wrap errors — preserves the chain for
//     errors.Is() and errors.As() callers."
//   - "Checking err but continuing to use the nil resource will panic."
//
// SPOT THE BUGS:
//  1. dial() ignores the error from net.Dial entirely.
//  2. openTunnel() returns the raw error without wrapping — caller can't tell
//     what operation failed.
//  3. logToFile() checks err but then falls through to use f when f is nil.
package errorhandling

import (
	"fmt"
	"net"
	"os"
)

// Bug: error from Dial is silently discarded. conn will be nil on failure
// and any subsequent use will panic.
func dial(addr string) net.Conn {
	conn, _ := net.Dial("tcp", addr) // _ discards error
	return conn
}

// Bug: returns raw error — caller cannot distinguish "tunnel not found" from
// "network failure" without brittle string matching.
func openTunnel(id string) (net.Conn, error) {
	conn, err := net.Dial("tcp", "localhost:4040")
	if err != nil {
		return nil, err // no context about what we were doing
	}
	_ = id
	return conn, nil
}

// Bug: checks err, but falls through to use f which is nil when err != nil.
func logToFile(path, msg string) error {
	f, err := os.Open(path)
	if err != nil {
		fmt.Println("warning:", err) // logs but doesn't return
	}
	// f is nil here if Open failed — next line panics
	defer f.Close()
	_, err = fmt.Fprintln(f, msg)
	return err
}
