/*
 * Question 1: String Manipulation (Easy)
 * Target Time: 10–15 Minutes
 * Tags: Strings, Two Pointers
 *
 * Task: The "Palindromic Prefix"
 *
 * Given a string s, find the longest prefix that is a palindrome.
 * Return the remaining part of the string after removing that prefix.
 * If no prefix (longer than 1 character) is a palindrome, return the original string.
 *
 * Examples:
 *   s = "aaaba" → Prefix "aaa" is a palindrome. Output: "ba"
 *   s = "racecar" → Entire string is a palindrome. Output: ""
 *
 * Constraints:
 *   - 1 ≤ s.length ≤ 10^5
 *   - s contains only lowercase English letters
 *
 * Strategy: Check prefixes from longest to shortest for palindrome property.
 */

fun palindromicPrefix(s: String): String {
    // TODO: iterate from the full string down to length 2,
    // return s.substring(prefix.length) for the first palindromic prefix found.
    // If none found (length > 1), return the original string.
    for (i in s.length downTo 2) {
        val prefix = s.substring(0, i)
        if (prefix.isPalindrome()) {
            //println("prefix $prefix is a Palindrome")
            return s.substring(prefix.length)
        }
        //println("prefix $prefix is not a Palindrome")
    }

    return s
}

fun String.isPalindrome(): Boolean {
    // TODO: helper — compare characters from both ends toward the center
    for (i in 0..this.length / 2) {
        if (this[i] != this[this.length - i - 1]) {
            return false
        }
    }

    return true
}

fun main() {
    println(palindromicPrefix("aaaba"))   // Expected: "ba"
    println(palindromicPrefix("racecar")) // Expected: ""
    println(palindromicPrefix("abcde"))   // Expected: "abcde" (no prefix > 1 char is a palindrome)
    println(palindromicPrefix("aabaa"))   // Expected: "" (whole string is a palindrome)
    println(palindromicPrefix("aab"))     // Expected: "b" (prefix "aa" is a palindrome)
}
