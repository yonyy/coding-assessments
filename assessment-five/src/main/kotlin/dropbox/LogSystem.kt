package dropbox

import dropbox.LogLevel
import java.time.Instant
import java.util.TreeMap

//  Background - You are building an in-memory log analysis system.
//  Logs are ingested as raw strings and your system must parse, query, and aggregate them efficiently.
//  All levels build on the previous. Design your Level 1 with extensibility in mind.

//  Level 1 — Parse and query structured logs
//  Your system receives log lines in the following format: <timestamp> <level> <service> <message>
//      timestamp — Unix epoch seconds (integer)
//      level — one of INFO, WARN, ERROR
//      service — a single alphanumeric word (no spaces)
//      message — the remainder of the line (may contain spaces)
//  Example input:
//      1700000000 ERROR payments Service unavailable
//      1700000001 INFO  auth User login successful
//      1700000002 WARN  payments Retry attempt 1
//      1700000003 ERROR auth Token validation failed
//      1700000004 INFO  payments Payment processed
//
//  Implement a LogSystem class supporting:
//      ingest(line: String) — parse and store a single log line. Throws if the line is malformed.
//      getLogs(service: String): List<LogEntry> — return all logs for a given service, sorted by timestamp ascending.
//      getErrors(): List<LogEntry> — return all ERROR-level logs across all services, sorted by timestamp ascending.
//
//  Requirements:
//      Define a LogEntry data class with fields: timestamp, level, service, message
//      Throw a descriptive exception for malformed lines (missing fields, invalid level, non-integer timestamp)
//      Both query methods return an empty list if no matching logs exist

//  Level 2 — Filtering
//  Extend your system with a flexible query method:
//      query(filter: LogFilter): List<LogEntry>
//  LogFilter must support the following optional filter criteria — results must match ALL criteria that are set:
//
//      service: String? — if set, only logs from this service
//      level: Level? — if set, only logs at this level
//      fromTimestamp: Long? — if set, only logs with timestamp >= fromTimestamp
//      toTimestamp: Long? — if set, only logs with timestamp <= toTimestamp
//      messageContains: String? — if set, only logs whose message contains this substring (case-insensitive)
//
//  Results must be returned sorted by timestamp ascending.
//  Requirements:
//
//      A LogFilter with no criteria set returns all logs
//      fromTimestamp and toTimestamp are both inclusive
//      Your existing getLogs() and getErrors() methods must continue to work unchanged

//  Level 3 — Aggregation
//  Add a method that produces summary statistics over a filtered set of logs:
//  aggregate(filter: LogFilter): LogSummary
//  LogSummary must contain:
//
//  totalCount: Int — total number of matching log entries
//  countByLevel: Map<Level, Int> — count of entries per log level
//  countByService: Map<String, Int> — count of entries per service
//  firstTimestamp: Long? — earliest timestamp in the result set, null if empty
//  lastTimestamp: Long? — latest timestamp in the result set, null if empty
//
//  Requirements:
//
//  aggregate() reuses LogFilter — the same filter criteria from Level 2 apply
//  An empty result set returns a LogSummary with zero counts and null timestamps
//  countByLevel and countByService only include keys that have at least one matching entry
//
//
//  Level 4 — Windowed aggregation
//  Add time-window bucketing to break aggregations into fixed-size time buckets:
//  aggregateWindowed(filter: LogFilter, windowSeconds: Long): List<WindowedSummary>
//  Each WindowedSummary contains:
//
//  windowStart: Long — start of the bucket (inclusive)
//  windowEnd: Long — end of the bucket (exclusive)
//  summary: LogSummary — the LogSummary for entries in this bucket
//
//  Requirements:
//
//  Windows are aligned to multiples of windowSeconds from Unix epoch 0 (e.g. for a 60-second window, buckets are [0,60), [60,120), [120,180), etc.)
//  Only windows that contain at least one matching log entry are returned
//  Windows are returned sorted by windowStart ascending
//  filter is applied before windowing — only logs matching the filter are bucketed
//  windowSeconds must be a positive integer; throw if not
enum class LogLevel {
    INFO,
    WARN,
    ERROR
}

class LogSummary(
    val totalCount: Int,
    val countByLevel: Map<LogLevel, Int>,
    val countByService: Map<String, Int>,
    val firstTimestamp: Long?,
    val lastTimestamp: Long?,
)

class LogFilter(
    val service: String? = null,
    val level: LogLevel? = null,
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null,
    val messageContains: String? = null,
) {
    fun matches(logEntry: LogEntry): Boolean {
        return levelMatches(logEntry.level) &&
                serviceMatches(logEntry.service) &&
                messageMatches(logEntry.message) &&
                timestampMatches(logEntry.timestamp)
    }

    private fun levelMatches(logLevel: LogLevel): Boolean {
        return level == null || level == logLevel
    }

    private fun serviceMatches(logService: String): Boolean {
        return service == null || service == logService
    }

    private fun messageMatches(logMessage: String): Boolean {
        return messageContains == null || logMessage.contains(messageContains, ignoreCase = true)
    }

    private fun timestampMatches(logTimestamp: Long): Boolean {
        val fromMatches = if (fromTimestamp == null) true else {
            logTimestamp >= fromTimestamp
        }
        val toMatches = if (toTimestamp == null) true else {
            logTimestamp <= toTimestamp
        }

        return fromMatches && toMatches
    }
}

class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val service: String,
    val message: String
) {
    fun formattedMessage() = "$timestamp $level $service $message"
}

class LogCollection {
    private val logs = TreeMap<Long, LogEntry>()

    companion object {
        const val MIN_LENGTH = 4
    }

    fun addLogEntry(entry: String) {
        val segments = entry.split(" ")

        if (segments.size < MIN_LENGTH) {
            throw IllegalArgumentException("Invalid log entry: $entry")
        }

        val timestampStr = segments[0]
        val levelStr = segments[1]
        val service = segments[2]
        val message = segments.drop(3).joinToString(" ")
        val timestamp = runCatching {
            timestampStr.toLong()
        }.getOrElse { throw IllegalStateException("Invalid timestamp: $timestampStr") }

        val level = runCatching {
            LogLevel.valueOf(levelStr)
        }.getOrElse { throw IllegalStateException("Invalid level: $levelStr") }

        if (service.isEmpty()) {
            throw IllegalArgumentException("Service not found: $service")
        }

        val entry = LogEntry(timestamp, level, service, message)
        logs[entry.timestamp] = entry
    }

    fun getLogs(
        logFilter: LogFilter = LogFilter()
    ): List<LogEntry> {
        return logs.values.filter {
            logFilter.matches(it)
        }
    }
}

class LogSystem {
    private val logs = LogCollection()

    fun ingest(line: String) {
        logs.addLogEntry(line)
    }

    fun getLogs(service: String): List<LogEntry> {
        return logs.getLogs(LogFilter(
            service = service
        ))
    }

    fun getErrors(): List<LogEntry> {
        return logs.getLogs(LogFilter(
            level = LogLevel.ERROR
        ))
    }

    fun query(logFilter: LogFilter): List<LogEntry> {
        return logs.getLogs(logFilter)
    }

    fun aggregate(filter: LogFilter): LogSummary {
        val results = query(filter)
        return LogSummary(
            totalCount = results.size,
            countByLevel = results.groupingBy { it.level }.eachCount(),
            countByService = results.groupingBy { it.service }.eachCount(),
            firstTimestamp = results.firstOrNull()?.timestamp,
            lastTimestamp = results.lastOrNull()?.timestamp,
        )
    }
}

fun main() {
    val logSystem = LogSystem()

    logSystem.ingest("1700000000 ERROR payments Service unavailable")
    logSystem.ingest("1700000000 ERROR payments Service unavailable")
    logSystem.ingest("1700000001 INFO auth User login successful")
    logSystem.ingest("1700000002 WARN payments Retry attempt 1")
    logSystem.ingest("1700000003 ERROR auth Token validation failed")
    logSystem.ingest("1700000004 INFO payments Payment processed")
    println("getLogs('payments')")
    logSystem.getLogs("payments").forEach { println(it.formattedMessage()) }
    println("getLogs('auth')")
    logSystem.getLogs("auth").forEach { println(it.formattedMessage()) }
    println("getError()")
    logSystem.getErrors().forEach { println(it.formattedMessage()) }
    println("query()")
    logSystem.query(LogFilter()).forEach { println(it.formattedMessage()) }
    println("query(fromTimestamp = 1700000003, toTimestamp = 1700000003)")
    logSystem.query(LogFilter(fromTimestamp = 1700000003, toTimestamp = 1700000003)).forEach { println(it.formattedMessage()) }
    println("aggregate()")
    val summary = logSystem.aggregate(LogFilter())
    println("totalCount=${summary.totalCount}")
    println("countByLevel=${summary.countByLevel}")
    println("countByService=${summary.countByService}")
    println("firstTimestamp=${summary.firstTimestamp}")
    println("lastTimestamp=${summary.lastTimestamp}")
}