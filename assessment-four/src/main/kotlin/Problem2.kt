import kotlin.math.abs

/*
 * Problem 2 — Inventory Rebalancer
 * Difficulty: Medium | Estimated Time: ~20 min | Tags: Two Pointers, Sorting, Greedy
 *
 * Description:
 * Instacart has two warehouses. Warehouse A has quantities a: List<Int> and warehouse
 * B has b: List<Int>. You may perform at most k swaps, where each swap exchanges one
 * value from A with one from B. After at most k swaps, minimize |sum(A) - sum(B)|.
 * Note: you are swapping values between lists — list sizes stay the same.
 *
 * Example:
 *   a = [1, 2, 11, 7], b = [3, 4, 1, 2], k = 1
 *   sumA = 21, sumB = 10, diff = 11
 *   Best single swap: a[3]=7 with b[2]=1 → sumA=15, sumB=16, diff=1
 *   Output: 1
 *
 * Constraints:
 *   - 1 ≤ a.size, b.size ≤ 10^5
 *   - 1 ≤ a[i], b[i] ≤ 10^5
 *   - 0 ≤ k ≤ min(a.size, b.size)
 *
 * Strategy:
 *   - If sumA == sumB already, return 0.
 *   - Ensure sumA ≥ sumB (swap references otherwise) so we always reduce A and grow B.
 *   - Sort A descending, B ascending.
 *   - For each of the k swaps, consider the best candidate pair (A[i], B[i]).
 *     A swap is beneficial only if it reduces the gap; stop early if it would overshoot
 *     (i.e. the new diff would be larger than current).
 *   - Return the minimum |sumA - sumB| seen.
 */

fun minDiff(a: List<Int>, b: List<Int>, k: Int): Int {
    // TODO: greedy — sort A descending, B ascending, perform up to k beneficial swaps
    val sortedA = a.sortedDescending()
    val sortedB = b.sorted()
    val sumA = sortedA.sum()
    val sumB = sortedB.sum()

    if (sumA == sumB) {
        return 0 // no swap needed
    }

    var currSumA = sumA
    var currSumB = sumB
    var candidatesA = sortedA.toMutableList()
    var candidatesB = sortedB.toMutableList()

    val currDiff = abs(currSumA - currSumB)
    repeat(k) {
        var aToSwap = -1
        var bToSwap = -1
        var newDiff = currDiff
        candidatesA.forEach { a ->
            candidatesB.forEach { b ->
                var newSumA = currSumA - a + b
                var newSumB = currSumB - b + a
                var diff = abs(newSumA - newSumB)

                if (diff < newDiff) {
                    aToSwap = a
                    bToSwap = b
                    newDiff = diff
                }
            }
        }

        if (aToSwap == -1) {
            return@repeat
        }

        candidatesA.remove(aToSwap)
        candidatesB.remove(bToSwap)
        currSumA = currSumA - aToSwap + bToSwap
        currSumB = currSumB - bToSwap + aToSwap
    }



    return abs(currSumA - currSumB)
}

fun main() {
    println(minDiff(listOf(1, 2, 11, 7), listOf(3, 4, 1, 2), 1)) // Expected: 1
    println(minDiff(listOf(1, 1, 1, 1), listOf(1, 1, 1, 1), 2)) // Expected: 0
    println(minDiff(listOf(5), listOf(1), 0))                    // Expected: 4
}
