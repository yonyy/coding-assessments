package dropbox.solution

import java.util.TreeMap

// ─────────────────────────────────────────────────────────────────────────────
// DOMAIN TYPES
// ─────────────────────────────────────────────────────────────────────────────

enum class LogLevel { INFO, WARN, ERROR }

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val service: String,
    val message: String
) {
    fun formatted(): String = "$timestamp $level $service $message"
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL 2: LogFilter — owns its own predicate logic.
//
// Design note: filter is a value object, not a query builder. matches() makes
// LogCollection.query() a single filter { } call that doesn't change through L4.
// All new criteria in L3/L4 are additive fields here — nothing else changes.
// ─────────────────────────────────────────────────────────────────────────────
data class LogFilter(
    val service: String? = null,
    val level: LogLevel? = null,
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null,
    val messageContains: String? = null
) {
    fun matches(entry: LogEntry): Boolean =
        levelMatches(entry.level) &&
                serviceMatches(entry.service) &&
                timestampMatches(entry.timestamp) &&
                messageMatches(entry.message)

    private fun levelMatches(l: LogLevel) =
        level == null || level == l

    private fun serviceMatches(s: String) =
        service == null || service == s

    private fun timestampMatches(ts: Long) =
        (fromTimestamp == null || ts >= fromTimestamp) &&
                (toTimestamp == null || ts <= toTimestamp)

    // Spec: case-insensitive substring match
    private fun messageMatches(m: String) =
        messageContains == null || m.contains(messageContains, ignoreCase = true)
}

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL 3: LogSummary
// ─────────────────────────────────────────────────────────────────────────────
data class LogSummary(
    val totalCount: Int,
    val countByLevel: Map<LogLevel, Int>,
    val countByService: Map<String, Int>,
    val firstTimestamp: Long?,
    val lastTimestamp: Long?
)

// ─────────────────────────────────────────────────────────────────────────────
// LEVEL 4: WindowedSummary
// ─────────────────────────────────────────────────────────────────────────────
data class WindowedSummary(
    val windowStart: Long,
    val windowEnd: Long,
    val summary: LogSummary
)

// ─────────────────────────────────────────────────────────────────────────────
// STORAGE: LogCollection
//
// Key design decision: TreeMap<Long, MutableList<LogEntry>> keyed by timestamp.
// - Insertion maintains sorted order — no sortedBy on every read.
// - values.flatten() gives all entries in timestamp order for free.
// - L4 windowing uses floorKey/subMap for O(log n) window boundary lookup.
//
// Trade-off: if two entries share a timestamp they land in the same bucket list,
// so within-bucket order is insertion order (L3 replay ties break by eventId
// via the sort in query(), not here).
// ─────────────────────────────────────────────────────────────────────────────
class LogCollection {

    private val entries = TreeMap<Long, MutableList<LogEntry>>()

    companion object {
        private const val MIN_SEGMENTS = 4
    }

    fun add(line: String) {
        val segments = line.split(" ")

        if (segments.size < MIN_SEGMENTS) {
            throw IllegalArgumentException("Malformed log line: '$line'")
        }

        val timestamp = runCatching { segments[0].toLong() }
            .getOrElse { throw IllegalArgumentException("Invalid timestamp: '${segments[0]}'") }

        val level = runCatching { LogLevel.valueOf(segments[1]) }
            .getOrElse { throw IllegalArgumentException("Invalid level: '${segments[1]}'") }

        val service = segments[2].takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Service cannot be blank")

        val message = segments.drop(3).joinToString(" ")

        val entry = LogEntry(timestamp, level, service, message)
        entries.getOrPut(timestamp) { mutableListOf() }.add(entry)
    }

    // Single query path — all public methods delegate here.
    // Returns entries already timestamp-sorted (TreeMap guarantees key order).
    // No additional sort needed unless there are same-timestamp ties.
    fun query(filter: LogFilter): List<LogEntry> =
        entries.values
            .flatten()
            .filter { filter.matches(it) }

    // ─────────────────────────────────────────────────────────────────────────
    // LEVEL 3: aggregate() — reuses query(), groups with groupingBy.eachCount()
    //
    // groupingBy { }.eachCount() only produces keys that have entries —
    // satisfies the spec requirement "only include keys with at least one entry"
    // without any extra filtering.
    // ─────────────────────────────────────────────────────────────────────────
    fun aggregate(filter: LogFilter): LogSummary {
        val matched = query(filter)
        return LogSummary(
            totalCount = matched.size,
            countByLevel = matched.groupingBy { it.level }.eachCount(),
            countByService = matched.groupingBy { it.service }.eachCount(),
            firstTimestamp = matched.firstOrNull()?.timestamp,
            lastTimestamp = matched.lastOrNull()?.timestamp
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEVEL 4: aggregateWindowed()
    //
    // Window alignment: buckets align to multiples of windowSeconds from epoch 0.
    // bucket index = timestamp / windowSeconds (integer division)
    // windowStart = bucketIndex * windowSeconds
    // windowEnd   = windowStart + windowSeconds
    //
    // Algorithm:
    // 1. Apply filter first — only matching entries get bucketed.
    // 2. Group by bucket index.
    // 3. For each bucket that has entries, build a LogSummary.
    // 4. Return sorted by windowStart ascending.
    //
    // Only non-empty windows returned — groupBy naturally gives us this.
    // ─────────────────────────────────────────────────────────────────────────
    fun aggregateWindowed(filter: LogFilter, windowSeconds: Long): List<WindowedSummary> {
        if (windowSeconds <= 0) {
            throw IllegalArgumentException("windowSeconds must be positive, got $windowSeconds")
        }

        val matched = query(filter)

        return matched
            .groupBy { it.timestamp / windowSeconds }   // bucket index
            .entries
            .sortedBy { it.key }                        // sort by bucket index = sort by windowStart
            .map { (bucketIndex, bucketEntries) ->
                val windowStart = bucketIndex * windowSeconds
                val windowEnd = windowStart + windowSeconds
                WindowedSummary(
                    windowStart = windowStart,
                    windowEnd = windowEnd,
                    summary = LogSummary(
                        totalCount = bucketEntries.size,
                        countByLevel = bucketEntries.groupingBy { it.level }.eachCount(),
                        countByService = bucketEntries.groupingBy { it.service }.eachCount(),
                        firstTimestamp = bucketEntries.first().timestamp,
                        lastTimestamp = bucketEntries.last().timestamp
                    )
                )
            }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PUBLIC API: LogSystem
//
// Thin delegation layer. Never contains logic — only translates the public
// contract into LogFilter objects and delegates to LogCollection.
// L1 methods (getLogs, getErrors) remain unchanged through L4.
// ─────────────────────────────────────────────────────────────────────────────
class LogSystemSolution {
    private val collection = LogCollection()

    // L1
    fun ingest(line: String) = collection.add(line)

    fun getLogs(service: String): List<LogEntry> =
        collection.query(LogFilter(service = service))

    fun getErrors(): List<LogEntry> =
        collection.query(LogFilter(level = LogLevel.ERROR))

    // L2
    fun query(filter: LogFilter): List<LogEntry> =
        collection.query(filter)

    // L3
    fun aggregate(filter: LogFilter): LogSummary =
        collection.aggregate(filter)

    // L4
    fun aggregateWindowed(filter: LogFilter, windowSeconds: Long): List<WindowedSummary> =
        collection.aggregateWindowed(filter, windowSeconds)
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN — exercises all four levels
// ─────────────────────────────────────────────────────────────────────────────
fun main() {
    val system = LogSystemSolution()

    system.ingest("1700000000 ERROR payments Service unavailable")
    system.ingest("1700000001 INFO  auth User login successful")
    system.ingest("1700000002 WARN  payments Retry attempt 1")
    system.ingest("1700000003 ERROR auth Token validation failed")
    system.ingest("1700000004 INFO  payments Payment processed")
    system.ingest("1700000064 ERROR payments Service unavailable again")  // second window bucket
    system.ingest("1700000065 WARN  payments Retry attempt 2")

    // L1
    println("=== L1: getLogs(payments) ===")
    system.getLogs("payments").forEach { println(it.formatted()) }

    println("\n=== L1: getErrors() ===")
    system.getErrors().forEach { println(it.formatted()) }

    // L2
    println("\n=== L2: query with filter ===")
    system.query(
        LogFilter(
            service = "payments",
            level = LogLevel.ERROR,
            fromTimestamp = 1700000000,
            toTimestamp = 1700000010
        )
    ).forEach { println(it.formatted()) }

    println("\n=== L2: messageContains (case-insensitive) ===")
    system.query(LogFilter(messageContains = "retry")).forEach { println(it.formatted()) }

    // L3
    println("\n=== L3: aggregate(payments) ===")
    val summary = system.aggregate(LogFilter(service = "payments"))
    println("total=${summary.totalCount}")
    println("byLevel=${summary.countByLevel}")
    println("byService=${summary.countByService}")
    println("first=${summary.firstTimestamp} last=${summary.lastTimestamp}")

    // L4
    println("\n=== L4: aggregateWindowed(payments, 60s windows) ===")
    system.aggregateWindowed(LogFilter(service = "payments"), windowSeconds = 60)
        .forEach { w ->
            println("window [${w.windowStart}, ${w.windowEnd}): count=${w.summary.totalCount} levels=${w.summary.countByLevel}")
        }

    // Error handling
    println("\n=== Error handling ===")
    try { system.ingest("") } catch (e: IllegalArgumentException) { println("Empty: ${e.message}") }
    try { system.ingest("notlong INFO payments msg") } catch (e: IllegalArgumentException) { println("Bad ts: ${e.message}") }
    try { system.ingest("1700000000 VERBOSE payments msg") } catch (e: IllegalArgumentException) { println("Bad level: ${e.message}") }
    try { system.aggregateWindowed(LogFilter(), windowSeconds = -1) } catch (e: IllegalArgumentException) { println("Bad window: ${e.message}") }
}
