package ngrok

/**
 * Implement a rate limiter for ngrok tunnels.
 * Each tunnel is identified by a tunnelId (String).
 * The limiter allows at most `maxRequests` requests per `windowSeconds` sliding window.
 */
class TunnelRateLimiter(val maxRequests: Int, val windowSeconds: Long) {
    private val acceptedRequests = mutableMapOf<String, ArrayDeque<Long>>()
    private val windowMs = windowSeconds * 1000L

    /**
     * Record a request for the given tunnelId at the given timestamp (epoch seconds).
     * Returns true if the request is ALLOWED, false if it should be REJECTED.
     */
    fun recordRequest(tunnelId: String, timestampSeconds: Long): Boolean { TODO("Not yet implemented") }

    /**
     * Returns the number of requests remaining in the current window for a tunnel.
     * Returns 0 if the tunnel is at or over the limit.
     */
    fun remainingRequests(tunnelId: String, currentTime: Long): Int { TODO("Not yet implemented") }
}
