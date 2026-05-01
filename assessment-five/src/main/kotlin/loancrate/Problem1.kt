package loancrate

//  Problem
//  Given a sentence string, return the most frequent word (case-insensitive). If there's a tie, return the lexicographically first one.
//  Input:  "The quick brown fox the fox jumps over the lazy fox"
//  Output: "the"  // appears 3x

fun mostFrequentWord(str: String): String {
    return str.split(" ")
        .groupingBy { it.lowercase() }
        .eachCount()
        .maxWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenBy { it.key }
        )
        .key
}

fun main() {
    println(mostFrequentWord("The quick brown fox the fox jumps over the lazy fox"))
}