// Package goroutineleak demonstrates goroutines that run forever with no exit path.
//
// CONCEPT: Goroutines are cheap (2KB stack) but not free. A goroutine that has
// no way to exit is a leak — it holds memory and any resources it captured
// forever, even after the struct that spawned it is garbage collected.
//
// COACHING PHRASE: "This goroutine has no exit condition — once started, it
// runs until the process dies. Pass a context so the caller controls its lifetime."
//
// SPOT THE BUGS:
//  1. StartHealthCheck launches a goroutine that loops forever — no context,
//     no done channel, no way to stop it.
//  2. CollectMetric sends on an unbuffered channel. If the caller returns early
//     (timeout, error), the goroutine blocks on the send forever — leaked.
package goroutineleak

import (
	"fmt"
	"time"
)

type TunnelMonitor struct {
	tunnelID string
}

// Bug: goroutine runs forever. Even after TunnelMonitor goes out of scope,
// the goroutine (and the memory it holds) never goes away.
func (m *TunnelMonitor) StartHealthCheck() {
	go func() {
		for {
			fmt.Printf("checking %s\n", m.tunnelID)
			time.Sleep(time.Second)
		}
	}()
}

// Bug: unbuffered channel + no exit path on the sender.
// If CollectMetric times out or errors before receiving, the goroutine
// blocks on ch <- result forever.
func (m *TunnelMonitor) CollectMetric() int {
	ch := make(chan int) // unbuffered: sender blocks until someone receives
	go func() {
		result := doWork()
		ch <- result // leaks if caller has already returned
	}()
	return <-ch
}

func doWork() int { return 42 }
