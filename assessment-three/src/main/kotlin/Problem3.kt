import java.util.PriorityQueue
import kotlin.comparisons.compareByDescending

/*
 * Question 3: Logic & Framework Simulation (Medium-Hard)
 * Target Time: 25–30 Minutes
 * Tags: Graphs, Topological Sort, BFS (Kahn's Algorithm)
 *
 * Task: The "Dependency Resolver"
 *
 * Given a list of tasks and their dependencies, return the order in which tasks
 * should be executed. A dependency [B, A] means B depends on A (A must run before B).
 * If a circular dependency exists, return an empty list.
 *
 * Examples:
 *   tasks = ["A", "B", "C"], deps = [["B", "A"], ["C", "B"]]
 *   → Output: ["A", "B", "C"]
 *
 *   tasks = ["A", "B"], deps = [["A", "B"], ["B", "A"]]
 *   → Output: [] (cycle detected)
 *
 * Constraints:
 *   - 1 ≤ tasks.size ≤ 10^4
 *   - 0 ≤ deps.size ≤ 10^4
 *   - Task names are unique non-empty strings
 *
 * Strategy: Kahn's Algorithm (BFS topological sort).
 *   - Build an adjacency list and in-degree map.
 *   - Enqueue all tasks with in-degree 0.
 *   - Process queue: for each task, reduce in-degree of dependents; enqueue those reaching 0.
 *   - If output size < tasks.size, a cycle exists → return [].
 */

fun resolveDependencies(tasks: List<String>, deps: List<List<String>>): List<String> {
    // TODO: build graph and in-degree map, run Kahn's BFS topological sort
    // map representing task -> num of dependencies it has
    val inDegree = mutableMapOf<String, Int>().apply {
        tasks.forEach { task -> put(task, 0) }
    }
    // map representing task -> list of tasks it unblocks
    val outboundDeps = mutableMapOf<String, MutableList<String>>().apply {
        tasks.forEach { task -> put (task, mutableListOf()) }
    }

    deps.forEach { dep ->
        val from = dep[0]
        val to = dep[1]

        inDegree[from] = (inDegree[from] ?: 0) + 1
        outboundDeps[to]?.add(from)
    }

    val queue = ArrayDeque<String>()
    // add tasks that don't have deps
    val initialTasks = tasks.filter { task -> inDegree[task]!! == 0 }
    queue.addAll(initialTasks)

    val processed = mutableListOf<String>()
    while (queue.isNotEmpty()) {
        val task = queue.removeFirst()

        processed.add(task)
        outboundDeps[task]?.forEach {
            // decrement in degree once a dep is processed
            inDegree[it] = inDegree[it]!! - 1
            if (inDegree[it] == 0) {
                queue.add(it)
            }
        }
    }

    return if (processed.size > tasks.size) emptyList() else processed
}

fun main() {
    // Linear chain: A → B → C
    println(resolveDependencies(
        listOf("A", "B", "C"),
        listOf(listOf("B", "A"), listOf("C", "B"))
    )) // Expected: [A, B, C]

    // Cycle: A ↔ B
    println(resolveDependencies(
        listOf("A", "B"),
        listOf(listOf("A", "B"), listOf("B", "A"))
    )) // Expected: []

    // No dependencies — any order valid (verify all tasks present)
    println(resolveDependencies(
        listOf("X", "Y", "Z"),
        emptyList()
    )) // Expected: some permutation of [X, Y, Z]

    // Diamond: A → B, A → C, B → D, C → D
    println(resolveDependencies(
        listOf("A", "B", "C", "D"),
        listOf(listOf("B", "A"), listOf("C", "A"), listOf("D", "B"), listOf("D", "C"))
    )) // Expected: A before B and C, both before D  e.g. [A, B, C, D] or [A, C, B, D]
}
