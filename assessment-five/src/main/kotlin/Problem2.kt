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
    var currentDir = ""
    var currCount = 0
    var encodedString = ""

    route.forEachIndexed { index, it ->
        if (it.toString() == currentDir) {
            currCount++
        } else {
            val numSuffix = if (currCount > 1) currCount.toString() else ""
            encodedString += "$currentDir$numSuffix"
            currentDir = it.toString()
            currCount = 1
        }

        if (index == route.length - 1) {
            val numSuffix = if (currCount > 1) currCount.toString() else ""
            encodedString += "$currentDir$numSuffix"
        }
    }

    return encodedString
}

// Alternative
//fun encode(route: String): String {
//    val sb = StringBuilder()
//    var i = 0
//    while (i < route.length) {
//        val ch = route[i]
//        var count = 1
//        // count how many consecutive identical chars follow
//        while (i + count < route.length && route[i + count] == ch) count++
//        sb.append(ch)
//        if (count > 1) sb.append(count)   // omit count when run = 1
//        i += count                         // jump past the entire run
//    }
//    return sb.toString()
//}

fun decode(encoded: String): String {
    // TODO: parse char optionally followed by digits, expand
    var decodedString = ""
    var currentDir = ""
    encoded.forEachIndexed { index, ch ->
        if (ch.isDigit()) {
            decodedString += currentDir.repeat(ch.digitToInt() - 1)
            currentDir = ""
        } else {
            currentDir = ch.toString()
            decodedString += currentDir
        }


    }

    return decodedString
}

// Alternative
// fun decode(encoded: String): String {
//    val sb = StringBuilder()
//    var i = 0
//    while (i < encoded.length) {
//        val ch = encoded[i]
//        // scan forward to collect all digit characters after this letter
//        var j = i + 1
//        while (j < encoded.length && encoded[j].isDigit()) j++
//        // substring(i+1, j) is the full number — empty means count of 1
//        val count = if (j > i + 1) encoded.substring(i + 1, j).toInt() else 1
//        sb.append(ch.toString().repeat(count))
//        i = j   // advance past the letter AND its digits
//    }
//    return sb.toString()
// }

fun main() {
    println(encode("NNNEESSWWWWN"))  // Expected: N3E2S2W4N
    println(encode("NSEW"))          // Expected: NSEW
    println(encode("SSSSSS"))        // Expected: S6

    println(decode("N3E2S2W4N"))     // Expected: NNNEESSWWWWN
    println(decode("NSEW"))          // Expected: NSEW
    println(decode("S6"))            // Expected: SSSSSS
}
