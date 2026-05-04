package dropbox.solution

// ─────────────────────────────────────────────────────────────────────────────
// CORE INTERFACE
//
// Design decision made before writing any implementation:
// RateLimiter is an interface, not an open class.
//
// Why interface not superclass:
//   - FixedWindow, SlidingWindow, and TokenBucket are alternative strategies,
//     not specializations of each other. Inheritance implies "is-a" — a sliding
//     window is not a kind of fixed window.
//   - RateLimiterFactory.create() returns RateLimiter. If RateLimiter were a
//     class, the factory return type would be coupled to one implementation.
//   - Shared validation (windowMs > 0, limit > 0) belongs in each class's
//     init block — not inherited, since TokenBucket has different params.
// ─────────────────────────────────────────────────────────────────────────────
interface RateLimiter {
    fun allow(timestampMs: Long): Boolean
    fun reset()
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL 1: Fixed window rate limiter
//
// Algorithm:
//   bucket = timestampMs / windowMs  (integer division — aligns to epoch multiples)
//   Each bucket has an independent counter.
//   Request allowed if counter < limit; increment on allow.
//
// Data structure: Map<Long, Int> keyed by bucket index.
//   - O(1) per request.
//   - Buckets accumulate over time. For a production system you'd evict old
//     buckets; for an in-memory assessment this is acceptable.
//
// Out-of-order handling: handled naturally — each timestamp computes its own
// bucket independently. No shared "current window" state to corrupt.
// ─────────────────────────────────────────────────────────────────────────────
class FixedWindowRateLimiter(
    private val windowMs: Long,
    private val limit: Int
) : RateLimiter {

    init {
        require(windowMs > 0) { "windowMs must be positive, got $windowMs" }
        require(limit > 0) { "limit must be positive, got $limit" }
    }

    private val buckets = mutableMapOf<Long, Int>()

    override fun allow(timestampMs: Long): Boolean {
        require(timestampMs >= 0) { "timestampMs must be non-negative" }

        val bucket = timestampMs / windowMs
        val current = buckets.getOrDefault(bucket, 0)

        return if (current < limit) {
            buckets[bucket] = current + 1
            true
        } else {
            false
        }
    }

    override fun reset() {
        buckets.clear()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL 2: Sliding window rate limiter
//
// Core insight: a sliding window cannot be represented by a single counter.
// You must store the timestamp of every accepted request, then evict those
// that fall outside the current window on each new request.
//
// Algorithm per allow(t):
//   1. Validate t >= lastSeen (non-decreasing requirement).
//   2. Evict all accepted timestamps <= (t - windowMs).
//      Window is (t - windowMs, t] — exclusive on left, inclusive on right.
//   3. If accepted.size < limit: add t to accepted, return true.
//      Otherwise: return false (do not add — denied requests are not tracked).
//
// Data structure: ArrayDeque<Long> used as a queue.
//   - addLast() for new timestamps (right end).
//   - removeFirst() to evict expired timestamps (left end).
//   - Since timestamps are non-decreasing, the deque is always sorted —
//     we only ever need to evict from the front.
//   - O(1) amortized per request (each timestamp added and removed at most once).
//
// Why not a counter + window boundary (your original approach):
//   Example: limit=3, windowMs=60s
//   t=0s  allow, t=10s allow, t=20s allow (count=3)
//   t=61s: relativeFrom=1s. All three requests (0s,10s,20s) are now outside
//          (1s, 61s]. Correct answer: allow. A counter of 3 with no individual
//          timestamps cannot determine which requests expired — you'd need to
//          reset the whole count, but that loses partial expiry information.
// ─────────────────────────────────────────────────────────────────────────────
class SlidingWindowRateLimiter(
    private val windowMs: Long,
    private val limit: Int
) : RateLimiter {

    init {
        require(windowMs > 0) { "windowMs must be positive, got $windowMs" }
        require(limit > 0) { "limit must be positive, got $limit" }
    }

    private val accepted = ArrayDeque<Long>()   // timestamps of allowed requests, oldest first
    private var lastTimestampMs: Long? = null

    override fun allow(timestampMs: Long): Boolean {
        val last = lastTimestampMs
        if (last != null && timestampMs < last) {
            // Spec: non-decreasing only. Equal timestamps (simultaneous requests) are valid.
            throw IllegalArgumentException(
                "Timestamps must be non-decreasing: got $timestampMs after $last"
            )
        }
        // Always update lastTimestampMs — even for denied requests.
        // If we only update on allow, a denied request at t=100s followed by
        // a request at t=50s would incorrectly pass the monotonic check.
        lastTimestampMs = timestampMs

        // Evict expired timestamps from the front of the deque.
        // Window: (timestampMs - windowMs, timestampMs] — exclusive on left.
        val windowStart = timestampMs - windowMs
        while (accepted.isNotEmpty() && accepted.first() <= windowStart) {
            accepted.removeFirst()
        }

        return if (accepted.size < limit) {
            accepted.addLast(timestampMs)
            true
        } else {
            false
        }
    }

    override fun reset() {
        accepted.clear()
        lastTimestampMs = null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL 3: Per-user registry
//
// UserStats is immutable — copy-on-write via data class copy().
// Stats intentionally survive reset(userId) per spec.
//
// UserRateLimiter owns both the limiter and the stats for one user.
// Registry stays simple — just routes by userId.
// ─────────────────────────────────────────────────────────────────────────────
data class UserStats(
    val userId: String,
    val totalRequested: Int = 0,
    val totalAllowed: Int = 0,
    val totalDenied: Int = 0
) {
    fun recordAllowed(): UserStats = copy(
        totalRequested = totalRequested + 1,
        totalAllowed = totalAllowed + 1
    )
    fun recordDenied(): UserStats = copy(
        totalRequested = totalRequested + 1,
        totalDenied = totalDenied + 1
    )
}

// Internal — not part of public API. Encapsulates one user's limiter + stats.
private class UserRateLimiter(
    private val limiter: RateLimiter,
    userId: String
) {
    var stats = UserStats(userId)
        private set

    fun allow(timestampMs: Long): Boolean {
        val allowed = limiter.allow(timestampMs)
        stats = if (allowed) stats.recordAllowed() else stats.recordDenied()
        return allowed
    }

    // reset() clears limiter state only — stats are preserved per spec
    fun reset() = limiter.reset()
}

interface RateLimiterFactory {
    fun create(): RateLimiter
}

class RateLimiterRegistry(private val factory: RateLimiterFactory) {

    private val users = mutableMapOf<String, UserRateLimiter>()

    fun allow(userId: String, timestampMs: Long): Boolean =
        users.getOrPut(userId) { UserRateLimiter(factory.create(), userId) }
            .allow(timestampMs)

    fun reset(userId: String) {
        users[userId]?.reset()
    }

    fun resetAll() {
        users.values.forEach { it.reset() }
    }

    // Returns null if user has never made a request — getOrPut not used here
    // because calling getStats() on an unknown user should not create an entry
    fun getStats(userId: String): UserStats? = users[userId]?.stats
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL 4: Token bucket
//
// Token bucket models burst capacity. Tokens accumulate over time up to a cap;
// each request consumes one token.
//
// Algorithm per allow(t):
//   1. Compute elapsed = t - lastTimestampMs (0 if first call).
//   2. Refill: tokens = min(tokens + elapsed * refillRatePerMs, bucketCapacity).
//   3. If tokens >= 1.0: tokens -= 1.0, return true.
//      Otherwise: return false.
//
// Key details:
//   - Refill is LAZY — computed only when allow() is called, not on a timer.
//     This is the standard production pattern (avoids background threads).
//   - tokens is Double internally. Do not round until the >= 1.0 check.
//     Rounding early causes incorrect allow/deny at fractional boundaries.
//   - coerceAtMost(bucketCapacity.toDouble()) caps the refill — tokens never
//     exceed capacity regardless of how much time passes between requests.
//   - Timestamps must be non-decreasing (can't refill backwards in time).
//   - Bucket starts full — first burst of `bucketCapacity` requests all allowed.
// ─────────────────────────────────────────────────────────────────────────────
class TokenBucketRateLimiter(
    private val refillRatePerMs: Double,
    private val bucketCapacity: Int
) : RateLimiter {

    init {
        require(refillRatePerMs > 0) { "refillRatePerMs must be positive, got $refillRatePerMs" }
        require(bucketCapacity > 0) { "bucketCapacity must be positive, got $bucketCapacity" }
    }

    private var tokens: Double = bucketCapacity.toDouble()  // starts full
    private var lastTimestampMs: Long? = null

    override fun allow(timestampMs: Long): Boolean {
        val last = lastTimestampMs
        if (last != null && timestampMs < last) {
            throw IllegalArgumentException(
                "Timestamps must be non-decreasing: got $timestampMs after $last"
            )
        }

        // Lazy refill: add tokens proportional to elapsed time, cap at capacity
        if (last != null) {
            val elapsed = timestampMs - last
            tokens = (tokens + elapsed * refillRatePerMs)
                .coerceAtMost(bucketCapacity.toDouble())
        }
        lastTimestampMs = timestampMs

        return if (tokens >= 1.0) {
            tokens -= 1.0
            true
        } else {
            false
        }
    }

    override fun reset() {
        tokens = bucketCapacity.toDouble()
        lastTimestampMs = null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FACTORY IMPLEMENTATIONS
// ─────────────────────────────────────────────────────────────────────────────
class FixedWindowFactory(
    private val windowMs: Long,
    private val limit: Int
) : RateLimiterFactory {
    override fun create(): RateLimiter = FixedWindowRateLimiter(windowMs, limit)
}

class SlidingWindowFactory(
    private val windowMs: Long,
    private val limit: Int
) : RateLimiterFactory {
    override fun create(): RateLimiter = SlidingWindowRateLimiter(windowMs, limit)
}

class TokenBucketFactory(
    private val refillRatePerMs: Double,
    private val bucketCapacity: Int
) : RateLimiterFactory {
    override fun create(): RateLimiter = TokenBucketRateLimiter(refillRatePerMs, bucketCapacity)
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN — exercises all four levels with annotated expected output
// ─────────────────────────────────────────────────────────────────────────────
fun main() {
    val windowMs = 60_000L   // 60 seconds
    val limit = 3

    // ── L1: Fixed window ──────────────────────────────────────────────────────
    println("=== L1: Fixed window (windowMs=60s, limit=3) ===")
    val fixed = FixedWindowRateLimiter(windowMs, limit)

    println(fixed.allow(0))           // true  — window [0,60s): count=1
    println(fixed.allow(10_000))      // true  — window [0,60s): count=2
    println(fixed.allow(20_000))      // true  — window [0,60s): count=3
    println(fixed.allow(30_000))      // false — window [0,60s): limit reached
    println(fixed.allow(60_000))      // true  — window [60s,120s): count=1 (new window)
    println(fixed.allow(70_000))      // true  — window [60s,120s): count=2

    fixed.reset()
    println(fixed.allow(5_000))       // true  — reset cleared all state

    // Out-of-order within same window
    println(fixed.allow(55_000))      // true  — window [0,60s): count=2
    println(fixed.allow(50_000))      // true  — window [0,60s): count=3 (earlier ts, same bucket)
    println(fixed.allow(45_000))      // false — window [0,60s): limit reached

    // ── L2: Sliding window ───────────────────────────────────────────────────
    println("\n=== L2: Sliding window (windowMs=60s, limit=3) ===")
    val sliding = SlidingWindowRateLimiter(windowMs, limit)

    println(sliding.allow(0))         // true  — window (-60s,0s]:  accepted=[0]
    println(sliding.allow(10_000))    // true  — window (-50s,10s]: accepted=[0,10s]
    println(sliding.allow(20_000))    // true  — window (-40s,20s]: accepted=[0,10s,20s]
    println(sliding.allow(30_000))    // false — window (-30s,30s]: all 3 still in window
    println(sliding.allow(61_000))    // true  — window (1s,61s]:   0s,10s,20s all expired → accepted=[61s]
    println(sliding.allow(71_000))    // true  — window (11s,71s]:  accepted=[61s,71s]
    println(sliding.allow(81_000))    // true  — window (21s,81s]:  accepted=[61s,71s,81s]
    println(sliding.allow(82_000))    // false — window (22s,82s]:  all 3 still in window

    sliding.reset()
    println(sliding.allow(200_000))   // true  — reset cleared state

    // Equal timestamps — valid per spec (simultaneous requests)
    println(sliding.allow(200_000))   // true  — same timestamp, limit=3 not yet hit

    // Non-decreasing violation
    try {
        sliding.allow(199_000)
    } catch (e: IllegalArgumentException) {
        println("Correctly threw: ${e.message}")
    }

    // ── L3: Per-user registry ────────────────────────────────────────────────
    println("\n=== L3: Registry with sliding window factory (windowMs=60s, limit=2) ===")
    val registry = RateLimiterRegistry(SlidingWindowFactory(windowMs, limit = 2))

    val userA = "yonatanp"
    val userB = "dropbox"

    println(registry.allow(userA, 0))          // true
    println(registry.allow(userA, 10_000))     // true
    println(registry.allow(userA, 20_000))     // false — userA at limit
    println(registry.allow(userB, 0))          // true  — userB independent limiter
    println(registry.allow(userB, 10_000))     // true
    println(registry.allow(userB, 20_000))     // false

    println(registry.getStats(userA))
    // UserStats(userId=yonatanp, totalRequested=3, totalAllowed=2, totalDenied=1)

    println(registry.getStats(userB))
    // UserStats(userId=dropbox, totalRequested=3, totalAllowed=2, totalDenied=1)

    registry.reset(userA)                      // resets userA limiter, stats preserved
    println(registry.allow(userA, 25_000))     // true  — limiter reset
    println(registry.getStats(userA))
    // UserStats(userId=yonatanp, totalRequested=4, totalAllowed=3, totalDenied=1)

    println(registry.getStats("unknown"))      // null — never made a request

    registry.resetAll()
    println(registry.allow(userA, 30_000))     // true  — all limiters reset
    println(registry.allow(userB, 30_000))     // true

    // ── L4: Token bucket ─────────────────────────────────────────────────────
    println("\n=== L4: Token bucket (capacity=5, refill=0.001 tokens/ms = 1/sec) ===")
    val bucket = TokenBucketRateLimiter(
        refillRatePerMs = 0.001,   // 1 token per second
        bucketCapacity = 5
    )

    // Burst: starts full, 5 immediate allows
    println(bucket.allow(0))        // true  — tokens: 5→4
    println(bucket.allow(1))        // true  — tokens: 4→3  (1ms elapsed: +0.001 tokens, negligible)
    println(bucket.allow(2))        // true  — tokens: 3→2
    println(bucket.allow(3))        // true  — tokens: 2→1
    println(bucket.allow(4))        // true  — tokens: 1→0
    println(bucket.allow(5))        // false — tokens: ~0.005, not enough
    println(bucket.allow(1_005))    // true  — 1001ms elapsed: 0.005+1.001=~1.006 tokens → allow → ~0.006
    println(bucket.allow(2_005))    // true  — 1000ms elapsed: 0.006+1.0=1.006 → allow → 0.006
    println(bucket.allow(2_006))    // false — 1ms elapsed: 0.006+0.001=0.007, not enough

    // Cap test: long idle period, tokens capped at capacity
    val capped = TokenBucketRateLimiter(refillRatePerMs = 0.001, bucketCapacity = 3)
    capped.allow(0)                 // tokens: 3→2
    println(capped.allow(100_000))  // true — 100s elapsed: 2+100=102, capped at 3 → 3→2. allow.
    println(capped.allow(100_001))  // true — tokens: 2→1
    println(capped.allow(100_002))  // true — tokens: 1→0
    println(capped.allow(100_003))  // false — tokens: ~0.001, not enough

    bucket.reset()
    println(bucket.allow(0))        // true  — reset refilled to capacity=5

    // Non-decreasing violation
    try {
        bucket.allow(-1)
    } catch (e: IllegalArgumentException) {
        println("Correctly threw: ${e.message}")
    }

    // Token bucket via registry
    println("\n=== L4: Token bucket via registry ===")
    val tbRegistry = RateLimiterRegistry(
        TokenBucketFactory(refillRatePerMs = 0.001, bucketCapacity = 2)
    )
    println(tbRegistry.allow("userC", 0))       // true
    println(tbRegistry.allow("userC", 1))       // true
    println(tbRegistry.allow("userC", 2))       // false — bucket empty
    println(tbRegistry.allow("userD", 0))       // true  — userD independent bucket
    println(tbRegistry.getStats("userC"))
    // UserStats(userId=userC, totalRequested=3, totalAllowed=2, totalDenied=1)
}
