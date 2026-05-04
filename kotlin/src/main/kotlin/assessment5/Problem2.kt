package assessment5

/*
 * Problem 2 — Delivery Route Compression
 * Difficulty: Medium | Estimated Time: ~20 min | Tags: Strings, Run-Length Encoding, Two Pointers
 *
 * Description:
 * Instacart encodes delivery routes as a string of direction characters (N, S, E, W).
 * To compress the route for transmission, apply run-length encoding: replace consecutive
 * repeated characters with the character followed by its count. If a character appears
 * only once consecutively, omit the count.
 *
 * Implement both encode and decode.
 *
 * Examples:
 *   Encode:
 *     "NNNEESSWWWWN"  → "N3E2S2W4N"
 *     "NSEW"          → "NSEW"   (no repeats — no numbers added)
 *
 *   Decode:
 *     "N3E2S2W4N"     → "NNNEESSWWWWN"
 *     "NSEW"          → "NSEW"
 *
 * Constraints:
 *   - 1 ≤ route.length ≤ 10^4
 *   - Encode input contains only N, S, E, W
 *   - Decode input is always a valid encoded string
 *   - Counts in encoded string are always ≥ 2 (single chars have no number)
 */

fun encode(route: String): String {
    // TODO: run-length encode — omit count when run length is 1
    TODO("Not yet implemented")
}

fun decode(encoded: String): String {
    TODO("Not yet implemented")
}

fun main() {
    // println(encode("NNNEESSWWWWN"))  // Expected: N3E2S2W4N
    // println(encode("NSEW"))          // Expected: NSEW
    // println(encode("SSSSSS"))        // Expected: S6

    // println(decode("N3E2S2W4N"))     // Expected: NNNEESSWWWWN
    // println(decode("NSEW"))          // Expected: NSEW
    // println(decode("S6"))            // Expected: SSSSSS
}
