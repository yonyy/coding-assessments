/*
 * Problem 3 — Freshness Window
 * Difficulty: Medium-Hard | Estimated Time: ~25 min | Tags: Sliding Window, Deque, Monotonic Queue
 *
 * Description:
 * Given an array of freshness scores and a window size w, return a list where each
 * element is the maximum freshness score in each contiguous window of size w.
 * There are scores.size - w + 1 windows total. Solve in O(n) time.
 *
 * Examples:
 *   scores = [3, 1, 5, 4, 2, 7, 6], w = 3 → [5, 5, 5, 7, 7]
 *     [3,1,5]→5  [1,5,4]→5  [5,4,2]→5  [4,2,7]→7  [2,7,6]→7
 *
 *   scores = [9, 8, 7, 6], w = 2 → [9, 8, 7]
 *
 * Constraints:
 *   - 1 ≤ w ≤ scores.size ≤ 10^5
 *   - 0 ≤ scores[i] ≤ 10^6
 *
 * Strategy: Monotonic decreasing deque of indices.
 *   - For each index i:
 *     1. Remove from front if the index is outside the current window (i - deque.front > w-1).
 *     2. Remove from back while scores[back] ≤ scores[i] (maintain decreasing order).
 *     3. Add i to the back.
 *     4. Once i >= w-1, the front of the deque is the index of the window's max.
 */

fun maxFreshness(scores: IntArray, w: Int): List<Int> {
    // TODO: monotonic decreasing deque — stores indices
    TODO("Not yet implemented")
}

fun main() {
    println(maxFreshness(intArrayOf(3,1,5,4,2,7,6), 3)) // Expected: [5,5,5,7,7]
    println(maxFreshness(intArrayOf(9,8,7,6), 2))        // Expected: [9,8,7]
    println(maxFreshness(intArrayOf(1,3,1,2), 1))        // Expected: [1,3,1,2]
    println(maxFreshness(intArrayOf(4,4,4,4), 4))        // Expected: [4]
}
