fun topKTips(tips: List<Double>, k: Int): List<Double> {
    // TODO
    return tips.sortedByDescending{ it }
        .take(k)
}

fun main() {
    println(topKTips(listOf(3.5, 10.0, 2.0, 8.5, 6.0), 3)) // Expected: [10.0, 8.5, 6.0]
    println(topKTips(listOf(1.0, 5.0), 5))                  // Expected: [5.0, 1.0]
    println(topKTips(listOf(4.0, 4.0, 4.0, 1.0), 2))        // Expected: [4.0, 4.0]
}
