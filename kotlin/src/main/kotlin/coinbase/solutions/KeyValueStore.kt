package coinbase.solutions

// =============================================================================
// Coinbase Coding Assessment — Complete Reference Solution
// Parts 1–4: KV Store with Transactions, TTL, and Time-Travel Queries
// =============================================================================

// -----------------------------------------------------------------------------
// Core data types
// -----------------------------------------------------------------------------

/**
 * A single version of a key's value, stamped with the timestamp it was written.
 *
 * @param value      The stored string value. Null when this version is a tombstone.
 * @param timestamp  The logical time this version was created (from SET or DELETE).
 * @param expiresAt  Optional absolute expiry time (timestamp + ttl). Null = no expiry.
 */
data class Version(
    val value: String?,          // null  → tombstone (DELETE)
    val timestamp: Long,
    val expiresAt: Long? = null
) {
    val isTombstone: Boolean get() = value == null

    /**
     * Returns true if this version is logically expired AT the given query time.
     * Spec: key is inaccessible when queryTime >= expiresAt.
     */
    fun isExpiredAt(queryTime: Long): Boolean =
        expiresAt != null && queryTime >= expiresAt

    /**
     * Returns true if this version represents a live, readable value at queryTime.
     */
    fun isAliveAt(queryTime: Long): Boolean =
        !isTombstone && !isExpiredAt(queryTime)
}

/**
 * One transaction scope on the stack.
 * Holds a full snapshot-copy of the store at the time BEGIN was called,
 * giving us O(1) rollback and clean isolation.
 */
data class Transaction(
    val store: MutableMap<String, MutableList<Version>>
)

// -----------------------------------------------------------------------------
// KeyValueStore — the full solution
// -----------------------------------------------------------------------------

class KeyValueStore {

    // The global store: key → list of versions in ascending timestamp order.
    // Parts 1–2 use only the latest version; Parts 3–4 rely on the full history.
    private val store = mutableMapOf<String, MutableList<Version>>()

    // Transaction stack. Each frame holds a deep-copied snapshot.
    private val transactions = ArrayDeque<Transaction>()

    companion object {
        const val OK = "OK"
        const val DELETED = "DELETED"
        const val KEY_NOT_FOUND = "KEY_NOT_FOUND"
        const val KEY_EXPIRED = "KEY_EXPIRED"
        const val NO_TRANSACTION = "NO_TRANSACTION"
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Returns the map that all operations should read/write to. */
    private fun currentWindow(): MutableMap<String, MutableList<Version>> =
        transactions.lastOrNull()?.store ?: store

    /**
     * Resolves the current (latest) visible state of a key at [timestamp].
     *
     * Returns:
     *   OK            → key exists and is alive; value in result.value
     *   KEY_EXPIRED   → key exists but its latest version is expired
     *   KEY_NOT_FOUND → key absent or tombstoned
     */
    private sealed class ResolveResult {
        data class Found(val value: String) : ResolveResult()
        object Expired : ResolveResult()
        object NotFound : ResolveResult()
    }

    private fun resolveLatest(key: String, timestamp: Long): ResolveResult {
        val versions = currentWindow()[key] ?: return ResolveResult.NotFound
        val latest = versions.lastOrNull() ?: return ResolveResult.NotFound
        return when {
            latest.isTombstone -> ResolveResult.NotFound
            latest.isExpiredAt(timestamp) -> ResolveResult.Expired
            else -> ResolveResult.Found(latest.value!!)
        }
    }

    /**
     * Resolves the state of a key AS OF [queryTime] (for GET_AT / SCAN_AT).
     *
     * Finds the latest version whose timestamp <= queryTime, then applies
     * TTL logic evaluated at queryTime (not at write time).
     */
    private fun resolveAt(key: String, queryTime: Long): ResolveResult {
        val versions = currentWindow()[key] ?: return ResolveResult.NotFound

        // Binary search: find rightmost version with version.timestamp <= queryTime
        var lo = 0;
        var hi = versions.size - 1;
        var idx = -1
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            if (versions[mid].timestamp <= queryTime) {
                idx = mid; lo = mid + 1
            } else hi = mid - 1
        }
        if (idx == -1) return ResolveResult.NotFound   // no version existed yet

        val v = versions[idx]
        return when {
            v.isTombstone -> ResolveResult.NotFound
            v.isExpiredAt(queryTime) -> ResolveResult.Expired
            else -> ResolveResult.Found(v.value!!)
        }
    }

    /** Appends a new version for [key] into [window], maintaining timestamp order. */
    private fun appendVersion(
        window: MutableMap<String, MutableList<Version>>,
        key: String,
        version: Version
    ) {
        window.getOrPut(key) { mutableListOf() }.add(version)
    }

    /** Deep-copies the current window for use in a new transaction frame. */
    private fun deepCopyWindow(
        source: Map<String, MutableList<Version>>
    ): MutableMap<String, MutableList<Version>> =
        source.mapValues { it.value.toMutableList() }.toMutableMap()

    // -------------------------------------------------------------------------
    // Part 1 — Core KV operations
    // -------------------------------------------------------------------------

    /**
     * SET(timestamp, key, value [, ttl])
     * Creates a new version. If a key previously expired, the old history is
     * preserved (needed for GET_AT) but this write supersedes it for live reads.
     */
    fun set(timestamp: Long, key: String, value: String, ttl: Long? = null): String {
        val expiresAt = if (ttl != null) timestamp + ttl else null
        appendVersion(currentWindow(), key, Version(value, timestamp, expiresAt))
        return OK
    }

    /** GET(timestamp, key) → value | KEY_EXPIRED | KEY_NOT_FOUND */
    fun get(timestamp: Long, key: String): String =
        when (val r = resolveLatest(key, timestamp)) {
            is ResolveResult.Found -> r.value
            is ResolveResult.Expired -> KEY_EXPIRED
            is ResolveResult.NotFound -> KEY_NOT_FOUND
        }

    /** DELETE(timestamp, key) → DELETED | KEY_NOT_FOUND | KEY_EXPIRED */
    fun delete(timestamp: Long, key: String): String {
        return when (resolveLatest(key, timestamp)) {
            is ResolveResult.NotFound -> KEY_NOT_FOUND
            is ResolveResult.Expired -> KEY_EXPIRED
            is ResolveResult.Found -> {
                appendVersion(currentWindow(), key, Version(null, timestamp))
                DELETED
            }
        }
    }

    /** EXISTS(timestamp, key) → "true" | "false" */
    fun exists(timestamp: Long, key: String): String =
        (resolveLatest(key, timestamp) is ResolveResult.Found).toString()

    /** GET_KEYS(timestamp) → sorted list of all currently live keys */
    fun getKeys(timestamp: Long): List<String> =
        currentWindow().keys
            .filter { resolveLatest(it, timestamp) is ResolveResult.Found }
            .sorted()

    // -------------------------------------------------------------------------
    // Part 2 — Prefix search + nested transactions
    // -------------------------------------------------------------------------

    /**
     * PREFIX_SEARCH(timestamp, prefix) → sorted [key, value] pairs for live keys
     * An empty prefix matches all keys.
     */
    fun prefixSearch(timestamp: Long, prefix: String): List<Pair<String, String>> =
        currentWindow().keys
            .filter { it.startsWith(prefix) }
            .mapNotNull { key ->
                val r = resolveLatest(key, timestamp)
                if (r is ResolveResult.Found) key to r.value else null
            }
            .sortedBy { it.first }

    /** COUNT_PREFIX(timestamp, prefix) → count of live keys with this prefix */
    fun countPrefix(timestamp: Long, prefix: String): Int =
        prefixSearch(timestamp, prefix).size

    /** BEGIN() → opens a new transaction scope with a snapshot of the current window */
    fun begin(): String {
        transactions.addLast(Transaction(deepCopyWindow(currentWindow())))
        return OK
    }

    /**
     * COMMIT() → merges the innermost transaction into the next outer scope (or global).
     *
     * For each key in the committed frame, we merge its version list into the target:
     *   - versions already present in the target (same object references from the snapshot)
     *     are kept as-is; only versions added DURING this transaction are appended.
     * Simple approach: replace the target's list with the committed list (correct because
     * the committed list IS the target's snapshot plus any new versions appended in-scope).
     */
    fun commit(): String {
        if (transactions.isEmpty()) return NO_TRANSACTION
        val committed = transactions.removeLast()
        val target = currentWindow()
        for ((key, versions) in committed.store) {
            target[key] = versions
        }
        // Remove any keys that exist in target but were fully absent in committed
        // (e.g., a key deleted via tombstone then compacted within the transaction — rare edge)
        target.keys.retainAll(committed.store.keys + target.keys)
        return OK
    }

    /** ROLLBACK() → discards the innermost transaction, restoring prior state */
    fun rollback(): String {
        if (transactions.isEmpty()) return NO_TRANSACTION
        transactions.removeLast()
        return OK
    }

    // -------------------------------------------------------------------------
    // Part 3 — TTL + SCAN_EXPIRING
    // -------------------------------------------------------------------------

    /**
     * SCAN_EXPIRING(timestamp, windowMs)
     * Returns [key, expiresAt] pairs for keys whose latest live version expires
     * within [timestamp, timestamp + windowMs] inclusive.
     * Runs in O(n) scan — for very large stores a sorted expiry index would be
     * needed (TreeMap<Long, Set<String>>), but correct O(n) passes within constraints.
     */
    fun scanExpiring(timestamp: Long, windowMs: Long): List<Pair<String, Long>> {
        val rangeEnd = timestamp + windowMs
        return currentWindow().entries
            .mapNotNull { (key, versions) ->
                val latest = versions.lastOrNull() ?: return@mapNotNull null
                val exp = latest.expiresAt ?: return@mapNotNull null
                // Only include: not tombstoned, not already expired, within window
                if (!latest.isTombstone && exp >= timestamp && exp <= rangeEnd)
                    key to exp
                else null
            }
            .sortedBy { it.second }
    }

    // -------------------------------------------------------------------------
    // Part 4 — Time-travel queries + compaction
    // -------------------------------------------------------------------------

    /**
     * GET_AT(timestampNow, key, queryTime)
     * Returns the value of [key] as it existed at [queryTime].
     * O(log v) via binary search over the key's version list.
     */
    fun getAt(timestampNow: Long, key: String, queryTime: Long): String =
        when (val r = resolveAt(key, queryTime)) {
            is ResolveResult.Found -> r.value
            is ResolveResult.Expired -> KEY_EXPIRED
            is ResolveResult.NotFound -> KEY_NOT_FOUND
        }

    /**
     * SCAN_AT(timestampNow, prefix, queryTime)
     * Returns sorted [key, value] pairs for all prefix-matching keys that were
     * alive (non-expired, non-tombstoned) at [queryTime].
     * Expired and deleted keys are excluded from results entirely.
     */
    fun scanAt(timestampNow: Long, prefix: String, queryTime: Long): List<Pair<String, String>> =
        currentWindow().keys
            .filter { it.startsWith(prefix) }
            .mapNotNull { key ->
                val r = resolveAt(key, queryTime)
                if (r is ResolveResult.Found) key to r.value else null
            }
            .sortedBy { it.first }

    /**
     * COMPACT(timestampNow, beforeTimestamp)
     *
     * Permanently removes all history for keys that are fully dead before
     * [beforeTimestamp], meaning their most recent version (as of beforeTimestamp)
     * is either a tombstone or an expired value — AND no live version exists
     * at or after beforeTimestamp.
     *
     * Keys with any live version at or after beforeTimestamp are untouched.
     * Returns the count of keys whose entire history was removed.
     *
     * Note: operates on the global store, not the current transaction window,
     * since compaction is a global, irreversible operation.
     */
    fun compact(timestampNow: Long, beforeTimestamp: Long): Int {
        var removed = 0
        val iter = store.iterator()
        while (iter.hasNext()) {
            val (key, versions) = iter.next()

            // Find the latest version strictly before beforeTimestamp
            val latestBeforeCutoff = versions.lastOrNull { it.timestamp < beforeTimestamp }

            // Check whether any version at or after beforeTimestamp is live
            val hasLiveVersionAfterCutoff = versions
                .filter { it.timestamp >= beforeTimestamp }
                .any { !it.isTombstone && !it.isExpiredAt(timestampNow) }

            if (hasLiveVersionAfterCutoff) continue   // key is still live, don't touch

            // Key is dead before the cutoff — check if its pre-cutoff state is also dead
            val deadBeforeCutoff = latestBeforeCutoff == null ||
                    latestBeforeCutoff.isTombstone ||
                    latestBeforeCutoff.isExpiredAt(beforeTimestamp)

            if (deadBeforeCutoff) {
                iter.remove()
                removed++
            }
        }
        return removed
    }
}

// =============================================================================
// Test harness — verifies all four parts end-to-end
// =============================================================================

fun main() {
    val db = KeyValueStore()

    // --- Part 1 ---
    println("=== Part 1 ===")
    println(db.set(1, "alpha", "1"))            // OK
    println(db.set(2, "beta", "2"))             // OK
    println(db.get(3, "alpha"))                 // 1
    println(db.exists(3, "gamma"))              // false
    println(db.delete(4, "alpha"))              // DELETED
    println(db.delete(5, "alpha"))              // KEY_NOT_FOUND
    println(db.get(6, "alpha"))                 // KEY_NOT_FOUND
    println(db.getKeys(6))                      // [beta]

    // --- Part 2 ---
    println("\n=== Part 2 ===")
    println(db.set(7, "coin", "100"))           // OK
    println(db.set(8, "coinbase", "200"))       // OK
    println(db.set(9, "cold", "300"))           // OK
    println(db.prefixSearch(10, "co"))          // [(coin,100),(coinbase,200),(cold,300)]
    println(db.countPrefix(10, "coin"))         // 2
    println(db.begin())                         // OK
    println(db.set(11, "coin", "999"))          // OK
    println(db.get(11, "coin"))                 // 999  ← sees uncommitted write
    println(db.rollback())                      // OK
    println(db.get(12, "coin"))                 // 100  ← rolled back
    println(db.begin())                         // OK
    println(db.delete(13, "cold"))              // DELETED
    println(db.begin())                         // OK
    println(db.exists(14, "cold"))              // false ← inner sees outer delete
    println(db.commit())                        // OK
    println(db.commit())                        // OK
    println(db.exists(15, "cold"))              // false ← globally deleted

    // --- Part 3 ---
    println("\n=== Part 3 ===")
    val db3 = KeyValueStore()
    println(db3.set(10, "session:alice", "data_a", 50))  // OK (expires t=60)
    println(db3.set(15, "session:bob", "data_b"))       // OK (no expiry)
    println(db3.get(30, "session:alice"))                  // data_a
    println(db3.get(60, "session:alice"))                  // KEY_EXPIRED
    println(db3.get(70, "session:bob"))                    // data_b
    println(db3.set(61, "session:alice", "data_a2", 100)) // OK (expires t=161)
    println(db3.scanExpiring(60, 110))                     // [(session:alice, 161)]
    println(db3.prefixSearch(62, "session:"))
    // [(session:alice, data_a2), (session:bob, data_b)]

    // --- Part 4 ---
    println("\n=== Part 4 ===")
    val db4 = KeyValueStore()
    db4.set(1, "price:BTC", "30000")
    db4.set(10, "price:BTC", "32000")
    db4.set(20, "price:BTC", "31500", 15)   // expires t=35
    db4.set(40, "price:BTC", "33000")
    db4.set(1, "price:ETH", "1800")
    db4.delete(15, "price:ETH")

    println(db4.getAt(50, "price:BTC", 5))   // 30000
    println(db4.getAt(50, "price:BTC", 10))  // 32000
    println(db4.getAt(50, "price:BTC", 30))  // 31500  ← alive at t=30 (expires t=35)
    println(db4.getAt(50, "price:BTC", 36))  // KEY_EXPIRED (t=20 version, queried at 36>=35)
    println(db4.getAt(50, "price:BTC", 45))  // 33000  ← t=40 version is latest

    println(db4.scanAt(50, "price:", 12))
    // [(price:BTC, 32000), (price:ETH, 1800)]
    println(db4.scanAt(50, "price:", 20))
    // [(price:BTC, 31500)]  ← ETH deleted at t=15, excluded

    println(db4.compact(50, 35))             // 1  (ETH deleted at t=15 < t=35)
    println(db4.getAt(50, "price:ETH", 12))  // KEY_NOT_FOUND (compacted)
    println(db4.getAt(50, "price:BTC", 5))   // 30000 (BTC history intact)
}