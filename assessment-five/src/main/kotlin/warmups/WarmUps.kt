package warmups

// ## Problem 1 — 3 minutes

// Given a list of strings, return a map of each unique character to the number of strings it appears in (not total occurrences — distinct strings).

// ```kotlin
// fun charToStringCount(strings: List<String>): Map<Char, Int>
// ```

// **Examples:**
// ```
// ["abc", "bcd", "cde"] → {a=1, b=2, c=3, d=2, e=1}
// ["aab", "b"]          → {a=1, b=2}   // 'a' appears in 1 string, not 2
// []                    → {}
// ```

fun charToStringCount(strings: List<String>): Map<Char, Int> {
    val count = mutableMapOf<Char, Int>()

    strings.forEach {
        val uniqueChars = it.toSet()
        uniqueChars.forEach { char ->
            count[char] = count.getOrDefault(char, 0) + 1
        }
    }

    return count
}

// ## Problem 2 — 3 minutes

// Given a string, return the first non-repeating character. Return `null` if none exists.

// ```kotlin
// fun firstUniqueChar(s: String): Char?
// ```

// **Examples:**
// ```
// "leetcode"   → 'l'
// "aabb"       → null
// "aabbc"      → 'c'
// ""           → null
// ```

fun firstUniqueChars(s: String): Char? {
    val map = LinkedHashMap<Char, Int>()
    for (char in s) {
        map[char] = (map[char] ?: 0) + 1
    }

    return map.entries.firstOrNull { it.value == 1 }?.key
}

// ## Problem 3 — 3 minutes

// Given a list of integers and a target sum, return `true` if any two distinct elements sum to the target.

// ```kotlin
// fun hasPairWithSum(nums: List<Int>, target: Int): Boolean
// ```

// **Examples:**
// ```
// [1, 2, 3, 4], target=5  → true   (1+4, 2+3)
// [1, 2, 3, 4], target=8  → false
// [3, 3], target=6        → true
// [], target=0            → false
// ```

fun hasPairWithSum(nums: List<Int>, target: Int): Boolean {
    val seen = mutableSetOf<Int>()
    for (num in nums) {
        if (seen.contains(target - num)) {
            return true
        }

        seen.add(num)
    }

    return false
}