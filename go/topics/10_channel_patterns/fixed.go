// Package channelpatterns — correct channel patterns.
//
// FIX: The producer/sender is responsible for closing. When there are multiple
// senders, use a WaitGroup + a single dedicated closer goroutine.
//
// Mental model:
//   producer → closes channel when done sending
//   consumer → ranges over channel until it's closed (never closes)
//   fan-in   → WaitGroup tracks all senders; one goroutine closes after all finish
package channelpatterns

import "sync"

// Fix: producer sends all items, then closes. Caller ranges over the channel.
func producer(items []string) <-chan string {
	out := make(chan string, len(items)) // buffered: won't block
	go func() {
		defer close(out) // producer closes exactly once when done
		for _, item := range items {
			out <- item
		}
	}()
	return out
}

// Fix: WaitGroup ensures close is called exactly once, after ALL senders finish.
func fanInFixed(sources []chan string) <-chan string {
	out := make(chan string)
	var wg sync.WaitGroup

	for _, src := range sources {
		wg.Add(1)
		go func(ch chan string) {
			defer wg.Done()
			for msg := range ch {
				out <- msg
			}
			// Each goroutine signals done — it does NOT close out
		}(src)
	}

	// Single closer: fires once, after all senders have finished.
	go func() {
		wg.Wait()
		close(out)
	}()

	return out
}
