 /*
 * Problem 4 — Warehouse Dependency Build Order
 * Difficulty: Hard | Estimated Time: ~30 min | Tags: Graphs, Topological Sort, BFS (Kahn's Algorithm)
 *
 * Description:
 * Given n tasks (0-indexed) and a list of dependencies where dependencies[i] = [a, b]
 * means task b must complete before task a, return a valid topological order to complete
 * all tasks. If a cycle exists, return an empty list.
 * If multiple valid orderings exist, return the lexicographically smallest one
 * (prefer lower-numbered tasks first when there is a choice).
 *
 * Examples:
 *   n = 4, dependencies = [[1,0],[2,0],[3,1],[3,2]]
 *   Output: [0, 1, 2, 3]
 *   (0 first; then 1 and 2 both ready — pick 1; then 2; then 3)
 *
 *   n = 3, dependencies = [[0,1],[1,2],[2,0]]  → cycle → Output: []
 *
 *   n = 3, dependencies = []  → Output: [0, 1, 2]
 *
 * Constraints:
 *   - 1 ≤ n ≤ 10^4
 *   - 0 ≤ dependencies.size ≤ 5×10^4
 *   - No self-loops
 *
 * Strategy: Kahn's Algorithm with a min-heap for lex-smallest order.
 *   1. Build adjacency list and in-degree array.
 *   2. Seed the min-heap with all nodes of in-degree 0.
 *   3. Poll min node, append to result, decrement in-degrees of its neighbors.
 *      Enqueue any neighbor whose in-degree drops to 0.
 *   4. If result.size < n, a cycle was detected — return emptyList().
 */

import java.util.PriorityQueue

fun buildOrder(n: Int, dependencies: List<List<Int>>): List<Int> {
    // TODO: Kahn's algorithm (BFS topological sort)
    // map where key is a task id and the num of tasks it requires
    val tasks = (0..<n).toList()
    val inDegreeMap = mutableMapOf<Int, Int>().apply {
        tasks.forEach { this[it] = 0 }
    }
    // map where key is the task id and the value is list of tasks enabled
    val dependenciesMap = mutableMapOf<Int, MutableList<Int>>().apply {
        repeat(n) { this[it] = mutableListOf() }
    }

    dependencies.forEach {
        val from = it[0]
        val to = it[1]

        inDegreeMap[from] = inDegreeMap[from]!! + 1
        dependenciesMap[to]!!.add(from)
    }

    val pq = PriorityQueue<Int>(compareBy{ it })
    val initialTasks = tasks.filter { inDegreeMap[it] == 0 }
    pq.addAll(initialTasks)

    val orderedTasks = mutableListOf<Int>()
    while (pq.isNotEmpty()) {
        val task = pq.poll()
        orderedTasks.add(task)

        dependenciesMap[task]?.forEach {
            inDegreeMap[it] = inDegreeMap[it]!! - 1
            if (inDegreeMap[it] == 0) {
                pq.add(it)
            }
        }
    }

    return if (orderedTasks.size < tasks.size) { emptyList() } else { orderedTasks }
}

fun main() {
    println(buildOrder(4, listOf(listOf(1,0), listOf(2,0), listOf(3,1), listOf(3,2))))
    // Expected: [0, 1, 2, 3]

    println(buildOrder(3, listOf(listOf(0,1), listOf(1,2), listOf(2,0))))
    // Expected: []

    println(buildOrder(3, emptyList()))
    // Expected: [0, 1, 2]

    println(buildOrder(6, listOf(listOf(5,2), listOf(5,0), listOf(4,0), listOf(4,1), listOf(2,3), listOf(3,1))))
    // Expected: [0, 1, 3, 2, 4, 5]
}
