package capture

import (
	"sort"
	"strings"
	"testing"
)

var tunnelIDs = []string{"tun-alpha", "tun-beta", "tun-gamma", "tun-delta"}

// TestBuggyCapture: under Go 1.22 (our go.mod) this passes because loop
// variables are per-iteration. Under 1.21 it would fail — all results would
// show the last id. The go directive in go.mod determines which semantics apply.
func TestBuggyCapture(t *testing.T) {
	results := LaunchTunnelWorkersBuggy(tunnelIDs)
	sort.Strings(results)

	t.Logf("results: %v", results)
	for _, id := range tunnelIDs {
		found := false
		for _, r := range results {
			if strings.Contains(r, id) {
				found = true
				break
			}
		}
		if !found {
			t.Logf("CLOSURE BUG (pre-1.22 behaviour): id %q not seen in results", id)
		}
	}
}

// TestShadowFix verifies each id appears exactly once.
func TestShadowFix(t *testing.T) {
	results := LaunchTunnelWorkersShadow(tunnelIDs)
	if len(results) != len(tunnelIDs) {
		t.Errorf("expected %d results, got %d", len(tunnelIDs), len(results))
	}
	for _, id := range tunnelIDs {
		found := false
		for _, r := range results {
			if strings.Contains(r, id) {
				found = true
				break
			}
		}
		if !found {
			t.Errorf("id %q not found in results", id)
		}
	}
}

// TestArgFix verifies the pass-by-argument fix also works.
func TestArgFix(t *testing.T) {
	results := LaunchTunnelWorkersArg(tunnelIDs)
	if len(results) != len(tunnelIDs) {
		t.Errorf("expected %d results, got %d", len(tunnelIDs), len(results))
	}
}
