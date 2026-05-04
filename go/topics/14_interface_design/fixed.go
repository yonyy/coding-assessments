// Package interfacedesign — correct interface design patterns.
package interfacedesign

import "fmt"

// Fix 1: define a small Logger interface at the consumer (TunnelProcessorFixed).
// Any type with Log(string) satisfies it — no registration needed.
// This is how Go achieves the equivalent of Kotlin's "program to interfaces."
type Logger interface {
	Log(msg string)
}

type TunnelProcessorFixed struct {
	logger Logger // interface: any Logger implementation works
}

func NewTunnelProcessorFixed(l Logger) *TunnelProcessorFixed {
	return &TunnelProcessorFixed{logger: l}
}

func (p *TunnelProcessorFixed) Process(id string) {
	p.logger.Log("processing " + id)
}

// Fix 2: split the giant interface into small, focused interfaces.
// Each is independently implementable and mockable.
type Connector interface {
	Connect(addr string) error
	Disconnect() error
}

type DataPipe interface {
	Send(data []byte) error
	Receive() ([]byte, error)
}

// Compose if you need the full capability in one spot.
// But functions that only dial don't need to import DataPipe.
type FullTunnelService interface {
	Connector
	DataPipe
}

// Fix 3: return the concrete type — callers access fields directly.
func newTunnelFixed() *TunnelFixed {
	return &TunnelFixed{ID: "tun-new"}
}

type TunnelFixed struct{ ID string }

// MockLogger for tests — satisfies Logger without any extra wiring.
// This is the payoff of small interfaces: mocks are trivial to write.
type MockLogger struct{ Messages []string }

func (m *MockLogger) Log(msg string) {
	m.Messages = append(m.Messages, msg)
	fmt.Println("[mock]", msg)
}
