// Package errorhandling — corrected version.
package errorhandling

import (
	"fmt"
	"net"
	"os"
)

// Fix: always handle errors — return them to the caller.
func dialFixed(addr string) (net.Conn, error) {
	conn, err := net.Dial("tcp", addr)
	if err != nil {
		return nil, fmt.Errorf("dial %s: %w", addr, err)
	}
	return conn, nil
}

// Fix: wrap with %w to preserve error chain.
// Caller can still use errors.Is(err, net.ErrClosed) etc.
func openTunnelFixed(id string) (net.Conn, error) {
	conn, err := net.Dial("tcp", "localhost:4040")
	if err != nil {
		return nil, fmt.Errorf("openTunnel %s: %w", id, err)
	}
	return conn, nil
}

// Fix: return immediately on error — never continue with a nil resource.
func logToFileFixed(path, msg string) error {
	f, err := os.Open(path)
	if err != nil {
		return fmt.Errorf("logToFile open %s: %w", path, err)
	}
	defer f.Close()
	if _, err := fmt.Fprintln(f, msg); err != nil {
		return fmt.Errorf("logToFile write %s: %w", path, err)
	}
	return nil
}
