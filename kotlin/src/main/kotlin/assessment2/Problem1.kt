package assessment2

/*
 * Problem 1 — Shopper Tip Calculator
 * Difficulty: Easy | Estimated Time: ~15 min | Tags: Arrays, Math, Sorting
 *
 * Instacart shoppers receive tips from customers after each delivery.
 * Given a list of tip amounts (as doubles), return the top k tips sorted
 * in descending order. If the list has fewer than k elements, return all
 * tips sorted descending.
 *
 * Examples:
 *   tips = [3.5, 10.0, 2.0, 8.5, 6.0], k = 3 → Output: [10.0, 8.5, 6.0]
 *   tips = [1.0, 5.0],                  k = 5 → Output: [5.0, 1.0]
 *
 * Constraints:
 *   - 1 ≤ tips.size ≤ 10^5
 *   - 0.0 ≤ tips[i] ≤ 1000.0
 *   - 1 ≤ k ≤ 10^5
 */

fun topKTips(tips: List<Double>, k: Int): List<Double> {
    // TODO: sort descending, take at most k elements
    TODO("Not yet implemented")
}

fun main() {
    println(topKTips(listOf(3.5, 10.0, 2.0, 8.5, 6.0), 3)) // Expected: [10.0, 8.5, 6.0]
    println(topKTips(listOf(1.0, 5.0), 5))                  // Expected: [5.0, 1.0]
    println(topKTips(listOf(4.0, 4.0, 4.0, 1.0), 2))        // Expected: [4.0, 4.0]
}
