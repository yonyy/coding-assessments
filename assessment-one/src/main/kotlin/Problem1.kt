/*
 * Problem 1 — Grocery Restock Priority
 * Difficulty: Easy | Estimated Time: ~15 min | Tags: Arrays, Sorting
 *
 * Instacart's warehouse management system tracks items that need restocking.
 * Each item has a `stock` level and a `threshold`. An item needs restocking
 * if its stock is strictly below its threshold.
 *
 * Given a list of items, return the names of items that need restocking,
 * sorted by (stock - threshold) ascending (most urgent first).
 * Break ties alphabetically.
 *
 * Example:
 *   items = [Item("apple",5,10), Item("banana",2,4), Item("mango",8,8), Item("grape",1,9)]
 *   Output: ["grape", "apple", "banana"]
 *
 * Constraints:
 *   - 1 ≤ items.size ≤ 10^4
 *   - 0 ≤ stock, threshold ≤ 10^6
 *   - All item names are unique lowercase strings
 */

data class Item(val name: String, val stock: Int, val threshold: Int)

fun restockPriority(items: List<Item>): List<String> {
    // TODO: filter items where stock < threshold,
    // then sort by (stock - threshold) asc, break ties alphabetically
    TODO("Not yet implemented")
}

fun main() {
    // println(restockPriority(listOf(Item("apple", 5, 10), Item("banana", 2, 4), Item("mango", 8, 8), Item("grape", 1, 9))))  // Expected: [grape, apple, banana]
}
