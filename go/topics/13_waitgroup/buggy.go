// Package waitgroup demonstrates sync.WaitGroup misuse.
//
// CONCEPT: sync.WaitGroup is a counter. Add(n) increments, Done() decrements,
// Wait() blocks until the counter reaches zero. Three strict rules:
//
//  1. Call Add() BEFORE launching the goroutine — never inside it.
//     If Add is inside, Wait() can return before Add is ever called.
//
//  2. Always defer Done() — it must fire on every return path, including errors.
//     A missing Done causes Wait() to block forever.
//
//  3. Pass WaitGroup by POINTER — passing by value copies it; Done() on the
//     copy decrements the copy's counter, leaving the original stuck at Wait().
//
// COACHING PHRASES:
//   - "wg.Add is inside the goroutine — that's a race with wg.Wait(). Move Add
//     to before the go statement."
//   - "This goroutine returns early without calling wg.Done() — the WaitGroup
//     will hang forever. Use defer wg.Done() at the top."
//   - "WaitGroup is being passed by value — Done() will decrement a copy and
//     the caller's Wait() will block forever. Pass a pointer."
//
// SPOT THE BUGS:
//  1. Add called inside goroutine — races with Wait.
//  2. Done not called on the early-return path.
//  3. runWorker accepts WaitGroup by value — copy is decremented, not original.
package waitgroup

import (
	"fmt"
	"sync"
)

// Bug 1: wg.Add(1) inside the goroutine. The goroutine may finish and call Done
// before Add ever runs, driving the counter negative — panic.
// Or Wait() returns before some goroutines have started — misses their work.
func processTunnels(ids []string) {
	var wg sync.WaitGroup
	for _, id := range ids {
		go func(id string) {
			wg.Add(1) // Bug: inside goroutine — races with Wait
			defer wg.Done()
			fmt.Printf("processing %s\n", id)
		}(id)
	}
	wg.Wait()
}

// Bug 2: Done is only called on the happy path. "bad-tunnel" returns early
// without Done — WaitGroup counter never reaches zero; Wait blocks forever.
func processTunnelsWithError(ids []string) {
	var wg sync.WaitGroup
	for _, id := range ids {
		wg.Add(1)
		go func(id string) {
			if id == "bad-tunnel" {
				return // Bug: missing wg.Done() — WaitGroup hangs
			}
			fmt.Printf("processing %s\n", id)
			wg.Done() // only called on happy path
		}(id)
	}
	wg.Wait()
}

// Bug 3: WaitGroup passed by value — runWorker gets a copy.
// Done() on the copy has no effect on the original wg in the caller.
func runWorker(wg sync.WaitGroup, id string) { // Bug: value copy
	defer wg.Done()
	fmt.Printf("worker %s done\n", id)
}
