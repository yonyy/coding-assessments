package ngrok

import java.time.Instant

//    ngrok's free tier enforces per-tunnel request rate limits to prevent abuse.
//    You're building the in-memory rate limiter used at the edge to decide whether an incoming request
//    should be allowed through or rejected.
//
//    Implement a sliding window counter rate limiter. Each "client" is identified by a string key
//    (could be a tunnel ID, an API token, or an IP). The limiter tracks how many requests a client
//    has made in the last N seconds and rejects requests once they exceed the configured threshold.
//
//    Implement this interface:
//
//    tryAcquire(clientId: String): Boolean
//      Returns true if the request is allowed (within limit), false if it should be rejected. Must record the attempt if allowed. Calling this is the "tick."
//    remainingCapacity(clientId: String): Int
//      Returns how many more requests this client may make before hitting the limit in the current window. Should not advance any state.
//    resetClient(clientId: String)
//      Clears all recorded requests for a client (admin/support use case — e.g. a paying customer just upgraded). No-op if client is unknown.
//    evictStaleClients(): Int
//      Removes entries for clients that have had zero activity within the current window. Returns count removed. Called periodically by a background job.
class TunnelRateLimiterV2(
    val windowMs: Long,
    val maxRequests: Int
) {
    // tunnelId => list of accepted requests where val is the timestamp accepted
    val tunnels = mutableMapOf<String, ArrayDeque<Long>>()

    fun tryAcquire(clientId: String): Boolean { TODO("Not yet implemented") }

    fun remainingCapacity(clientId: String): Int { TODO("Not yet implemented") }

    fun resetClient(clientId: String) { TODO("Not yet implemented") }

    fun evictStaleClients(): Int { TODO("Not yet implemented") }
}

fun main() {
    // val windowMs = 3L * 1000L
    // val tunnelRateLimiterV2 = TunnelRateLimiterV2(windowMs = windowMs, maxRequests = 1)

    // val client1 = "client1"
    // val client2 = "client2"

    // println("tryAcquire: $client1")
    // println(tunnelRateLimiterV2.tryAcquire(client1))    // Expected: true
    // println("tryAcquire: $client2")
    // println(tunnelRateLimiterV2.tryAcquire(client2))    // Expected: true

    // println("remainingCapacity: $client1")
    // println(tunnelRateLimiterV2.remainingCapacity(client1))  // Expected: 0
    // println("remainingCapacity: $client2")
    // println(tunnelRateLimiterV2.remainingCapacity(client2))  // Expected: 0

    // println("tryAcquire: $client1")
    // println(tunnelRateLimiterV2.tryAcquire(client1))    // Expected: false
    // println("tryAcquire: $client2")
    // println(tunnelRateLimiterV2.tryAcquire(client2))    // Expected: false

    // println("resetClient: $client1")
    // tunnelRateLimiterV2.resetClient(client1)
    // println("resetClient: $client2")
    // tunnelRateLimiterV2.resetClient(client2)

    // println("evictStaleClients")
    // println(tunnelRateLimiterV2.evictStaleClients())    // Expected: 2
}
