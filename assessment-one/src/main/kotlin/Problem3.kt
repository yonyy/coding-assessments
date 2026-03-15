fun longestBatch(aisles: IntArray, k: Int): Int {
    val frequencyMap = mutableMapOf<Int, Int>()
    var leftIndex = 0
    var maxSize = 0
    var distinctCount = 0

    aisles.forEachIndexed { index, aisleId ->
        val newCount = (frequencyMap[aisleId] ?: 0) + 1
        if (newCount == 1) distinctCount++
        frequencyMap[aisleId] = newCount

        while (distinctCount > k) {
            val leftId = aisles[leftIndex]
            val leftCount = frequencyMap[leftId]!! - 1
            if (leftCount == 0) {
                frequencyMap.remove(leftId)
                distinctCount--
            } else {
                frequencyMap[leftId] = leftCount
            }
            leftIndex++
        }

        maxSize = maxOf(maxSize, index - leftIndex + 1)
    }

    return maxSize
}

fun main() {
    println(longestBatch(intArrayOf(1, 2, 1, 3, 4, 2, 3), 2)) // Expected: 3
    println(longestBatch(intArrayOf(1, 2, 3, 4, 5), 3))       // Expected: 3
    println(longestBatch(intArrayOf(1, 1, 1, 1), 2))           // Expected: 4
    println(longestBatch(intArrayOf(1, 2, 1, 2, 3), 2))        // Expected: 4
}