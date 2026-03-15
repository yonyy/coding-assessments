fun maxDeliveries(deliveries: List<IntArray>): Int {
    val sorted = deliveries.sortedBy { it[1] }
    var lastEnd = Int.MIN_VALUE
    var count = 0

    for (delivery in sorted) {
        if (delivery[0] >= lastEnd) {
            count++
            lastEnd = delivery[1]
        }
    }

    return count
}

fun main() {
    println(maxDeliveries(listOf(
        intArrayOf(1, 3), intArrayOf(2, 4), intArrayOf(3, 5),
        intArrayOf(6, 8), intArrayOf(7, 9), intArrayOf(8, 10)
    ))) // Expected: 4  — [1,3],[3,5],[6,8],[8,10] all compatible (end==start is not an overlap)

    println(maxDeliveries(listOf(
        intArrayOf(1, 10), intArrayOf(2, 3), intArrayOf(4, 5)
    ))) // Expected: 2  — [2,3],[4,5]

    println(maxDeliveries(listOf(
        intArrayOf(1, 2), intArrayOf(3, 4), intArrayOf(5, 6)
    ))) // Expected: 3  — no overlaps, take all

    println(maxDeliveries(listOf(
        intArrayOf(1, 5), intArrayOf(2, 3), intArrayOf(4, 6)
    ))) // Expected: 2  — [2,3],[4,6]

    println(maxDeliveries(listOf(
        intArrayOf(5, 5)
    ))) // Expected: 1  — single delivery
}