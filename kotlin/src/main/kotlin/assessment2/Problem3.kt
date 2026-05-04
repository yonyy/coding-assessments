package assessment2

/*
 * Problem 3 — Order Wave Scheduler
 * Difficulty: Medium-Hard | Estimated Time: ~25 min | Tags: Sorting, Greedy, Simulation
 *
 * Instacart processes orders in waves. Each order has an id, arrivalTime, and
 * processingTime. A single processor handles one order at a time.
 *
 * Rules:
 *   - At time 0, the processor is free.
 *   - Always picks the available order with the shortest processingTime
 *     (ties broken by lowest id).
 *   - An order is available if arrivalTime <= currentTime.
 *   - If no order is available, jump forward to the next arrivalTime.
 *
 * Return the order IDs in the sequence they are processed.
 *
 * Example:
 *   orders = [Order(1,0,5), Order(2,0,3), Order(3,2,1), Order(4,6,2)]
 *   → Output: [2, 3, 1, 4]
 *   (t=0: pick 2 (len 3) → t=3: pick 3 (len 1) → t=4: pick 1 (len 5) → t=9: pick 4)
 *
 * Constraints:
 *   - 1 ≤ orders.size ≤ 10^5
 *   - 0 ≤ arrivalTime ≤ 10^9
 *   - 1 ≤ processingTime ≤ 10^4
 *   - All order IDs are unique
 */

import java.util.PriorityQueue

data class Order(val id: Int, val arrivalTime: Int, val processingTime: Int)

fun processOrders(orders: List<Order>): List<Int> {
    // TODO: simulate shortest-job-first scheduling
    // Sort by arrival. Use a min-heap on (processingTime, id) for available orders.
    TODO("Not yet implemented")
}

fun main() {
    val orders = listOf(
        Order(1, 0, 5),
        Order(2, 0, 3),
        Order(3, 2, 1),
        Order(4, 6, 2)
    )
    println(processOrders(orders)) // Expected: [2, 3, 1, 4]

    val orders2 = listOf(
        Order(3, 0, 2),
        Order(1, 0, 5),
        Order(2, 0, 2)
    )
    println(processOrders(orders2)) // Expected: [2, 3, 1]
}
