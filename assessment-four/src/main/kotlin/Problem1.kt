/*
 * Problem 1 — Promo Code Validator
 * Difficulty: Easy | Estimated Time: ~15 min | Tags: Strings, Hash Map
 *
 * Description:
 * Instacart issues promo codes to customers. A promo code is valid if:
 *   1. It is exactly 8 characters long.
 *   2. It contains only alphanumeric characters (letters and digits).
 *   3. It is not already in the used set.
 *
 * Given a list of promo codes and a set of already-used codes, return the list
 * of valid unused codes in the order they appear. Treat all returned codes as
 * used — so duplicates in the input are only returned once.
 *
 * Example:
 *   codes = ["SAVE20AB", "HI", "ABCD1234", "SAVE20AB", "abc!1234", "FREESHIP"]
 *   used  = {"FREESHIP"}
 *   Output: ["SAVE20AB", "ABCD1234"]
 *
 *   - "SAVE20AB" → valid, not used ✓
 *   - "HI"       → too short ✗
 *   - "ABCD1234" → valid ✓
 *   - "SAVE20AB" → duplicate of already-returned code ✗
 *   - "abc!1234" → contains '!' ✗
 *   - "FREESHIP" → in used set ✗
 *
 * Constraints:
 *   - 1 ≤ codes.size ≤ 10^4
 *   - 1 ≤ codes[i].length ≤ 20
 *   - Codes are case-sensitive
 *
 * Strategy:
 *   Build a mutable seen set (seeded with `used`). Iterate codes: add to result
 *   if length == 8, all chars alphanumeric, and not in seen. Add each accepted
 *   code to seen immediately to handle duplicates.
 */

fun validCodes(codes: List<String>, used: Set<String>): List<String> {
    // TODO: filter by length == 8, alphanumeric only, not in used, not already returned
    return codes
        .filter { it.isValidPromoCode() && !used.contains(it) }
        .distinct()
}

fun String.isValidPromoCode(): Boolean {
    val length = 8
    val alphanumericPattern = "[A-Za-z0-9]+".toRegex()

    return this.length == length && alphanumericPattern.matches(this)
}

fun main() {
    val codes = listOf("SAVE20AB", "HI", "ABCD1234", "SAVE20AB", "abc!1234", "FREESHIP")
    val used  = setOf("FREESHIP")
    println(validCodes(codes, used)) // Expected: [SAVE20AB, ABCD1234]

    println(validCodes(listOf("AAAAAAAA", "AAAAAAAA"), emptySet())) // Expected: [AAAAAAAA]
    println(validCodes(emptyList(), emptySet()))                    // Expected: []
}
