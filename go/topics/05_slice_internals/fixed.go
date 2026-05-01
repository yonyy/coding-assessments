// Package sliceinternals — corrected version using copy().
//
// FIX: Always return a copy of internal slices. Two equivalent idioms:
//
//	return append([]string(nil), l.entries...)  // concise
//
//	out := make([]string, len(l.entries))       // explicit
//	copy(out, l.entries)
//	return out
//
// Both produce a new backing array the caller owns exclusively.
package sliceinternals

import "sync"

type TunnelLoggerFixed struct {
	mu      sync.Mutex
	entries []string
}

func (l *TunnelLoggerFixed) Log(msg string) {
	l.mu.Lock()
	defer l.mu.Unlock()
	l.entries = append(l.entries, msg)
}

// Logs returns a copy — caller cannot affect internal state.
func (l *TunnelLoggerFixed) Logs() []string {
	l.mu.Lock()
	defer l.mu.Unlock()
	return append([]string(nil), l.entries...)
}
