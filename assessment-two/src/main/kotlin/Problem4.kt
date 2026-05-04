/*
 * Problem 4 — Store Coverage Radius
 * Difficulty: Hard | Estimated Time: ~30 min | Tags: Binary Search, Geometry, Greedy
 *
 * Instacart is placing delivery zones along a single road (number line 0..roadLength).
 * There are n stores at given positions. Place exactly k circular coverage zones
 * (each a segment of length 2r) such that all stores are covered by at least one zone.
 *
 * Find the minimum radius r (Double) such that k zones can cover all store positions.
 * Zone centers can be placed anywhere on the number line.
 *
 * Key insight: For a given r, greedily check coverage:
 *   - Sort stores.
 *   - Place zone 1 center at stores[0]+r (covers leftmost uncovered store as far right as possible).
 *   - Advance to next uncovered store, repeat. Count zones used.
 *
 * Examples:
 *   stores=[1,5,9,15], k=2, roadLength=20 → Output: 3.0
 *   stores=[1,2,3],    k=1, roadLength=10 → Output: 1.0
 *   stores=[0,10],     k=2, roadLength=10 → Output: 0.0
 *
 * Constraints:
 *   - 1 ≤ stores.size ≤ 10^4
 *   - 1 ≤ k ≤ stores.size
 *   - 0 ≤ stores[i] ≤ roadLength ≤ 10^6
 *   - All store positions are distinct
 */

fun minCoverageRadius(stores: List<Int>, k: Int, roadLength: Int): Double {
    val sorted = stores.sorted()

    // canCover: greedy check — can k zones of radius r cover all stores?
    fun canCover(r: Double): Boolean {
        // TODO: greedy left-to-right sweep
        TODO("Not yet implemented")
    }

    // Binary search on r over [0.0, roadLength / 2.0]
    // TODO
    TODO("Not yet implemented")
}

fun main() {
    println(minCoverageRadius(listOf(1, 5, 9, 15), 2, 20))  // Expected: 5.0
    println(minCoverageRadius(listOf(1, 2, 3), 1, 10))       // Expected: 1.0
    println(minCoverageRadius(listOf(0, 10), 2, 10))          // Expected: 0.0
}
