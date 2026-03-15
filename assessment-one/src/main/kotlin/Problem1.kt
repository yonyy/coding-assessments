data class Item(val name: String, val stock: Int, val threshold: Int)

fun restockPriority(items: List<Item>): List<String> {
    // TODO: filter items where stock < threshold,
    // then sort by (stock - threshold) asc, break ties alphabetically
    return items
        .filter { it.stock < it.threshold }
        .sortedWith(compareBy({ it.stock - it.threshold} , { it.name }))
        .map { it.name }
}

fun main() {
    val items = listOf(
        Item("apple",  5,  10),
        Item("banana", 2,  4),
        Item("mango",  8,  8),
        Item("grape",  1,  9)
    )
    val prioritizedItems = restockPriority(items)
    println(prioritizedItems)
    // Expected: [grape, apple, banana]

    assert(prioritizedItems.size == 3)
    assert(prioritizedItems[0] == "grape")
    assert(prioritizedItems[1] == "apple")
    assert(prioritizedItems[0] == "banana")
}
