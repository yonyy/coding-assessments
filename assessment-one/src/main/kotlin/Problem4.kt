/*
 * Problem 4 — Shopper Network Reliability
 * Difficulty: Hard | Estimated Time: ~30 min | Tags: Graphs, Dijkstra, Priority Queue
 *
 * Instacart models its shopper network as a weighted directed graph. Nodes are
 * fulfillment zones; edges carry a travel time (Int) and a reliability (Double,
 * 0.0–1.0 probability of being open).
 *
 * Given n zones (0-indexed), edges as [u, v, time, reliability], a src, a dst,
 * and a minReliability threshold, find the minimum travel time from src to dst
 * along any path whose path reliability (product of edge reliabilities) is
 * >= minReliability. Return -1 if no valid path exists.
 *
 * Example:
 *   n=4, edges=[[0,1,4,0.90],[0,2,2,0.80],[1,3,3,0.95],[2,3,5,0.70],[0,3,10,1.00]]
 *   src=0, dst=3, minReliability=0.75 → Output: 7  (path 0→1→3, rel=0.855)
 *
 * Constraints:
 *   - 1 ≤ n ≤ 10^4
 *   - 0 ≤ edges.size ≤ 5×10^4
 *   - 0.0 ≤ reliability ≤ 1.0
 *   - All edge times are positive integers
 *   - src ≠ dst
 */

import java.util.PriorityQueue

data class Edge(val to: Int, val time: Int, val reliability: Double)
data class State(val time: Int, val node: Int, val reliability: Double)

fun minTravelTime(
    n: Int,
    edges: List<List<Any>>,
    src: Int,
    dst: Int,
    minReliability: Double
): Int {
    // TODO: build adjacency list, then run modified Dijkstra
    // min-heap on time; track best (time, reliability) per node
    // Accept dst only if reliability >= minReliability
    TODO("Not yet implemented")
}

fun main() {
    // val edges = listOf(listOf(0, 1, 4, 0.90), listOf(0, 2, 2, 0.80), listOf(1, 3, 3, 0.95), listOf(2, 3, 5, 0.70), listOf(0, 3, 10, 1.00))
    // println(minTravelTime(4, edges, 0, 3, 0.75))  // Expected: 7
    // println(minTravelTime(4, edges, 0, 3, 0.99))  // Expected: 10
    // println(minTravelTime(4, listOf(listOf(0, 1, 5, 0.9)), 0, 3, 0.5))  // Expected: -1
    // println(minTravelTime(2, listOf(listOf(0, 1, 3, 0.80)), 0, 1, 0.75))  // Expected: 3
}
