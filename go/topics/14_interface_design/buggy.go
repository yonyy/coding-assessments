// Package interfacedesign demonstrates Go interface design anti-patterns.
//
// CONCEPT: Go interfaces are satisfied IMPLICITLY — no "implements" keyword.
// Any type with the right methods automatically satisfies the interface.
// This makes Go interfaces uniquely powerful for decoupling code.
//
// Key principles:
//  1. "Accept interfaces, return concrete types" — accepting an interface makes
//     your function easy to test (inject a mock); returning concrete gives callers
//     full type information without needing an assertion.
//  2. Keep interfaces small — 1-2 methods is ideal (io.Reader, io.Writer).
//     Small interfaces are easy to implement, easy to mock, easy to compose.
//  3. Define interfaces at the CONSUMER (caller) side, not the producer side.
//     The consumer says what it needs; the producer doesn't need to know.
//  4. Don't return any/interface{} when the concrete type is known.
//
// COACHING PHRASES:
//   - "This function accepts a concrete *TunnelLogger — that makes it hard to
//     test. Define a small Logger interface and accept that instead."
//   - "This interface has 8 methods — any mock or alternative implementation
//     must implement all 8. Split it into smaller focused interfaces."
//   - "Returning interface{} here loses all type information. Return *Tunnel
//     directly — callers get full access without a type assertion."
//
// SPOT THE BUGS:
//  1. TunnelProcessor accepts *TunnelLogger — concrete type, not an interface.
//  2. TunnelService has 6 methods — too large, impossible to mock easily.
//  3. newTunnel() returns interface{} — loses type information.
package interfacedesign

import "fmt"

// Bug 1: concrete dependency — can't inject a mock logger in tests.
type TunnelLogger struct{}

func (l *TunnelLogger) Log(msg string) { fmt.Println(msg) }

type TunnelProcessor struct {
	logger *TunnelLogger // Bug: concrete type — caller must use exactly *TunnelLogger
}

func NewTunnelProcessor(l *TunnelLogger) *TunnelProcessor {
	return &TunnelProcessor{logger: l}
}

func (p *TunnelProcessor) Process(id string) {
	p.logger.Log("processing " + id)
}

// Bug 2: giant interface — requires implementing 6 methods just to satisfy it.
// Writing a test double or alternative implementation is painful.
type TunnelService interface {
	Connect(addr string) error
	Disconnect() error
	Send(data []byte) error
	Receive() ([]byte, error)
	GetStats() map[string]int
	Restart() error
}

// Bug 3: returns interface{} — caller must type-assert to use *Tunnel fields.
func newTunnel() interface{} {
	return &Tunnel{ID: "tun-new"}
}

type Tunnel struct{ ID string }
