package interfacedesign

import "testing"

// TestConcreteTypeBlocksMocking shows the limitation of accepting concrete types.
func TestConcreteTypeBlocksMocking(t *testing.T) {
	t.Log("BUG: TunnelProcessor accepts *TunnelLogger — a concrete type.")
	t.Log("To test Process(), you must use the real logger — you can't inject a mock.")
	t.Log("You can't capture log output, can't assert what was logged, can't swap it out.")
	t.Log("Fix: accept a Logger interface — any type with Log(string) satisfies it.")

	// You're forced to use the real thing — no control in tests.
	realLogger := &TunnelLogger{}
	p := NewTunnelProcessor(realLogger)
	p.Process("tun-1") // logs to stdout — can't assert on this in a test
}

// TestInterfaceEnablesMocking shows the fixed version accepts any Logger — including a mock.
func TestInterfaceEnablesMocking(t *testing.T) {
	mock := &MockLogger{}
	p := NewTunnelProcessorFixed(mock)

	p.Process("tun-1")
	p.Process("tun-2")

	// Now we can assert exactly what was logged — no real I/O needed.
	if len(mock.Messages) != 2 {
		t.Errorf("expected 2 logged messages, got %d", len(mock.Messages))
	}
	if mock.Messages[0] != "processing tun-1" {
		t.Errorf("expected 'processing tun-1', got %q", mock.Messages[0])
	}
	t.Log("OK: mock logger injected via interface — Process() is fully testable")
}

// TestImplicitInterfaceSatisfaction shows Go's "no implements keyword" rule.
// MockLogger satisfies Logger without ever mentioning Logger.
func TestImplicitInterfaceSatisfaction(t *testing.T) {
	var l Logger = &MockLogger{} // compiles: MockLogger has Log(string) — that's enough
	l.Log("implicit satisfaction")

	// RealLogger also satisfies Logger — same interface, different implementation.
	var l2 Logger = &TunnelLogger{}
	l2.Log("real logger also satisfies Logger")

	t.Log("OK: both types satisfy Logger without any explicit 'implements' declaration")
}

// TestReturnConcreteTypePreservesInfo shows returning concrete vs interface{}.
func TestReturnConcreteTypePreservesInfo(t *testing.T) {
	// With concrete return: direct field access, no assertion needed.
	tunnel := newTunnelFixed()
	tunnel.ID = "overridden" // compiles — type is *TunnelFixed

	// With interface{} return: must type-assert to access any field.
	raw := newTunnel()
	tun, ok := raw.(*Tunnel) // assertion required just to use the value
	if !ok {
		t.Fatal("type assertion failed on newTunnel() return")
	}
	_ = tun.ID // can access ID now, but only after the assertion ceremony

	t.Log("OK: concrete return gives full type access; interface{} forces assertion")
}
