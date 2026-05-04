package coinbase

import java.util.PriorityQueue
import java.util.TreeMap

//    Banking system — core account operations
//    Description
//    Design a simple in-memory banking system. Each account is identified by a unique string accountId and holds an integer balance (in cents). Implement the three operations below. All operations are timestamped; timestamps are strictly increasing integers.
//
//    Part 1:
//    Operations to implement
//      createAccount(timestamp, accountId): Boolean -
//          Creates a new account with zero balance. Returns true on success, false if the account already
//          exists.
//      deposit(timestamp, accountId, amount): Int? -
//          Adds amount (positive integer) to the account's balance. Returns the new balance,
//          or null if the account doesn't exist.
//      transfer(timestamp, fromAccountId, toAccountId, amount): Int? -
//          Transfers amount from one account to another. Returns the balance of fromAccountId after the
//          transfer. Returns null if either account doesn't exist, the accounts are the same, or the sender
//          has insufficient funds.
//      Part 2:
//      Extend the system from Problem 1. All previous operations still apply with no regressions.
//      Add one new query that ranks accounts by total outbound spend (the sum of all amounts transferred
//      out of an account).
//
//      topSpenders(timestamp, n): List<String> -
//          Returns the top n accounts ranked by cumulative outbound transfer amount (highest first).
//          Ties are broken by accountId in ascending lexicographic order. Each entry is formatted as
//          "accountId(amount)". If fewer than n accounts exist, return all accounts that have ever existed.
//      Part 3:
//      Extend the system from Problems 1–2. Add the ability to schedule a future deduction from an
//      account after a delay, and to cancel it before it fires. Scheduled payments must be applied
//      lazily — only when a later operation's timestamp reaches or passes the scheduled execution time.
//
//      schedulePayment(timestamp, accountId, amount, delay) -
//          Schedules a deduction of amount from accountId at time timestamp + delay.
//          Returns a unique paymentId string. Returns null if the account doesn't exist.
//          The deduction is guaranteed to have sufficient funds when it fires
//          (you may assume this for scheduling, but validate at execution time —
//          return null silently on execution if funds are insufficient).
//      cancelPayment(timestamp, accountId, paymentId) -
//          Cancels a scheduled payment before it executes. Returns the paymentId on success, or
//          null if the payment doesn't exist, has already executed, or doesn't belong to accountId.
//    Part 4:
//    Extend the system from Problems 1–3. Add point-in-time balance queries and account merging with full
//    history preservation. This problem requires careful bookkeeping of every balance mutation and its
//    timestamp.
//
//    All test cases from Problems 1–3 must still pass.
//    New operations:
//
//    getBalance(timestamp, accountId, timeAt): Int? -
//          Returns the balance of accountId at exactly time timeAt (i.e., just after all operations at
//          timeAt have completed). Returns null if the account didn't exist at timeAt. timeAt may be
//          any past timestamp including the account creation timestamp.
//
//    mergeAccounts(timestamp, accountId1, accountId2): Boolean -
//          Merges accountId2 into accountId1. After the merge: accountId1 receives the current balance
//          of accountId2; the full transaction history of accountId2 is folded into accountId1's timeline
//          so that getBalance on accountId1 at any past time returns the combined balance of both accounts
//          at that time. accountId2 is deleted and subsequent operations on it return null. Returns true
//          on success, false if either account doesn't exist or they are the same.
data class ScheduledPayment(
    val accountId: String,
    val paymentId: String,
    val amount: Int,
    val executesAt: Long
)

data class Account(
    val id: String,
    val createdAt: Long
) {
    val balanceHistory = TreeMap<Long, Int>().apply {
        put(createdAt, 0)
    }

    val currentBalance: Int = balanceHistory.lastEntry().value
    var totalSpent = 0

    fun withdraw(timestamp: Long, amount: Int) { TODO("Not yet implemented") }
    fun deposit(timestamp: Long, amount: Int) { TODO("Not yet implemented") }
    fun balanceAt(timestamp: Long): Int { TODO("Not yet implemented") }
}

class BankingSystem {
    val accounts = mutableMapOf<String, Account>()
    val pendingPayments = PriorityQueue<ScheduledPayment>(
        compareBy { it.executesAt }
    )
    var paymentCounter = 0
    val cancelledPayments = mutableSetOf<String>()

    fun getNextPaymentId(): String = "payment-${++paymentCounter}"

    // Part 1

    fun createAccount(timestamp: Long, accountId: String): Boolean { TODO("Not yet implemented") }

    fun deposit(timestamp: Long, accountId: String, amount: Int): Int? { TODO("Not yet implemented") }

    fun transfer(timestamp: Long, fromAccountId: String, toAccountId: String, amount: Int): Int? { TODO("Not yet implemented") }

    // Part 2

    fun topSpenders(timestamp: Long, n: Int): List<String> { TODO("Not yet implemented") }

    // Part 3

    fun schedulePayment(timestamp: Long, accountId: String, amount: Int, delay: Long): String? { TODO("Not yet implemented") }

    fun cancelPayment(timestamp: Long, accountId: String, paymentId: String): String? { TODO("Not yet implemented") }

    private fun applyAllPendingPayments(timestamp: Long) { TODO("Not yet implemented") }

    // Part 4

    fun getBalance(timestamp: Long, accountId: String, timeAt: Long): Int? { TODO("Not yet implemented") }

    fun mergeAccounts(timestamp: Long, toAccountId: String, fromAccountId: String): Boolean { TODO("Not yet implemented") }
}


fun main() {
    // val bank = BankingSystem()
    // println(bank.createAccount(1, "acc1"))   // true
    // println(bank.createAccount(2, "acc2"))   // true
    // println(bank.createAccount(2, "acc1"))   // false   // duplicate
    // println(bank.deposit(3, "acc1", 2000))   // 2000
    // println(bank.deposit(4, "acc2", 500))    // 500
    // println(bank.transfer(5, "acc1", "acc2", 300)) // 1700
    // println(bank.transfer(6, "acc1", "acc2", 5000)) // null  // insufficient funds
    // println(bank.transfer(7, "acc3", "acc1", 100))  // null  // non-existent account
    //
    // println(bank.deposit(8, "acc1", 10000))   // → 11700
    // println(bank.transfer(9, "acc1", "acc2", 400))  // → 11300
    // println(bank.transfer(10, "acc2", "acc1", 100)) // → 900
    // // acc1 has spent 300 + 400 = 700, acc2 has spent 100
    // println(bank.topSpenders(11, 2)) // → ["acc1(700)", "acc2(100)"]

    // println(bank.createAccount(1, "acc1"))        // → true
    // println(bank.deposit(2, "acc1", 5000))        // → 5000
    // println(bank.schedulePayment(3, "acc1", 1000, 10))  // → "payment1"
    // // payment1 fires at time 13
    // println(bank.schedulePayment(5, "acc1", 500, 7))    // → "payment2"
    // // payment2 fires at time 12
    // println(bank.deposit(15, "acc1", 0))
    // // Before deposit at t=15, apply payment2 (t=12) then payment1 (t=13)
    // // acc1: 5000 - 500 - 1000 = 3500, then deposit 0 = 3500
    // println(bank.deposit(15, "acc1", 0))          // → 3500
    // println(bank.cancelPayment(20, "acc1", "payment1")) // → null  // already executed

    // val payment3Id = bank.schedulePayment(21, "acc1", 200, 5)
    // println(payment3Id)  // → "payment3"
    // println(bank.cancelPayment(22, "acc1", payment3Id!!)) // → "payment3"
    // println(bank.deposit(30, "acc1", 100))        // → 3600  // payment3 was cancelled
}
