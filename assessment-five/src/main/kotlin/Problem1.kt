/*
 * Problem 1 — Cart Item Frequency
 * Difficulty: Easy | Estimated Time: ~15 min | Tags: Hash Map, Sorting
 *
 * Description:
 * Given a list of item names representing everything added to a customer's cart
 * (including duplicates), return the items sorted by frequency descending.
 * Break ties alphabetically.
 *
 * Examples:
 *   Example 1:
 *     Input:  listOf("apple", "banana", "apple", "orange", "banana", "apple")
 *     Output: ["apple", "banana", "orange"]
 *     Explanation: apple→3, banana→2, orange→1
 *
 *   Example 2:
 *     Input:  listOf("milk", "eggs", "milk", "bread", "eggs")
 *     Output: ["eggs", "milk", "bread"]
 *     Explanation: eggs=2, milk=2 (tie → alphabetical: eggs before milk), bread=1
 *
 * Constraints:
 *   - 1 ≤ cart.size ≤ 10^5
 *   - All item names are lowercase strings
 *   - 1 ≤ item.length ≤ 30
 */

fun sortByFrequency(cart: List<String>): List<String> {
    TODO("Not yet implemented")
}

fun main() {
    // println(sortByFrequency(listOf("apple", "banana", "apple", "orange", "banana", "apple")))
    // Expected: [apple, banana, orange]

    // println(sortByFrequency(listOf("milk", "eggs", "milk", "bread", "eggs")))
    // Expected: [eggs, milk, bread]

    // println(sortByFrequency(listOf("a")))
    // Expected: [a]
}
