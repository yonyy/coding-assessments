// Package receivers demonstrates the value-receiver bug on a struct that
// contains a mutex and a slice.
//
// COACHING PHRASE: "Value receiver on a mutating method silently copies the
// struct — your mutex and your data are on the copy, not the original."
//
// SPOT THE BUGS:
//  1. Log() uses a value receiver — every call copies TunnelLogger including
//     its mutex (sync.Mutex must never be copied after first use).
//  2. SetLevel() also uses a value receiver — the level change is lost.
package receivers

import (
	"fmt"
	"sync"
	"time"
)

type TunnelLogger struct {
	mu      sync.Mutex
	entries []string
	level   string
}

// Bug: value receiver copies the struct. The mutex on the copy is a different
// mutex than the one on the original. go vet will flag "Lock called on copy".
func (l TunnelLogger) Log(msg string) {
	l.mu.Lock()
	defer l.mu.Unlock()
	entry := fmt.Sprintf("[%s] %s %s", l.level, time.Now().Format(time.RFC3339), msg)
	l.entries = append(l.entries, entry)
}

// Bug: value receiver — level is set on the copy and immediately discarded.
func (l TunnelLogger) SetLevel(level string) {
	l.level = level
}

func (l *TunnelLogger) Entries() []string {
	l.mu.Lock()
	defer l.mu.Unlock()
	return l.entries
}
