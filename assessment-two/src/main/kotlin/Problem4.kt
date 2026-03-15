import java.util.PriorityQueue
import kotlin.math.roundToInt

fun minCoverageRadius(stores: List<Int>, k: Int, roadLength: Int): Double {
    val sorted = stores.sorted()

    // canCover: greedy check — can k zones of radius r cover all stores?
    fun canCover(r: Double): Boolean {
        // TODO: greedy left-to-right sweep
        return k * (2 * r + 1) >= sorted.max()
    }

    // Binary search on r over [0.0, roadLength / 2.0]
    // TODO
    var r = 0.0
    var low = 0.0
    var upper = roadLength.toDouble()
    val pq = PriorityQueue<Double>(compareBy { it })
    while (true) {
        r = Math.ceil((low + upper) / 2.0)
        println("r = $r = ($low + $upper) / 2.0")
        if (canCover(r) && !pq.contains(r)) {
            pq.add(r)
            upper = r
            println("upper = $r")
        } else {
            break;
        }

    }

    return pq.poll()
}

fun main() {
    println(minCoverageRadius(listOf(1, 5, 9, 15), 2, 20))  // Expected: 5.0
    println(minCoverageRadius(listOf(1, 2, 3), 1, 10))       // Expected: 1.0
    println(minCoverageRadius(listOf(0, 10), 2, 10))          // Expected: 0.0
}
