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
        return expiresAt != null && timestamp >= expiresAt
    }
}

data class TrackedValue(
    private val value: Value? = null,
) {
    private val trackedValues = ArrayDeque<Value>().apply {
        if (value != null) add(value)
    }

    fun isExpired(timestamp: Long): Boolean = trackedValues.last().isExpired(timestamp)
    fun isTombstoned(): Boolean = trackedValues.last().tombstoned
    fun latestValue(): Value = trackedValues.last()
    fun add(value: Value) = trackedValues.add(value)
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

    private fun currentWindow(): MutableMap<String, TrackedValue> {
        return transactions.lastOrNull()?.store ?: store
    }

    private fun resolve(key: String): Value? {
        val v = currentWindow()[key]
        return if (v?.isTombstoned() == false) {
            v.latestValue()
        } else {
            null
        }
    }

    fun set(key: String, value: String, timestamp: Long, ttl: Long? = null): String {
        val v = Value(
            value = value,
            version = timestamp,
            expiresAt = if (ttl != null) timestamp + ttl else null
        )

        currentWindow().getOrPut(key) { TrackedValue() }.add(v)
        return OK
    }

    fun get(key: String, timestamp: Long): String {
        return resolve(key)?.let {
            if (it.isExpired(timestamp)) {
                KEY_EXPIRED
            } else {
                it.value
            }
        } ?: KEY_NOT_FOUND
    }

    fun delete(key: String, timestamp: Long): String {
        return if (currentWindow().containsKey(key)) {
            currentWindow()[key]!!.add(currentWindow()[key]!!.latestValue().copy(
                version = timestamp,
                tombstoned = true
            ))

            DELETED
        } else {
            KEY_NOT_FOUND
        }
    }

    fun exists(key: String, timestamp: Long): String {
        return (currentWindow().containsKey(key) &&
                (!currentWindow()[key]!!.latestValue().tombstoned &&
                        !currentWindow()[key]!!.latestValue().isExpired(timestamp))
                ).toString()
    }

    fun getKeys(timestamp: Long): List<String> {
        return currentWindow().keys
            .filter { !currentWindow()[it]!!.latestValue().tombstoned &&
                    !currentWindow()[it]!!.latestValue().isExpired(timestamp)
            }
            .sorted()
    }

    fun prefixSearch(prefix: String, timestamp: Long? = null): List<Pair<String, String>> {
        return currentWindow()
            .entries
            .filter {
                it.key.startsWith(prefix) &&
                        !it.value.isTombstoned() &&
                        (timestamp == null || !it.value.isExpired(timestamp))
            }
            .sortedBy { it.key }
            .map { it.key to it.value.latestValue().value }
    }

    fun countPrefix(prefix: String): Int {
        return prefixSearch(prefix).size
    }

    fun begin() {
        transactions.addLast(
            Transaction(
                store = currentWindow().toMutableMap()
            )
        )
    }

    fun commit(): String {
        if (transactions.isEmpty()) {
            return NO_TRANSACTION
        }

        val committed = transactions.removeLast()
        committed.store.entries.forEach {
            if (it.value.isTombstoned()) {
                currentWindow().remove(it.key)
            } else {
                currentWindow()[it.key] = it.value
            }
        }

        return OK
    }

    fun rollback(): String? {
        if (transactions.isEmpty()) {
            return NO_TRANSACTION
        }

        transactions.removeLast()
        return null
    }

    fun scanExpiring(timestamp: Long, windowMs: Long): List<Pair<String, Long>> {
        return currentWindow()
            .entries
            .filter {
                it.value.latestValue().expiresAt != null &&
                        !it.value.isTombstoned() &&
                        it.value.latestValue().expiresAt!! <= timestamp + windowMs &&
                        it.value.latestValue().expiresAt!! >= timestamp
            }
            .sortedBy { it.value.latestValue().expiresAt }
            .map { it.key to it.value.latestValue().expiresAt!! }
    }
}

class MonotonicVersion {
    var timestamp: Long = 0
    private set

    fun inc(): Long {
        return timestamp++
    }
}

fun main() {
    val store = KeyValueStore()
    val timestamp = MonotonicVersion()

    store.set("user1.id", "xyz", timestamp.inc())
    store.set("user2.id", "abc", timestamp.inc())

    store.begin()
    store.set("user1.id", "xyz123", timestamp.inc())
    store.begin()
    store.set("user2.id", "abc123", timestamp.inc())
    store.rollback()
    store.commit()

    println("store.get(\"user1.id\")")
    println(store.get("user1.id", timestamp.inc()))
    println("store.get(\"user2.id\")")
    println(store.get("user2.id", timestamp.inc()))
}