/*
 * Problem 3 — Batch Order Fulfillment
 * Difficulty: Medium-Hard | Estimated Time: ~25 min | Tags: Sliding Window, Hash Map, Two Pointers
 *
 * Instacart batches multiple customer orders into a single shopper trip. A trip
 * is valid if the shopper visits at most k distinct store aisles.
 *
 * Given a list of items as their aisle IDs in collection order and an integer k,
 * return the length of the longest contiguous sub-trip visiting at most k distinct aisles.
 *
 * Examples:
 *   aisles = [1,2,1,3,4,2,3], k = 2 → Output: 3  ([1,2,1] visits {1,2})
 *   aisles = [1,2,3,4,5],     k = 3 → Output: 3
 *   aisles = [1,1,1,1],       k = 2 → Output: 4
 *
 * Constraints:
 *   - 1 ≤ aisles.size ≤ 10^5
 *   - 1 ≤ k ≤ aisles.size
 *   - 1 ≤ aisles[i] ≤ 10^4
 */

fun longestBatch(aisles: IntArray, k: Int): Int {
    // TODO: sliding window with a frequency map —
    // expand right pointer, shrink left when distinct count > k
    TODO("Not yet implemented")
}

fun main() {
    println(longestBatch(intArrayOf(1, 2, 1, 3, 4, 2, 3), 2)) // Expected: 3
    println(longestBatch(intArrayOf(1, 2, 3, 4, 5), 3))       // Expected: 3
    println(longestBatch(intArrayOf(1, 1, 1, 1), 2))           // Expected: 4
    println(longestBatch(intArrayOf(1, 2, 1, 2, 3), 2))        // Expected: 4
}
