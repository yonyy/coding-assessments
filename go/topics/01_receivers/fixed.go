// Package receivers — corrected version.
//
// FIX: All mutating methods (and any method on a struct with a mutex) must use
// pointer receivers. Rule of thumb: if ANY method needs a pointer receiver,
// ALL methods on that type should use pointer receivers for consistency.
package receivers

import (
	"fmt"
	"sync"
	"time"
)

type TunnelLoggerFixed struct {
	mu      sync.Mutex
	entries []string
	level   string
}

func (l *TunnelLoggerFixed) Log(msg string) {
	l.mu.Lock()
	defer l.mu.Unlock()
	entry := fmt.Sprintf("[%s] %s %s", l.level, time.Now().Format(time.RFC3339), msg)
	l.entries = append(l.entries, entry)
}

func (l *TunnelLoggerFixed) SetLevel(level string) {
	l.level = level
}

func (l *TunnelLoggerFixed) Entries() []string {
	l.mu.Lock()
	defer l.mu.Unlock()
	out := make([]string, len(l.entries))
	copy(out, l.entries)
	return out
}
