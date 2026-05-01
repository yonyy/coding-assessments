// Package sliceinternals demonstrates returning a slice that shares its
// backing array with internal state.
//
// COACHING PHRASE: "Returning an internal slice hands the caller a window into
// your struct's backing array. They can mutate your private state, or your
// next append can overwrite their slice."
//
// SPOT THE BUGS:
//  1. Logs() returns l.entries directly — caller can mutate entries in place.
//  2. After the caller receives the slice, an internal append may or may not
//     share the same backing array (capacity-dependent), causing subtle aliasing.
package sliceinternals

import "sync"

type TunnelLogger struct {
	mu      sync.Mutex
	entries []string
}

func (l *TunnelLogger) Log(msg string) {
	l.mu.Lock()
	defer l.mu.Unlock()
	l.entries = append(l.entries, msg)
}

// Bug: returns the internal slice directly — caller holds a reference to the
// same backing array and can corrupt internal state.
func (l *TunnelLogger) Logs() []string {
	l.mu.Lock()
	defer l.mu.Unlock()
	return l.entries
}
