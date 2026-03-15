fun finalItem(substitutions: Map<String, String>, start: String): String {
    var to = substitutions[start]
    var end = start
    while(to != null) {
        end = to
        to = substitutions[to]
    }

    return end
}

fun main() {
    val subs1 = mapOf("apple" to "pear", "pear" to "mango", "mango" to "grape")
    println(finalItem(subs1, "apple"))  // Expected: grape

    val subs2 = mapOf("cola" to "pepsi")
    println(finalItem(subs2, "water"))  // Expected: water

    println(finalItem(emptyMap(), "milk")) // Expected: milk
}
