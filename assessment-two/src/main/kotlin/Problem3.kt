import java.util.PriorityQueue

data class Order(val id: Int, val arrivalTime: Int, val processingTime: Int)

fun processOrders(orders: List<Order>): List<Int> {
    // TODO: simulate shortest-job-first scheduling
    // Sort by arrival. Use a min-heap on (processingTime, id) for available orders.
    val pq = PriorityQueue<Order>(compareBy({ it.arrivalTime }, { it.processingTime }, { it.id }))
    pq.addAll(orders)


    val processed = mutableListOf<Order>()
    var currentTime = 0
    while (pq.isNotEmpty()) {
        val nextOrder = pq.peek()
        if (nextOrder.arrivalTime <= currentTime) {
            processed.add(pq.poll())
            currentTime += nextOrder.processingTime
        } else {
         currentTime = nextOrder.arrivalTime
        }
    }

    return processed.map { it.id }
}

fun main() {
    val orders = listOf(
        Order(1, 0, 5),
        Order(2, 0, 3),
        Order(3, 2, 1),
        Order(4, 6, 2)
    )
    println(processOrders(orders)) // Expected: [2, 3, 1, 4]

    // All arrive at once — pick by shortest then by id
    val orders2 = listOf(
        Order(3, 0, 2),
        Order(1, 0, 5),
        Order(2, 0, 2)
    )
    println(processOrders(orders2)) // Expected: [2, 3, 1]
}
