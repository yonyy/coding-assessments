package deferbehavior

import (
	"sync"
	"testing"
)

// TestDoubleUnlockDocumented documents the double-unlock bug.
// NOTE: sync.Mutex.Unlock on an unlocked mutex calls runtime fatal() — this is
// NOT a recoverable panic, so we cannot call processWithNestedLock directly in
// a test. The bug would crash the entire process.
// To see it live, run the function manually in scratch/main.go.
func TestDoubleUnlockDocumented(t *testing.T) {
	t.Log("Bug: processWithNestedLock calls secondary.Unlock() manually AND defers secondary.Unlock()")
	t.Log("The deferred call fires after the manual one, unlocking an already-unlocked mutex")
	t.Log("Result: runtime fatal 'sync: unlock of unlocked mutex' — NOT recoverable")
	t.Log("Fix: remove the manual secondary.Unlock() and let only the deferred one fire")
}

// TestFixedNestedLockNoPanic shows the fixed version doesn't panic.
func TestFixedNestedLockNoPanic(t *testing.T) {
	p := &RequestProcessorFixed{}
	secondary := &sync.Mutex{}
	// Should not panic
	p.processWithNestedLock(secondary)
	t.Log("OK: no panic from nested lock with single deferred unlock")
}

// TestDeferInLoopDeadlocks demonstrates the lock is held across iterations,
// causing a deadlock on the second iteration.
//
// The buggy processRequests():
//  1. Iteration 1: Lock() succeeds, defers Unlock() (but it doesn't fire yet)
//  2. Iteration 2: Lock() BLOCKS — mutex is still held from iteration 1
//  3. Deadlock: the deferred Unlock() can never fire because we're stuck in Lock()
//
// With a single request there's no second iteration, so we can safely observe
// that the lock is never released until the function returns.
func TestDeferInLoopDeadlocks(t *testing.T) {
	t.Log("BUG: with >1 request, processRequests deadlocks on the 2nd iteration.")
	t.Log("Iteration 1 locks, defers unlock (never fires mid-loop), iteration 2 tries to lock again — BLOCKED.")
	t.Log("Fix: extract loop body to helper, or use explicit unlock without defer.")

	// Safe to run with 1 request — no second lock attempt.
	p := &RequestProcessor{requests: []string{"req-1"}}
	p.processRequests()
	t.Log("OK with 1 request: no deadlock, deferred unlock fires at function return")
}
