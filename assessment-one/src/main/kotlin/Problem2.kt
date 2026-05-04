/*
 * Problem 2 — Delivery Time Windows
 * Difficulty: Medium | Estimated Time: ~20 min | Tags: Intervals, Greedy, Sorting
 *
 * Instacart offers delivery windows to customers. A shopper can handle one
 * delivery at a time. Given a list of delivery requests as [start, end] pairs
 * (inclusive), find the maximum number of non-overlapping deliveries a shopper
 * can complete.
 *
 * Two deliveries overlap if one starts before the other ends. A delivery ending
 * at time t and another starting at t do NOT overlap.
 *
 * Example:
 *   deliveries = [[1,3],[2,4],[3,5],[6,8],[7,9],[8,10]] → Output: 4
 *   deliveries = [[1,10],[2,3],[4,5]]                   → Output: 2
 *
 * Constraints:
 *   - 1 ≤ deliveries.size ≤ 10^5
 *   - 0 ≤ start ≤ end ≤ 10^9
 */

fun maxDeliveries(deliveries: List<IntArray>): Int {
    // TODO: classic interval scheduling —
    // sort by end time, greedily pick the earliest-ending non-overlapping delivery
    TODO("Not yet implemented")
}

fun main() {
    // println(maxDeliveries(listOf(intArrayOf(1, 3), intArrayOf(2, 4), intArrayOf(3, 5), intArrayOf(6, 8), intArrayOf(7, 9), intArrayOf(8, 10))))  // Expected: 4
    // println(maxDeliveries(listOf(intArrayOf(1, 10), intArrayOf(2, 3), intArrayOf(4, 5))))  // Expected: 2
    // println(maxDeliveries(listOf(intArrayOf(1, 2), intArrayOf(3, 4), intArrayOf(5, 6))))  // Expected: 3
    // println(maxDeliveries(listOf(intArrayOf(1, 5), intArrayOf(2, 3), intArrayOf(4, 6))))  // Expected: 2
    // println(maxDeliveries(listOf(intArrayOf(5, 5))))  // Expected: 1
}
