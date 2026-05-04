package valon

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// A homeowner makes a monthly payment. It must be applied in this strict order:
// (1) fees & penalties owed, (2) interest owed, (3) principal owed, (4) escrow owed.
// Any remaining amount is a prepayment toward principal. Model this and write an allocation function.

// entities: monthly_payment , monthly_loan_bill (fees, interest, principal, escrow)

// Now we need to track the full payment history for a loan.
// Build a PaymentLedger that records each payment event with a timestamp,
// applies it to update the loan's running balance, and can return a statement —
// the list of events plus the current state."


// Ledger
//  - loan
//  - payment event

//  - access statement
//  - record payment

// Statement
//  - current loan status
//  - payment events
//  - total paid

// LoanStatus
//  - initial balance
//  - current balance

data class Ledger(
    private val loan: Loan
) {
    private var loanHistory = mutableListOf(loan)
    private val paymentEvents: MutableList<PaymentEvent> = mutableListOf()

    fun recordPayment(payment: BigDecimal, loanBill: LoanBill): PaymentEvent { TODO("Not yet implemented") }

    fun getStatement(): Statement { TODO("Not yet implemented") }
}


data class Statement(
    val currentLoanStatus: Loan,
    val paymentEvents: List<PaymentEvent>
)

data class PaymentEvent(
    val id: String,
    val amount: BigDecimal,
    var occurredAt: Instant,
    val allocation: PaymentAllocation
)

data class Loan(
    val feesOwed: BigDecimal,
    val interestOwed: BigDecimal,
    val principalOwed: BigDecimal,
    val escrowOwed: BigDecimal,
) {
    fun balance(): BigDecimal { TODO("Not yet implemented") }

    fun loanAfterPayment(paymentAllocation: PaymentAllocation): Loan { TODO("Not yet implemented") }
}

data class LoanBill(
    val feesOwed: BigDecimal,
    val interestOwed: BigDecimal,
    val principalOwed: BigDecimal,
    val escrowOwed: BigDecimal,
)

data class PaymentAllocation(
    val feesApplied: BigDecimal,
    val interestApplied: BigDecimal,
    val principalApplied: BigDecimal,
    val escrowApplied: BigDecimal,
    val prePayment: BigDecimal,
    val remainingBalance: BigDecimal,
)

fun allocate(payment: BigDecimal, loanBill: LoanBill): PaymentAllocation {
    TODO("Not yet implemented")
}

fun main() {
    // val loan = Loan(
    //     feesOwed = BigDecimal.ZERO,
    //     interestOwed = (500 * 1000 * 0.0375).toBigDecimal(),
    //     principalOwed = (500 * 1000).toBigDecimal(),
    //     escrowOwed = 1000.0.toBigDecimal()
    // )

    // val ledger = Ledger(loan)

    // ledger.recordPayment(1000.toBigDecimal(), LoanBill(
    //     feesOwed = BigDecimal.ZERO,
    //     interestOwed = 800.toBigDecimal(),
    //     principalOwed = 100.toBigDecimal(),
    //     escrowOwed = 100.toBigDecimal()
    // ))

    // println(ledger.getStatement())
}
