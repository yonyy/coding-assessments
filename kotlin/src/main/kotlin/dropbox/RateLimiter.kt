package dropbox

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

// ### Background
// You are building an in-memory rate limiting system. The system controls how many requests are allowed over time, with increasing sophistication across levels.
// All levels build on the previous. Read all four levels before writing a line.

// ---

// ### Level 1 — Fixed window rate limiter

// Implement a basic fixed window rate limiter. A fixed window divides time into discrete buckets of a fixed duration. All requests within the same bucket share a single counter. When the counter reaches the limit, requests are rejected until the next window opens.

// ```
// Window size: 60 seconds, limit: 3 requests

// Timeline:
// t=0s   allow  (window [0,60):  count=1)
// t=10s  allow  (window [0,60):  count=2)
// t=20s  allow  (window [0,60):  count=3)
// t=30s  deny   (window [0,60):  count=3, limit reached)
// t=60s  allow  (window [60,120): count=1)  ← new window resets
// t=70s  allow  (window [60,120): count=2)
// ```

// Implement a `RateLimiter` class supporting:

// `allow(timestampMs: Long): Boolean` — returns true if the request is allowed, false if it is denied. Increments the counter for the current window if allowed.

// `reset()` — clears all state. Subsequent calls to `allow()` behave as if no requests have been made.

// **Constructor:** `RateLimiter(windowMs: Long, limit: Int)`
// - `windowMs` — the duration of each fixed window in milliseconds
// - `limit` — maximum number of allowed requests per window

// **Requirements:**
// - Window boundaries align to multiples of `windowMs` from epoch 0 (e.g. for a 60,000ms window, buckets are `[0, 60000)`, `[60000, 120000)`, etc.)
// - Requests at the exact boundary timestamp belong to the new window
// - `windowMs` and `limit` must be positive; throw `IllegalArgumentException` if not
// - Requests are not guaranteed to arrive in chronological order within reason — your implementation must handle out-of-order timestamps for the current and previous window, but you may assume timestamps are within 1 window of the most recent request seen

// ---

// ### Level 2 — Sliding window rate limiter

// A fixed window has a boundary problem: a burst of requests just before and just after a window reset can double the effective rate. A sliding window fixes this by counting requests in the last N milliseconds relative to each incoming request.

// ```
// Window size: 60 seconds, limit: 3 requests

// Timeline:
// t=0s   allow  (window (−60s, 0s]: count=1)
// t=10s  allow  (window (−50s,10s]: count=2)
// t=20s  allow  (window (−40s,20s]: count=3)
// t=30s  deny   (window (−30s,30s]: count=3, all still in window)
// t=61s  allow  (window (1s,  61s]: t=0s expired, count=1)
// t=71s  allow  (window (11s, 71s]: t=10s expired, count=2)
// ```

// Add a second implementation:

// `SlidingWindowRateLimiter(windowMs: Long, limit: Int)`

// Same public interface as `RateLimiter` — `allow(timestampMs: Long): Boolean` and `reset()`.

// **Requirements:**
// - The sliding window is the half-open interval `(timestampMs - windowMs, timestampMs]` — inclusive on the right, exclusive on the left
// - A request is allowed if the count of accepted requests with timestamp in that interval is strictly less than `limit`
// - Timestamps must be non-decreasing — throw `IllegalArgumentException` if a timestamp older than the most recent seen is provided
// - `reset()` clears all stored timestamps

// ---

// ### Level 3 — Per-user quotas

// Wrap your rate limiter implementations behind a multi-tenant facade that enforces independent limits per user.

// `RateLimiterRegistry(factory: RateLimiterFactory)`

// `RateLimiterFactory` is an interface:
// ```
// interface RateLimiterFactory {
//     fun create(): RateLimiter
// }
// ```

// The registry supports:

// `allow(userId: String, timestampMs: Long): Boolean` — returns true if the request for this user is allowed. Creates a new limiter for the user on first request using the factory.

// `reset(userId: String)` — resets the limiter for this specific user only.

// `resetAll()` — resets limiters for all users.

// `getStats(userId: String): UserStats?` — returns stats for a user, or null if the user has never made a request.

// `UserStats` must contain:
// - `userId: String`
// - `totalRequested: Int` — total requests attempted (allowed + denied)
// - `totalAllowed: Int` — total requests allowed
// - `totalDenied: Int` — total requests denied

// **Requirements:**
// - Each user gets an independent limiter instance created by the factory
// - Stats persist across `reset(userId)` — resetting a user's limiter does not clear their stats
// - A user that has never made a request returns null from `getStats()`
// - The factory interface must be used — do not hardcode a limiter type inside the registry

// ---

// ### Level 4 — Token bucket with burst capacity

// Add a third rate limiter strategy that supports burst capacity: requests can be served immediately up to a burst size, then replenish at a fixed rate over time.

// `TokenBucketRateLimiter(refillRatePerMs: Double, bucketCapacity: Int)`

// - `bucketCapacity` — maximum tokens the bucket can hold; the bucket starts full
// - `refillRatePerMs` — tokens added per millisecond (can be fractional, e.g. 0.1 = 1 token per 10ms)

// Same public interface: `allow(timestampMs: Long): Boolean` and `reset()`.

// **How it works:**
// ```
// capacity=5, refillRate=1 token/sec (0.001 per ms)

// t=0ms    allow (tokens: 5→4)
// t=1ms    allow (tokens: 4→3)
// t=2ms    allow (tokens: 3→2)
// t=3ms    allow (tokens: 2→1)
// t=4ms    allow (tokens: 1→0)
// t=5ms    deny  (tokens: 0, only 0.005 refilled — not enough for 1)
// t=1005ms allow (tokens: ~1.005→0.005, floor to 0 after consuming 1)
// ```

// **Requirements:**
// - Tokens are replenished lazily — only when `allow()` is called, compute elapsed time since last call and add `elapsedMs * refillRatePerMs` tokens
// - Token count is capped at `bucketCapacity` after refill
// - A request costs exactly 1 token; it is allowed if tokens >= 1 after refill, denied otherwise
// - Token count is a `Double` internally — do not round until checking `>= 1.0`
// - `reset()` refills the bucket to `bucketCapacity` and resets the last-seen timestamp
// - `refillRatePerMs` and `bucketCapacity` must be positive; throw if not
// - Timestamps must be non-decreasing; throw if a timestamp older than the most recent is provided

// ---

// ### Notes

// - You may code in any language
// - Partial credit is awarded — submit after each level
// - Plan for 90 minutes of uninterrupted time
// - All data is in-memory; no persistence required
data class SlidingWindow(
    val fromMs: Long,
    val toMs: Long
)

class SlidingWindowRateLimiter(
    windowMs: Long,
    limit: Int
) : RateLimiter(windowMs, limit) {
    private val count = AtomicInteger(0)
    private val window = AtomicReference<SlidingWindow>()
    private val lastTimestampMs = AtomicReference<Long>()

    override fun allow(timestampMs: Long): Boolean { TODO("Not yet implemented") }

    override fun reset() { TODO("Not yet implemented") }
}

open class RateLimiter(
    val windowMs: Long,
    val limit: Int
) {
    init {
        require(limit > 0) { "Limit must be positive" }
        require(windowMs > 0) { "Window ms must be positive" }
    }

    val counters = mutableMapOf<Long, AtomicInteger>()

    open fun allow(timestampMs: Long): Boolean { TODO("Not yet implemented") }

    open fun reset() { TODO("Not yet implemented") }
}

interface RateLimiterFactory {
    fun create(): RateLimiter
}

data class UserStats(
    val userId: String,
    val totalRequested: Int = 0,
    val totalAllowed: Int = 0,
    val totalDenied: Int = 0
) {
    fun incrementAllowed(): UserStats { TODO("Not yet implemented") }
    fun incrementDenied(): UserStats { TODO("Not yet implemented") }
}

data class UserRateLimiter(
    private val rateLimiter: RateLimiter,
    private val userId: String
) {
    var userStats: UserStats = UserStats(userId)

    fun allow(timestampMs: Long): Boolean { TODO("Not yet implemented") }

    fun reset() { TODO("Not yet implemented") }
}

class RateLimiterRegistry(
    private val factory: RateLimiterFactory
) {
    val rateRegistry = mutableMapOf<String, UserRateLimiter>()

    fun allow(userId: String, timestampMs: Long): Boolean { TODO("Not yet implemented") }

    fun reset(userId: String) { TODO("Not yet implemented") }

    fun resetAll() { TODO("Not yet implemented") }

    fun getStats(userId: String): UserStats? { TODO("Not yet implemented") }
}

class DefaultRateLimiterFactory: RateLimiterFactory {
    override fun create(): RateLimiter { TODO("Not yet implemented") }
}

fun main() {
    // val windowMs = (60 * 1000).toLong()
    // val rateLimiter = RateLimiter(windowMs = windowMs, limit = 1)

    // println("rateLimiter.allow(0s)")
    // println(rateLimiter.allow(0))              // Expected: true
    // println("rateLimiter.allow(1s)")
    // println(rateLimiter.allow(1 * 1000))       // Expected: false
    // println("rateLimiter.allow(2s)")
    // println(rateLimiter.allow(2 * 1000))       // Expected: false
    // println("rateLimiter.reset()")
    // rateLimiter.reset()
    // println("rateLimiter.allow(3s)")
    // println(rateLimiter.allow(3 * 1000))       // Expected: true
    // println("rateLimiter.allow(60s)")
    // println(rateLimiter.allow(windowMs))       // Expected: true

    // val slidingWindow = SlidingWindowRateLimiter(windowMs = windowMs, limit = 1)

    // println("slidingWindow.allow(0s)")
    // println(slidingWindow.allow(0))            // Expected: true
    // println("slidingWindow.allow(1s)")
    // println(slidingWindow.allow(1 * 1000))     // Expected: false
    // println("slidingWindow.reset()")
    // slidingWindow.reset()
    // println("slidingWindow.allow(3s)")
    // println(slidingWindow.allow(3 * 1000))     // Expected: true

    // val defaultRateLimiterFactory = DefaultRateLimiterFactory()
    // val registry = RateLimiterRegistry(defaultRateLimiterFactory)
    // val user = "yonatanp"
    // println("registry.allow(0s, $user)")
    // println(registry.allow(user, 0))           // Expected: true
    // println("registry.getStats($user)")
    // println(registry.getStats(user))
}
