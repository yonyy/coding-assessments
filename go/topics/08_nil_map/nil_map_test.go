package nilmap

import "testing"

// TestNilMapReadIsSafe shows that reading a nil map doesn't panic.
func TestNilMapReadIsSafe(t *testing.T) {
	r := TunnelRegistry{} // zero value — tunnels is nil
	addr, ok := r.Lookup("tun-1")
	if ok || addr != "" {
		t.Errorf("expected empty result from nil map, got %q %v", addr, ok)
	}
	t.Log("OK: reading nil map is safe, returns zero value")
}

// TestNilMapWritePanics demonstrates the runtime panic on nil map write.
func TestNilMapWritePanics(t *testing.T) {
	r := TunnelRegistry{} // zero value — tunnels is nil

	defer func() {
		if r := recover(); r != nil {
			t.Logf("CONFIRMED BUG: nil map write panicked: %v", r)
		}
	}()

	r.Register("tun-1", "backend:8080") // panics here
	t.Error("should have panicked — nil map write")
}

// TestConstructorPreventsNilMap shows the fixed version is safe.
func TestConstructorPreventsNilMap(t *testing.T) {
	r := NewTunnelRegistry()
	r.Register("tun-1", "backend:8080")

	addr, ok := r.Lookup("tun-1")
	if !ok || addr != "backend:8080" {
		t.Errorf("expected tun-1 -> backend:8080, got %q %v", addr, ok)
	}
	t.Log("OK: constructor-initialized registry works correctly")
}

// TestLazyInitZeroValueSafe shows lazy init handles zero-value usage.
func TestLazyInitZeroValueSafe(t *testing.T) {
	var r TunnelRegistryLazy // zero value
	r.Register("tun-1", "backend:8080")
	addr, ok := r.Lookup("tun-1")
	if !ok || addr != "backend:8080" {
		t.Errorf("expected tun-1 -> backend:8080, got %q %v", addr, ok)
	}
	t.Log("OK: lazy-init pattern supports zero-value usage")
}
