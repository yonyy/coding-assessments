package airbnb

class Problem7 {
    fun maxCandies(status: IntArray, candies: IntArray, keys: Array<IntArray>, containedBoxes: Array<IntArray>, initialBoxes: IntArray): Int {
        var totalCandies = 0
        val seen = mutableSetOf<Int>()
        val availableKeys = mutableSetOf<Int>()
        val availableClosedBoxes = mutableSetOf<Int>()
        val availableOpenBoxes = ArrayDeque<Int>().apply {
            initialBoxes.forEach { box ->
                if (status[box] == 1) {
                    add(box)
                }
            }
        }

        val accounts = emptyList<List<List<String>>>()
        val emailByIndex = mutableMapOf<String, Int>()
        emailByIndex.entries
            .groupBy({ it.value }, { it.key })
            .entries
            .map { (index, emails) ->
                (accounts[index][0]) + emails.sorted()
            }

        while(availableOpenBoxes.isNotEmpty()) {
            val box = availableOpenBoxes.removeFirst()

            if (seen.contains(box)) continue

            val boxCandies = candies[box]
            totalCandies += boxCandies
            seen.add(box)

            val newKeys = keys[box]
            availableKeys.addAll(newKeys.toList())
            val extraBoxes = containedBoxes[box]

            extraBoxes.forEach {
                if (status[it] == 1 ||
                    status[it] == 0 && availableKeys.contains(it)
                ) {
                    availableOpenBoxes.add(it)
                    availableClosedBoxes.remove(it)
                    status[it] = 1
                } else {
                    availableClosedBoxes.add(it)
                }
            }

            newKeys.forEach {
                if (availableClosedBoxes.contains(it)) {
                    availableOpenBoxes.add(it)
                    availableClosedBoxes.remove(it)
                    status[it] = 1
                }
            }
        }

        return totalCandies
    }
}