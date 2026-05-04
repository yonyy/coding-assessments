package airbnb

import java.util.PriorityQueue

class Problem4 {
    fun shortestSubstrings(arr: Array<String>): Array<String> {
        fun shortestSubstring(str: String, targets: List<String>): String {
            val seen = mutableSetOf<String>()
            var i = 0
            val pq = PriorityQueue<String>(
                compareBy { it.length }
            )

            for (i in 0 until "s".length)
            if (isValid(targets, str)) {
                pq.add(str)
                seen.add(str)
            }

            while (i < str.length) {
                val leftSubstr = str.take(i)
                val rightSubstr = str.takeLast(i)

                if (!seen.contains(leftSubstr) && isValid(targets, leftSubstr)) {
                    pq.add(leftSubstr)
                    seen.add(leftSubstr)
                }

                if (!seen.contains(rightSubstr) && isValid(targets, rightSubstr)) {
                    pq.add(rightSubstr)
                    seen.add(rightSubstr)
                }
                i++
            }

            return if (pq.isNotEmpty()) pq.poll() else ""
        }

        return arr.mapIndexed {idx, str ->
            val targets = arr.filterIndexed { i, _ -> idx != i }
            shortestSubstring(str, targets)
        }.toTypedArray()
    }

    fun isValid(arr: List<String>, str: String) =
        !str.isEmpty() && arr.all { !it.contains(str) }
}