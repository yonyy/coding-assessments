package assessment3

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
    TODO("Not yet implemented")
}

fun String.isPalindrome(): Boolean {
    // TODO: helper — compare characters from both ends toward the center
    TODO("Not yet implemented")
}

fun main() {
    println(palindromicPrefix("aaaba"))   // Expected: "ba"
    println(palindromicPrefix("racecar")) // Expected: ""
    println(palindromicPrefix("abcde"))   // Expected: "abcde"
    println(palindromicPrefix("aabaa"))   // Expected: ""
    println(palindromicPrefix("aab"))     // Expected: "b"
}
