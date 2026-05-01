package loancrate

//  Problem
//  Design a simple in-memory event bus with the following API:
//  kotlinval bus = EventBus()
//  bus.subscribe("payment.created") { event -> println(event) }
//  bus.subscribe("payment.created") { event -> log(event) }
//  bus.publish("payment.created", "txn-123")
//  bus.unsubscribe("payment.created", handlerRef)
//  Requirements:
//
//  Multiple subscribers per topic
//  publish calls all handlers for that topic
//  unsubscribe removes a specific handler by reference
//  Thread-safe (bonus: add this after the base implementation works)
typealias EventHandler = (String) -> Unit

class EventBus() {
    val subscriptions = mutableMapOf<String, MutableList<EventHandler>>()

    fun subscribe(event: String, handler: EventHandler) {
        subscriptions.getOrPut(event) { mutableListOf() }
            .add(handler)
    }

    fun publish(event: String, arg: String) {
        subscriptions[event]?.forEach {
            it(arg)
        }
    }

    fun unsubscribe(event: String, handler: EventHandler) {
        subscriptions[event]?.remove(handler)
    }
}

fun main() {
    val bus = EventBus()
    val handler: EventHandler = { e -> println("Received: $e") }
    bus.subscribe("payment.created", handler)
    bus.publish("payment.created", "txn-abc")
    bus.unsubscribe("payment.created", handler)
}