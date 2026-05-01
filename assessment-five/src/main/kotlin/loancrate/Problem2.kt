package loancrate

//  Problem
//  Given a mutable list of integers, remove all duplicates in-place (preserve first occurrence order), then return the count of elements that appeared more than once.
//  Input:  [4, 3, 2, 4, 1, 3, 5]
//  Output: list becomes [4, 3, 2, 1, 5], return 2  (4 and 3 were duplicates)

fun removeDuplicates(nums: MutableList<Int>): Int {
    val seen = mutableSetOf<Int>()
    val dupes = mutableSetOf<Int>()
    val iterator = nums.iterator()


    while (iterator.hasNext()) {
        val next = iterator.next()
        if (!seen.contains(next)) {
            seen.add(next)
        } else {
            iterator.remove()
            dupes.add(next)
        }
    }

    return dupes.size
}

fun main() {
    println(removeDuplicates(mutableListOf(4, 3, 2, 4, 1, 3, 5)))
}