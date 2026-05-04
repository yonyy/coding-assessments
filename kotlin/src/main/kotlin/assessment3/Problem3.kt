package assessment3

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
    TODO("Not yet implemented")
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
    )) // Expected: A before B and C, both before D
}
