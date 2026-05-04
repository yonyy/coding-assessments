package coinbase

// Part 1
//    SET(key, value)       → "OK"
//    GET(key)              → value | "KEY_NOT_FOUND"
//    DELETE(key)           → "DELETED" | "KEY_NOT_FOUND"
//    EXISTS(key)           → "true" | "false"
//    GET_KEYS()            → sorted list of all keys, or empty list []
// Part 2
//    PREFIX_SEARCH(prefix)      → sorted list of [key, value] pairs where key starts with prefix
//    COUNT_PREFIX(prefix)       → integer count of keys with this prefix
//    BEGIN()                    → opens a new transaction scope (nestable)
//    COMMIT()                   → commits innermost transaction; "NO_TRANSACTION" if none open
//    ROLLBACK()                 → discards innermost transaction; "NO_TRANSACTION" if none open
// Part 3
//    SET(timestamp, key, value)           → "OK"
//    SET(timestamp, key, value, ttl)      → "OK"  (ttl = duration in ms, expires at timestamp + ttl)
//    GET(timestamp, key)                  → value | "KEY_NOT_FOUND" | "KEY_EXPIRED"
//    DELETE(timestamp, key)               → "DELETED" | "KEY_NOT_FOUND" | "KEY_EXPIRED"
//    EXISTS(timestamp, key)               → "true" | "false"   (expired → "false")
//    PREFIX_SEARCH(timestamp, prefix)     → only non-expired keys
//    SCAN_EXPIRING(timestamp, window)     → sorted list of [key, expiry_ts] expiring within [timestamp, timestamp + window] inclusive
//
// Part 4
//    GET_AT(timestamp_now, key, timestamp_query)
//    → Returns the value of key as it existed at timestamp_query.
//    Returns "KEY_NOT_FOUND" if the key didn't exist yet at that time.
//    Returns "KEY_EXPIRED" if the key existed but was expired at that time.
//
//    SCAN_AT(timestamp_now, prefix, timestamp_query)
//    → Returns sorted [key, value] pairs for all keys matching prefix
//    that were alive (non-expired) at timestamp_query.
//
//    COMPACT(timestamp_now, before_timestamp)
//    → Permanently removes all historical versions of keys whose
//    *latest* version was either deleted or expired before before_timestamp.
//    Returns the count of keys fully removed.
//    Versions that are the most recent state of a still-live key must NOT be removed.
data class Transaction(
    val store: MutableMap<String, TrackedValue>
)

data class Value(
    val value: String,
    val version: Long,
    val expiresAt: Long? = null,
    val tombstoned: Boolean = false
) {
    fun isExpired(timestamp: Long): Boolean {
        TODO("Not yet implemented")
    }
}

data class TrackedValue(
    private val value: Value? = null,
) {
    private val trackedValues = ArrayDeque<Value>().apply {
        if (value != null) add(value)
    }

    fun isExpired(timestamp: Long): Boolean { TODO("Not yet implemented") }
    fun isTombstoned(): Boolean { TODO("Not yet implemented") }
    fun latestValue(): Value { TODO("Not yet implemented") }
    fun add(value: Value) { TODO("Not yet implemented") }
}

class KeyValueStore {
    private val transactions = ArrayDeque<Transaction>()
    private val store = mutableMapOf<String, TrackedValue>()

    companion object {
        const val OK = "OK"
        const val DELETED = "DELETED"
        const val KEY_NOT_FOUND = "KEY_NOT_FOUND"
        const val NO_TRANSACTION = "NO_TRANSACTION"
        const val KEY_EXPIRED = "KEY_EXPIRED"
    }

    private fun currentWindow(): MutableMap<String, TrackedValue> { TODO("Not yet implemented") }

    private fun resolve(key: String): Value? { TODO("Not yet implemented") }

    fun set(key: String, value: String, timestamp: Long, ttl: Long? = null): String { TODO("Not yet implemented") }

    fun get(key: String, timestamp: Long): String { TODO("Not yet implemented") }

    fun delete(key: String, timestamp: Long): String { TODO("Not yet implemented") }

    fun exists(key: String, timestamp: Long): String { TODO("Not yet implemented") }

    fun getKeys(timestamp: Long): List<String> { TODO("Not yet implemented") }

    fun prefixSearch(prefix: String, timestamp: Long? = null): List<Pair<String, String>> { TODO("Not yet implemented") }

    fun countPrefix(prefix: String): Int { TODO("Not yet implemented") }

    fun begin() { TODO("Not yet implemented") }

    fun commit(): String { TODO("Not yet implemented") }

    fun rollback(): String? { TODO("Not yet implemented") }

    fun scanExpiring(timestamp: Long, windowMs: Long): List<Pair<String, Long>> { TODO("Not yet implemented") }
}

class MonotonicVersion {
    var timestamp: Long = 0
    private set

    fun inc(): Long { TODO("Not yet implemented") }
}

fun main() {
    // val store = KeyValueStore()
    // val timestamp = MonotonicVersion()

    // store.set("user1.id", "xyz", timestamp.inc())
    // store.set("user2.id", "abc", timestamp.inc())

    // store.begin()
    // store.set("user1.id", "xyz123", timestamp.inc())
    // store.begin()
    // store.set("user2.id", "abc123", timestamp.inc())
    // store.rollback()
    // store.commit()

    // println("store.get(\"user1.id\")")
    // println(store.get("user1.id", timestamp.inc()))  // Expected: xyz123
    // println("store.get(\"user2.id\")")
    // println(store.get("user2.id", timestamp.inc()))  // Expected: abc
}
