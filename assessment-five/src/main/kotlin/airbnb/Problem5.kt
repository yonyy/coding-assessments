package airbnb

import java.util.PriorityQueue

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
        val target = passingFees.size - 1
        val pq = PriorityQueue<Node>(
            compareBy { it.cost }
        )
        val bidirectionalEdges = Array<MutableList<Edge>>(passingFees.size) { mutableListOf() }

        edges.forEachIndexed { idx, edge ->
            val from = edge[0]
            val to = edge[1]
            val time = edge[2]

            bidirectionalEdges[from].add(Edge(id = idx, to = to, time = time))
            bidirectionalEdges[to].add(Edge(id = idx, to = from, time = time))
        }

        pq.offer(
        Node(
                node = 0,
                time = 0,
                cost = passingFees[0]
            )
        )

        val results = mutableListOf<Int>()
        while (pq.isNotEmpty()) {
            val node = pq.poll()

            if (node.node == target) {
                if (node.time <= maxTime) {
                    results.add(node.cost)
                }
                continue
            }

            val updatedSeen = node.seen.toMutableSet().apply { add(node.node) }
            bidirectionalEdges[node.node].forEach { to ->
                if (to.to !in node.seen) {
                    pq.offer(
                        Node(
                            node = to.to,
                            time = node.time + to.time,
                            cost = node.cost + passingFees[to.to],
                            seen = updatedSeen,
                        )
                    )
                }
            }
        }

        return if (results.isEmpty()) -1 else results.min()
    }
}