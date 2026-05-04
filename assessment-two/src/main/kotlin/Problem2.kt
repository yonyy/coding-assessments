/*
 * Problem 2 — Substitution Chains
 * Difficulty: Medium | Estimated Time: ~20 min | Tags: Hash Map, Simulation
 *
 * When a requested item is out of stock, Instacart allows shoppers to substitute
 * it. Substitutions can chain: item A is replaced by B, B by C, and so on.
 *
 * Given a map of substitutions where substitutions[item] is the replacement for
 * item, and a starting item, return the final item at the end of the chain.
 * A chain ends when an item has no substitution. Assume there are no cycles.
 *
 * Examples:
 *   subs = {"apple"->"pear", "pear"->"mango", "mango"->"grape"}, start = "apple"
 *   → Output: "grape"
 *
 *   subs = {"cola"->"pepsi"}, start = "water" → Output: "water"
 *
 * Constraints:
 *   - 0 ≤ substitutions.size ≤ 10^4
 *   - No cycles in the substitution chain
 *   - All keys and values are non-empty lowercase strings
 */

fun finalItem(substitutions: Map<String, String>, start: String): String {
    // TODO: follow the chain until no substitution exists
    TODO("Not yet implemented")
}

fun main() {
    val subs1 = mapOf("apple" to "pear", "pear" to "mango", "mango" to "grape")
    println(finalItem(subs1, "apple"))  // Expected: grape

    val subs2 = mapOf("cola" to "pepsi")
    println(finalItem(subs2, "water"))  // Expected: water

    println(finalItem(emptyMap(), "milk")) // Expected: milk
}
