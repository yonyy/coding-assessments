import kotlin.math.max

/*
 * Problem 3 — Restock Trip Minimum Cost
 * Difficulty: Medium-Hard | Estimated Time: ~25 min | Tags: Dynamic Programming, Arrays
 *
 * Description:
 * A shopper travels along a straight route of n stops (0-indexed). At each stop they
 * can pick up stock. Each stop i has a cost[i] to visit. From stop i, the shopper can
 * jump to any stop j where i < j ≤ i + maxJump.
 *
 * The shopper starts at stop 0 (paying cost[0]) and must reach stop n - 1.
 * Find the minimum total cost to travel from stop 0 to stop n - 1.
 *
 * Examples:
 *   Example 1:
 *     Input:  cost = [10, 15, 20, 5, 1, 50], maxJump = 2
 *     Output: 80
 *     Explanation:
 *       dp[0]=10, dp[1]=25, dp[2]=30, dp[3]=30, dp[4]=31, dp[5]=80
 *       Optimal: 0→1→3→5 costs 10+15+5+50 = 80
 *
 *   Example 2:
 *     Input:  cost = [1,100,1,1,1,100,1,1,100,1], maxJump = 3
 *     Output: 6
 *     Explanation: Jump over expensive stops — 0→2→3→4→6→7→9: 1+1+1+1+1+1=6
 *
 * Constraints:
 *   - 2 ≤ cost.size ≤ 10^5
 *   - 1 ≤ cost[i] ≤ 10^4
 *   - 1 ≤ maxJump ≤ cost.size
 *
 * Strategy:
 *   dp[i] = cost[i] + min(dp[i-1], dp[i-2], ..., dp[i-maxJump])
 *   For small maxJump the inner loop is O(n * maxJump); a monotonic deque gives O(n).
 */

fun minTripCost(cost: IntArray, maxJump: Int): Int {
    // TODO: dp[i] = min cost to reach stop i
    // dp[i] = cost[i] + min(dp[i-1], dp[i-2], ..., dp[i-maxJump])
    val minCosts = IntArray(cost.size) { 0 }
    minCosts[0] = cost[0]

    for (i in 1..<cost.size) {
        var minBeforeJump = minCosts[i - 1]
        var j = i - 2
        while (j >= max(i - maxJump, 0)) {
            if (minCosts[j] < minBeforeJump) {
                minBeforeJump = minCosts[j]
            }
            j--
        }

        minCosts[i] = cost[i] + minBeforeJump
    }

    return minCosts.last()
}

// Alternative — O(n) using monotonic increasing deque (sliding window minimum)
// fun minTripCost(cost: IntArray, maxJump: Int): Int {
//     val n = cost.size
//     val dp = IntArray(n)
//     dp[0] = cost[0]
//     val deque = ArrayDeque<Int>()   // stores indices; front = min dp index in window
//     deque.addLast(0)
//     for (i in 1 until n) {
//         // expire indices that have fallen outside the jump window
//         if (deque.first() < i - maxJump) deque.removeFirst()
//         // dp[i] = cost[i] + cheapest reachable predecessor
//         dp[i] = cost[i] + dp[deque.first()]
//         // maintain increasing order: pop back while its dp value >= dp[i]
//         while (deque.isNotEmpty() && dp[deque.last()] >= dp[i]) deque.removeLast()
//         deque.addLast(i)
//     }
//     return dp[n - 1]
// }

fun main() {
    println(minTripCost(intArrayOf(10, 15, 20, 5, 1, 50), 2))
    // Expected: 80

    println(minTripCost(intArrayOf(1, 100, 1, 1, 1, 100, 1, 1, 100, 1), 3))
    // Expected: 4

    println(minTripCost(intArrayOf(5, 1), 1))
    // Expected: 6

    println(minTripCost(intArrayOf(3, 2, 4, 1, 5), 3))
    // Expected: 9  (0→3→4: 3+1+5=9)
}
