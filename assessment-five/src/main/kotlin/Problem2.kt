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
    var encoded = StringBuilder()

    var index = 0
    while (index < route.length) {
        var count = 0
        val ch = route[index]
        while (index < route.length && route[index] == ch) {
            count++
            index++
       }
        encoded.append(ch)
        encoded.append(if (count > 1) count.toString() else "")
    }

    return encoded.toString()
}

fun decode(encoded: String): String {
    // Iterate over the encoded string
    // if we get a char move pointer to the right
    //  if char is a digit then continue moving pointer building digit => append n times
    //  if char does not match => continue
    val decoded = StringBuilder()
    var index = 0

    while (index < encoded.length) {
        val ch = encoded[index]

        if (ch.isLetter()) {
            if (index + 1 >= encoded.length) {
                decoded.append(ch)
                break
            }
            val nextChar = encoded[++index]
            if (nextChar.isLetter()) {
                decoded.append(ch)
                continue
            } else {
                val digitBuilder = StringBuilder()
                while (index < encoded.length && encoded[index].isDigit()) {
                    digitBuilder.append(encoded[index++])
                }

                repeat(digitBuilder.toString().toInt()) {
                    decoded.append(ch)
                }
            }

        }
    }

    return decoded.toString()
}

fun main() {
    println(encode("NNNEESSWWWWN"))  // Expected: N3E2S2W4N
    println(encode("NSEW"))          // Expected: NSEW
    println(encode("SSSSSS"))        // Expected: S6

    println(decode("N3E2S2W4N"))     // Expected: NNNEESSWWWWN
    println(decode("NSEW"))          // Expected: NSEW
    println(decode("S6"))            // Expected: SSSSSS
}
