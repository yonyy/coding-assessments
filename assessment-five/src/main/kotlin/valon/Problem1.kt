package valon

import java.math.BigDecimal

//"Given a list of mortgage payment amounts as strings (e.g. "1500.00", " 2000.50 ", some may be null or blank),
// write a Kotlin function that returns the total as a BigDecimal, ignoring invalid entries."
fun sumPayments(payments: List<String?>): BigDecimal {
    return payments
        .filterNotNull()
        .filter { it.isNotEmpty() }
        .sumOf {
            runCatching {
                it.toBigDecimal()
            }.getOrElse { BigDecimal.ZERO }
        }
}

fun main() {
    val payments = listOf(
        "150.00",
        "50",
        null,
        "",
        "",
        "50.00"
    )

    println(sumPayments(payments))
}