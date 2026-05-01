// Package channelpatterns demonstrates common channel misuse bugs.
//
// CONCEPT: Channels are Go's way to communicate between goroutines ("share memory
// by communicating"). Key rules to know:
//   - Sending to a CLOSED channel → panic (send on closed channel)
//   - Receiving from a closed channel → returns zero value + false (safe, non-blocking)
//   - Unbuffered channel: BOTH sender and receiver must be ready simultaneously
//   - Buffered channel: sender only blocks when the buffer is full
//   - Only the SENDER should close a channel; the receiver never closes
//
// COACHING PHRASE: "Who owns this channel's close? If multiple goroutines can
// send, none of them should close — use a WaitGroup + a dedicated closer."
//
// SPOT THE BUGS:
//  1. closeAndSend closes the channel then tries to send — panics.
//  2. fanIn has multiple goroutines each trying to close the output channel
//     after draining their source — the second close panics.
package channelpatterns

// Bug 1: send after close → "send on closed channel" panic.
func closeAndSend(results chan string) {
	close(results)
	results <- "one more result" // panic: send on closed channel
}

// Bug 2: multiple sender goroutines, each closes the shared output channel.
// First close succeeds; second panics: "close of closed channel".
func fanIn(sources []chan string) chan string {
	out := make(chan string)
	for _, src := range sources {
		go func(ch chan string) {
			for msg := range ch {
				out <- msg
			}
			close(out) // Bug: every goroutine tries to close — second one panics
		}(src)
	}
	return out
}
