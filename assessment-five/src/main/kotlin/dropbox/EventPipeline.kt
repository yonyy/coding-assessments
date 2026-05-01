package dropbox

import java.util.concurrent.atomic.AtomicInteger

// Background
// You are building an in-memory event processing pipeline. Events are ingested from a stream and your system must process, deduplicate, and replay them with ordering guarantees.
// All levels build on the previous. Design your Level 1 with extensibility in mind — read all four levels before writing a line.

// Level 1 — Consume and query events
// Your system receives events as raw strings in the following format:
// <eventId> <timestamp> <topic> <payload>

// eventId — a unique string identifier (no spaces, alphanumeric)
// timestamp — Unix epoch milliseconds (long integer)
// topic — a single alphanumeric word (no spaces)
// payload — the remainder of the line (may contain spaces)

// Example input:
// evt001 1700000000000 payments charge.created amount=100
// evt002 1700000001000 auth user.login userId=42
// evt003 1700000002000 payments charge.updated amount=150
// evt004 1700000003000 auth user.logout userId=42
// evt005 1700000004000 payments charge.failed amount=150

// Implement an EventPipeline class supporting:
// publish(line: String) — parse and store a single event line. Throws if the line is malformed.
// getEvents(topic: String): List<Event> — return all events for a given topic, sorted by timestamp ascending.
// getEvent(eventId: String): Event? — return the event with the given id, or null if not found.
// Requirements:

// Define an Event data class with fields: eventId, timestamp, topic, payload
// Throw a descriptive exception for malformed lines (missing fields, non-long timestamp, blank eventId/topic)
// getEvents() returns an empty list if no events exist for that topic

// Level 2 — Deduplication
// Extend your system so that duplicate events are silently ignored.
// An event is a duplicate if its eventId has already been ingested — regardless of whether any other fields differ.
// Add the following method:
// publishBatch(lines: List<String>): PublishResult
// PublishResult must contain:

// accepted: Int — count of events successfully ingested
// duplicates: Int — count of events rejected as duplicates
// errors: Int — count of lines that failed to parse

// Requirements:

// publish() must also silently ignore duplicates (no exception thrown)
// Duplicate detection is by eventId only
// Parse errors within a batch do not abort the batch — process all lines, count failures
// Your existing getEvents() and getEvent() methods must continue to work unchanged


// Level 3 — Replay
// Add the ability to replay events from a given point in time:
// replay(topic: String, fromTimestamp: Long): List<Event>
// Returns all events for the given topic with timestamp >= fromTimestamp, sorted by timestamp ascending. If two events share the same timestamp, order them by eventId lexicographically ascending.
// Add a consumer registration system:
// subscribe(topic: String, consumer: EventConsumer)
// EventConsumer is an interface:
// interface EventConsumer {
//     fun onEvent(event: Event)
// }
// replayTo(topic: String, fromTimestamp: Long, consumer: EventConsumer) — replays matching events by calling consumer.onEvent(event) for each, in the same order as replay().
// Requirements:

// subscribe() registers a consumer for future events on that topic — when publish() or publishBatch() ingests a new (non-duplicate) event, all consumers subscribed to that topic are called synchronously in registration order
// A topic may have multiple consumers
// replay() and replayTo() do not trigger registered consumers — replay is read-only
// Errors thrown by a consumer do not abort processing of remaining consumers


// Level 4 — Ordering guarantees
// Your pipeline must now enforce strict ordering invariants:
// publishOrdered(line: String, expectedSequence: Long)
// Each topic maintains an internal sequence counter starting at 0, incremented by 1 for each accepted event (duplicates do not increment). publishOrdered throws an OutOfOrderException if expectedSequence does not match the topic's current sequence counter at the time of ingestion.
// Add:
// getSequence(topic: String): Long — returns the current sequence counter for a topic (0 if no events accepted yet).
// replayFrom(topic: String, fromSequence: Long): List<Event> — returns all events for the topic with sequence number >= fromSequence, sorted by sequence ascending. Each event must carry its assigned sequence number.
// Requirements:

// Sequence counters are per-topic, start at 0, increment only on accepted (non-duplicate) events
// OutOfOrderException must carry the topic, expected sequence, and actual sequence provided
// publish() and publishBatch() are unaffected — they do not enforce ordering
// replayFrom() requires Event to carry a sequence: Long? field — null for events ingested without ordering enforcement, assigned for events ingested via publishOrdered()
// Sequence numbers must survive deduplication — a duplicate submitted via publishOrdered() is silently ignored and does not increment the counter


// Notes

// You may code in any language
// Partial credit is awarded — submit after each level
// Plan for 90 minutes of uninterrupted time
// All data is in-memory; no persistence required
class OutOfOrderException: Exception()

interface EventConsumer {
    fun onEvent(event: Event)
}

data class PublishResult(
    val accepted: Int,
    val duplicates: Int,
    val errors: Int
)

data class EventFilter(
    val id: String? = null,
    val fromTimestamp: Long? = null,
    val topic: String? = null,
    val fromSequence: Long? = null
) {
    fun matches(event: Event): Boolean {
      return idMatches(event) &&
              topicMatches(event) &&
              fromTimestampMatches(event) &&
              fromSequenceMatches(event)
    }

    private fun idMatches(event: Event): Boolean {
        return id == null || event.id == id
    }

    private fun topicMatches(event: Event): Boolean {
        return topic == null || event.topic == topic
    }

    private fun fromTimestampMatches(event: Event): Boolean {
        return fromTimestamp == null || event.timestamp >= fromTimestamp
    }

    private fun fromSequenceMatches(event: Event): Boolean {
        return fromSequence == null ||
                (event.sequenceNumber != null && event.sequenceNumber >= fromSequence)
    }

    // TODO: fill out more filters as needed
}

data class Event(
    val id: String,
    val timestamp: Long,
    val topic: String,
    val payload: String,
    val sequenceNumber: Long? = null
) {
    companion object {
        const val MIN_LENGTH = 4
        fun parseFrom(line: String): Event {
            if (line.isBlank()) {
                throw IllegalArgumentException("Empty event")
            }

            val segments = line.split(" ")
            if (segments.size < MIN_LENGTH) {
                throw IllegalArgumentException("Invalid event: $line")
            }

            val id = segments[0].takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Event id can't be empty")
            val timestamp = runCatching {
                segments[1].toLong()
            }.getOrElse { throw IllegalArgumentException("Invalid timestamp: ${segments[1]}") }
            val topic = segments[2].takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Topic can't be empty")
            val payload = segments.drop(3).joinToString(" ")

            return Event(
                id = id,
                timestamp = timestamp,
                topic = topic,
                payload = payload
            )
        }
    }

    fun formattedMessage(): String = "$id $timestamp $topic $payload"
}

class EventPipeline {
    private val events = mutableMapOf<String, Event>()
    private val subscribedTopics = mutableMapOf<String, MutableList<EventConsumer>>()
    private val topicSequenceNumbers = mutableMapOf<String, AtomicInteger>()

    fun publish(line: String) {
        val event = Event.parseFrom(line)
        publish(event)
    }

    private fun publish(event: Event) {
        events.putIfAbsent(event.id, event)
        subscribedTopics[event.topic]?.forEach { consumer ->
            runCatching {
                consumer.onEvent(event)
            }
        }
    }

    fun getEvents(topic: String): List<Event> {
        return getEvents(EventFilter(topic = topic))
    }

    fun getEvent(eventId: String): Event? {
        return events[eventId]
    }

    fun publishBatch(lines: List<String>): PublishResult {
        val batchEvents = lines.map {
            runCatching {
                Event.parseFrom(it)
            }.getOrElse { null }
        }
        val validEvents = batchEvents.filterNotNull()
        val erroredCount = batchEvents.size - validEvents.size
        val newEvents = validEvents.filter { events[it.id] == null }
        val duplicateCount = validEvents.size - newEvents.size

        newEvents.forEach {
            publish(it)
        }

        return PublishResult(
            accepted = newEvents.size,
            duplicates = duplicateCount,
            errors = erroredCount
        )
    }

    fun replay(topic: String, fromTimestamp: Long): List<Event> {
        return getEvents(EventFilter(topic = topic, fromTimestamp = fromTimestamp))
    }

    fun subscribe(topic: String, consumer: EventConsumer) {
        subscribedTopics.getOrPut(topic) { mutableListOf() }.add(consumer)
    }

    fun replayTo(topic: String, fromTimestamp: Long, consumer: EventConsumer) {
        replay(topic, fromTimestamp).forEach {
            consumer.onEvent(it)
        }
    }

    fun publishOrdered(line: String, expectedSequence: Long) {
        val event = Event.parseFrom(line)
        val currentSequence = topicSequenceNumbers.getOrPut(event.topic) { AtomicInteger(0) }
        if (currentSequence.toLong() != expectedSequence) {
            throw OutOfOrderException()
        }

        val eventWithSequence = Event(
            id = event.id,
            timestamp = event.timestamp,
            topic = event.topic,
            payload = event.payload,
            sequenceNumber = currentSequence.toLong()
        )
        publish(eventWithSequence)
        topicSequenceNumbers[event.topic]?.incrementAndGet()
    }

    fun getSequence(topic: String): Long {
        return topicSequenceNumbers.getOrPut(topic) { AtomicInteger(0) }.toLong()
    }

    fun replayFrom(topic: String, fromSequence: Long): List<Event> {
        return getEvents(EventFilter(topic = topic, fromSequence = fromSequence))
    }

    private fun getEvents(filter: EventFilter): List<Event> {
        return events.values.filter {
            filter.matches(it)
        }.sortedWith(
            compareBy<Event> { it.timestamp }.thenBy { it.id }
        )
    }
}

class SimpleEventConsumer: EventConsumer {
    override fun onEvent(event: Event) {
        println("Consumed Event: ${event.id}")
    }
}

fun main() {
    val event1 = "evt001 1700000000000 payments charge.created amount=100"
    val event2 = "evt002 1700000001000 auth user.login userId=42"
    val event3 = "evt003 1700000002000 payments charge.updated amount=150"
    val event4 = "evt004 1700000003000 auth user.logout userId=42"
    val event5 = "evt005 1700000004000 payments charge.failed amount=150"
    val event6 = "evt006 1700000004000 payments charge.failed amount=250"

    val pipeline = EventPipeline()
    val eventConsumer1 = SimpleEventConsumer()
    pipeline.subscribe("payments", eventConsumer1)
    pipeline.publish(event1)
    pipeline.publish(event2)
    pipeline.publish(event3)
    pipeline.publish(event4)
    pipeline.publish(event5)
    println("pipeline.getEvents(\"payments\")")
    pipeline.getEvents("payments").forEach { println(it.formattedMessage()) }
    println("pipeline.getEvent(\"evt001\")")
    println(pipeline.getEvent("evt001")?.formattedMessage())
    println("pipeline.publishBatch(all duplicates)")
    println(pipeline.publishBatch(listOf(event1, event2, event3, event4, event5)))
    println("pipeline.publishBatch(all invalid)")
    println(pipeline.publishBatch(listOf("not valid", "evt005 1700000004000", "evt005 string payments charge.failed amount=150")))
    println("pipeline.publishBatch(mix)")
    println(pipeline.publishBatch(listOf(event6, event1, "evt005 string payments charge.failed amount=150")))
    println("pipeline.getEvents(\"payments\")")
    pipeline.getEvents("payments").forEach { println(it.formattedMessage()) }
    println("pipeline.replay(\"payments\", 1700000004000)")
    pipeline.replay("payments", 1700000004000).forEach { println(it.formattedMessage()) }
    println("pipeline.replayTo(\"payments\", 1700000004000, eventConsumer1)")
    pipeline.replayTo("payments", 1700000004000, eventConsumer1)
}