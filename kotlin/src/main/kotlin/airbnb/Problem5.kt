package airbnb

import java.util.PriorityQueue

/* Problem 5 — Minimum Cost With Max Time: find the minimum cost path from node 0 to
   the last node within a time budget, where each node has a passing fee. */

class Problem5 {
    data class Node(
        val node: Int,
        val time: Int,
        val cost: Int,
        val seen: Set<Int> = emptySet(),
    )

    data class Edge(
        val id: Int,
        val to: Int,
        val time: Int,
    )

    fun minCost(maxTime: Int, edges: Array<IntArray>, passingFees: IntArray): Int {
        TODO("Not yet implemented")
    }
}
