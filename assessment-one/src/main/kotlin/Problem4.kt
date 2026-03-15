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
    val graph = Array(n) { mutableListOf<Edge>() }
    for (e in edges) {
        val u = e[0] as Int; val v = e[1] as Int
        val t = e[2] as Int; val r = e[3] as Double
        graph[u].add(Edge(v, t, r))
    }

    // Min-heap on time; bestReliability[node] tracks the max reliability of any
    // settled path to that node — used to prune dominated states on pop.
    val pq = PriorityQueue<State>(compareBy { it.time })
    val bestReliability = DoubleArray(n) { 0.0 }

    pq.offer(State(0, src, 1.0))

    while (pq.isNotEmpty()) {
        val (time, node, rel) = pq.poll()

        // A later-arriving state already settled this node with higher reliability.
        if (rel < bestReliability[node]) continue
        bestReliability[node] = rel

        if (node == dst) {
            return if (rel >= minReliability) time else continue
        }

        for (edge in graph[node]) {
            val newRel = rel * edge.reliability
            // Only enqueue if this path offers better reliability than anything
            // already settled for the neighbor (pruning dominated pushes).
            if (newRel > bestReliability[edge.to]) {
                pq.offer(State(time + edge.time, edge.to, newRel))
            }
        }
    }

    return -1
}

fun main() {
    val edges = listOf(
        listOf(0, 1, 4,  0.90),
        listOf(0, 2, 2,  0.80),
        listOf(1, 3, 3,  0.95),
        listOf(2, 3, 5,  0.70),
        listOf(0, 3, 10, 1.00)
    )
    // 0→1→3: time=7, rel=0.855 ✓  |  0→2→3: time=7, rel=0.56 ✗  |  0→3: time=10, rel=1.0 ✓
    println(minTravelTime(4, edges, 0, 3, 0.75))   // Expected: 7

    // All paths fall below minReliability
    println(minTravelTime(4, edges, 0, 3, 0.99))   // Expected: 10 (only 0→3 direct at 1.0 qualifies... wait 1.0>=0.99)
    // Actually: 0→3 direct rel=1.0 >= 0.99, time=10. Expected: 10

    // No path to dst at all
    println(minTravelTime(4, listOf(
        listOf(0, 1, 5, 0.9)
    ), 0, 3, 0.5))                                  // Expected: -1

    // Direct single-hop path
    println(minTravelTime(2, listOf(
        listOf(0, 1, 3, 0.80)
    ), 0, 1, 0.75))                                 // Expected: 3

    // Faster path fails reliability; slower path qualifies
    println(minTravelTime(3, listOf(
        listOf(0, 1, 1, 0.40),
        listOf(0, 2, 2, 0.50),
        listOf(1, 2, 1, 0.40),  // 0→1→2: time=2, rel=0.16 ✗
        listOf(0, 2, 5, 0.90)   // 0→2 direct: time=5, rel=0.90 ✓
    ), 0, 2, 0.80))                                 // Expected: 5
}