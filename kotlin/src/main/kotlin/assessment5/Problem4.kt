package assessment5

/*
 * Problem 4 — Regional Surge Pricing Zones
 * Difficulty: Hard | Estimated Time: ~30 min | Tags: Union-Find (DSU), Graphs
 *
 * Description:
 * Instacart divides a city into n zones (0-indexed). Zones that share a delivery
 * corridor are connected. When surge pricing is activated, it spreads to all zones
 * reachable through connected corridors — forming a surge region.
 *
 * Given:
 *   - n: number of zones
 *   - corridors: list of [a, b] pairs meaning zone a and zone b are directly connected
 *   - queries: list of [x, y] pairs
 *
 * For each query [x, y], return true if zones x and y are in the same surge region
 * (connected), false otherwise. Also return the number of distinct surge regions.
 *
 * Return a Pair<List<Boolean>, Int> — query answers and region count.
 *
 * Examples:
 *   Example 1:
 *     n = 6, corridors = [[0,1],[1,2],[3,4]], queries = [[0,2],[0,3],[3,4],[5,5]]
 *     Output: Pair([true, false, true, true], 3)
 *
 *   Example 2:
 *     n = 4, corridors = [], queries = [[0,1],[2,3]]
 *     Output: Pair([false, false], 4)
 *
 * Constraints:
 *   - 1 ≤ n ≤ 10^5
 *   - 0 ≤ corridors.size ≤ 2×10^5
 *   - 1 ≤ queries.size ≤ 10^5
 *   - 0 ≤ x, y < n
 */

class DSU(n: Int) {
    val parent = IntArray(n) { it }
    val rank = IntArray(n) { 0 }

    fun find(x: Int): Int {
        // TODO: path compression
        TODO()
    }

    fun union(a: Int, b: Int) {
        // TODO: union by rank
        TODO()
    }

    fun connected(a: Int, b: Int) = find(a) == find(b)

    fun regionCount(): Int {
        // TODO: count nodes where parent[i] == i (roots)
        TODO()
    }
}

fun surgePricingZones(
    n: Int,
    corridors: List<List<Int>>,
    queries: List<List<Int>>
): Pair<List<Boolean>, Int> {
    // TODO: build DSU, union corridors, answer queries
    TODO()
}

fun main() {
    val r1 = surgePricingZones(
        6,
        listOf(listOf(0, 1), listOf(1, 2), listOf(3, 4)),
        listOf(listOf(0, 2), listOf(0, 3), listOf(3, 4), listOf(5, 5))
    )
    println(r1) // Expected: ([true, false, true, true], 3)

    val r2 = surgePricingZones(
        4,
        emptyList(),
        listOf(listOf(0, 1), listOf(2, 3))
    )
    println(r2) // Expected: ([false, false], 4)
}
