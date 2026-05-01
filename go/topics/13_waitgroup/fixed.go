// Package waitgroup — corrected version.
package waitgroup

import (
	"fmt"
	"sync"
)

// Fix 1: Add called BEFORE launching the goroutine — guaranteed visible to Wait.
func processTunnelsFixed(ids []string) {
	var wg sync.WaitGroup
	for _, id := range ids {
		wg.Add(1) // Add before go — no race
		go func(id string) {
			defer wg.Done()
			fmt.Printf("processing %s\n", id)
		}(id)
	}
	wg.Wait()
}

// Fix 2: defer wg.Done() at the top of the goroutine — fires on every return path.
func processTunnelsWithErrorFixed(ids []string) {
	var wg sync.WaitGroup
	for _, id := range ids {
		wg.Add(1)
		go func(id string) {
			defer wg.Done() // fires even on early return
			if id == "bad-tunnel" {
				return // Done still fires via defer — WaitGroup doesn't hang
			}
			fmt.Printf("processing %s\n", id)
		}(id)
	}
	wg.Wait()
}

// Fix 3: WaitGroup passed by pointer — Done() decrements the original counter.
func runWorkerFixed(wg *sync.WaitGroup, id string) {
	defer wg.Done()
	fmt.Printf("worker %s done\n", id)
}
