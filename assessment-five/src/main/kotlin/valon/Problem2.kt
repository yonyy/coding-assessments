package valon

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.assert

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

    fun recordPayment(payment: BigDecimal, loanBill: LoanBill): PaymentEvent {
        val occurredAt = Instant.now()
        val paymentAllocation = allocate(payment, loanBill)
        val newLoanState = loanHistory.last().loanAfterPayment(paymentAllocation)
        val paymentEvent = PaymentEvent(
            id = UUID.randomUUID().toString(),
            occurredAt = occurredAt,
            allocation = paymentAllocation,
            amount = payment,
        )
        paymentEvents.add(paymentEvent)
        loanHistory.add(newLoanState)
        return paymentEvent
    }

    fun getStatement(): Statement {
        return Statement(
            currentLoanStatus = loanHistory.last(),
            paymentEvents = paymentEvents,
        )
    }
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
    fun balance(): BigDecimal {
        return feesOwed + interestOwed + principalOwed + escrowOwed
    }

    fun loanAfterPayment(paymentAllocation: PaymentAllocation): Loan {
        assert(paymentAllocation.feesApplied >= BigDecimal.ZERO)
        assert(paymentAllocation.interestApplied >= BigDecimal.ZERO)
        assert(paymentAllocation.principalApplied >= BigDecimal.ZERO)
        assert(paymentAllocation.escrowApplied >= BigDecimal.ZERO)

        return Loan(
            feesOwed = feesOwed - paymentAllocation.feesApplied,
            interestOwed = interestOwed - paymentAllocation.interestApplied,
            principalOwed = principalOwed - paymentAllocation.principalApplied,
            escrowOwed = escrowOwed - paymentAllocation.escrowApplied,
        )
    }
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
    var remainingBalance = payment
    fun apply(payment: BigDecimal, dueAmount: BigDecimal): BigDecimal {
        val applied = minOf(payment, dueAmount)
        remainingBalance -= applied
        return applied
    }

    val feesApplied = apply(remainingBalance, loanBill.feesOwed)
    val interestApplied = apply(remainingBalance, loanBill.interestOwed)
    val principalApplied = apply(remainingBalance, loanBill.principalOwed)
    val escrowApplied = apply(remainingBalance, loanBill.escrowOwed)

    return PaymentAllocation(
        feesApplied = feesApplied,
        interestApplied = interestApplied,
        principalApplied = principalApplied,
        escrowApplied = escrowApplied,
        prePayment = if (remainingBalance > BigDecimal.ZERO) remainingBalance else BigDecimal.ZERO,
        remainingBalance = if (remainingBalance <= BigDecimal.ZERO) BigDecimal.ZERO else remainingBalance
    )
}

fun main() {
    val loan = Loan(
        feesOwed = BigDecimal.ZERO,
        interestOwed = (500 * 1000 * 0.0375).toBigDecimal(),
        principalOwed = (500 * 1000).toBigDecimal(),
        escrowOwed = 1000.0.toBigDecimal()
    )

    val ledger = Ledger(loan)

    ledger.recordPayment(1000.toBigDecimal(), LoanBill(
        feesOwed = BigDecimal.ZERO,
        interestOwed = 800.toBigDecimal(),
        principalOwed = 100.toBigDecimal(),
        escrowOwed = 100.toBigDecimal()
    ))

    println(ledger.getStatement())
}