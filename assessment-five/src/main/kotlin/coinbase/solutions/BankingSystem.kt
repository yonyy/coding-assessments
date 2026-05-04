package coinbase.solutions

import java.util.TreeMap

// ─────────────────────────────────────────────────────────────────────────────
// Account
//
// Holds a timestamped balance history as a TreeMap<timestamp, balance>.
// TreeMap gives us O(log H) insertion and O(log H) floor-key lookup for free,
// which is exactly what getBalance / mergeAccounts need.
// ─────────────────────────────────────────────────────────────────────────────
class Account(createdAt: Int) {

    // key   = timestamp of mutation
    // value = balance just after that mutation
    val history: TreeMap<Int, Long> = TreeMap<Int, Long>().also { it[createdAt] = 0L }

    var spend: Long = 0L          // cumulative outbound transfers + scheduled payments

    val currentBalance: Long
        get() = history.lastEntry().value

    /** Record a new balance at the given timestamp. */
    fun record(timestamp: Int, newBalance: Long) {
        history[timestamp] = newBalance
    }

    /**
     * Return the balance at time [t] (i.e. the most recent snapshot at or before [t]).
     * Returns 0 if [t] is before this account was created.
     */
    fun balanceAt(t: Int): Long {
        val entry = history.floorEntry(t) ?: return 0L
        return entry.value
    }

    val createdAt: Int get() = history.firstKey()
}

// ─────────────────────────────────────────────────────────────────────────────
// ScheduledPayment — value object stored in the pending min-heap
// ─────────────────────────────────────────────────────────────────────────────
data class ScheduledPayment(
    val execTime: Int,
    val paymentId: String,
    val accountId: String,
    val amount: Long
) : Comparable<ScheduledPayment> {
    // Natural order: (execTime ASC, paymentId ASC) — paymentId is "paymentN"
    // so lexicographic order here matches numeric order as long as we zero-pad,
    // but since we only need stable ordering (not numeric), lex is fine.
    override fun compareTo(other: ScheduledPayment): Int {
        val cmp = execTime.compareTo(other.execTime)
        return if (cmp != 0) cmp else paymentId.compareTo(other.paymentId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BankingSystem
// ─────────────────────────────────────────────────────────────────────────────
class BankingSystem {

    private val accounts  = mutableMapOf<String, Account>()
    private var paymentCounter = 0

    // PriorityQueue acts as a min-heap on ScheduledPayment's natural order.
    private val pending   = java.util.PriorityQueue<ScheduledPayment>()
    private val cancelled = mutableSetOf<String>()

    // ── internal helpers ────────────────────────────────────────────────────

    private fun nextPaymentId(): String = "payment${++paymentCounter}"

    /**
     * Lazily drain all pending (non-cancelled) payments whose execTime <= [now].
     * Must be the FIRST call in every public method.
     */
    private fun applyPending(now: Int) {
        while (pending.isNotEmpty() && pending.peek().execTime <= now) {
            val p = pending.poll()
            if (p.paymentId in cancelled) continue
            val acc = accounts[p.accountId] ?: continue
            if (acc.currentBalance < p.amount) continue   // silent skip
            val newBal = acc.currentBalance - p.amount
            acc.record(p.execTime, newBal)
            acc.spend += p.amount
        }
    }

    // ── Problem 1 ───────────────────────────────────────────────────────────

    fun createAccount(timestamp: Int, accountId: String): Boolean {
        applyPending(timestamp)
        if (accountId in accounts) return false
        accounts[accountId] = Account(createdAt = timestamp)
        return true
    }

    fun deposit(timestamp: Int, accountId: String, amount: Long): Long? {
        applyPending(timestamp)
        val acc = accounts[accountId] ?: return null
        val newBal = acc.currentBalance + amount
        acc.record(timestamp, newBal)
        return newBal
    }

    fun transfer(timestamp: Int, fromId: String, toId: String, amount: Long): Long? {
        applyPending(timestamp)
        if (fromId == toId) return null
        val sender   = accounts[fromId] ?: return null
        val receiver = accounts[toId]   ?: return null
        if (sender.currentBalance < amount) return null

        val senderNew   = sender.currentBalance   - amount
        val receiverNew = receiver.currentBalance + amount
        sender.record(timestamp, senderNew)
        receiver.record(timestamp, receiverNew)
        sender.spend += amount
        return senderNew
    }

    // ── Problem 2 ───────────────────────────────────────────────────────────

    fun topSpenders(timestamp: Int, n: Int): List<String> {
        applyPending(timestamp)
        return accounts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Account>> { it.value.spend }
                .thenBy { it.key })
            .take(n)
            .map { (id, acc) -> "$id(${acc.spend})" }
    }

    // ── Problem 3 ───────────────────────────────────────────────────────────

    fun schedulePayment(
        timestamp: Int, accountId: String, amount: Long, delay: Int
    ): String? {
        applyPending(timestamp)
        if (accountId !in accounts) return null
        val pid = nextPaymentId()
        pending.add(ScheduledPayment(
            execTime  = timestamp + delay,
            paymentId = pid,
            accountId = accountId,
            amount    = amount
        ))
        return pid
    }

    fun cancelPayment(timestamp: Int, accountId: String, paymentId: String): String? {
        applyPending(timestamp)
        // Find in pending — must belong to accountId and not yet executed
        val entry = pending.firstOrNull { it.paymentId == paymentId } ?: return null
        if (entry.accountId != accountId) return null
        cancelled.add(paymentId)
        return paymentId
    }

    // ── Problem 4 ───────────────────────────────────────────────────────────

    fun getBalance(timestamp: Int, accountId: String, timeAt: Int): Long? {
        applyPending(timestamp)
        val acc = accounts[accountId] ?: return null
        if (timeAt < acc.createdAt) return null
        return acc.balanceAt(timeAt)
    }

    /**
     * Merge [accountId2] into [accountId1].
     *
     * History merge algorithm — two-pointer over both sorted histories:
     *   At every unique timestamp t, combinedBalance(t) = acc1.balanceAt(t) + acc2.balanceAt(t)
     * We walk both TreeMaps in ascending key order, carrying the last-known
     * balance from whichever side hasn't advanced yet.
     */
    fun mergeAccounts(timestamp: Int, accountId1: String, accountId2: String): Boolean {
        applyPending(timestamp)
        if (accountId1 == accountId2) return false
        val acc1 = accounts[accountId1] ?: return false
        val acc2 = accounts[accountId2] ?: return false

        // Build merged history
        val merged = TreeMap<Int, Long>()
        val keys1  = acc1.history.keys.toList()
        val keys2  = acc2.history.keys.toList()
        var i = 0; var j = 0
        var last1 = 0L; var last2 = 0L

        while (i < keys1.size && j < keys2.size) {
            val t1 = keys1[i]; val t2 = keys2[j]
            when {
                t1 < t2 -> { last1 = acc1.history[t1]!!; merged[t1] = last1 + last2; i++ }
                t2 < t1 -> { last2 = acc2.history[t2]!!; merged[t2] = last1 + last2; j++ }
                else    -> {                              // same timestamp
                    last1 = acc1.history[t1]!!
                    last2 = acc2.history[t2]!!
                    merged[t1] = last1 + last2
                    i++; j++
                }
            }
        }
        while (i < keys1.size) {
            val t1 = keys1[i]; last1 = acc1.history[t1]!!; merged[t1] = last1 + last2; i++
        }
        while (j < keys2.size) {
            val t2 = keys2[j]; last2 = acc2.history[t2]!!; merged[t2] = last1 + last2; j++
        }

        // Replace acc1's history with the merged one; carry over combined spend
        acc1.history.clear()
        acc1.history.putAll(merged)
        acc1.spend += acc2.spend

        // Delete acc2; reroute any still-pending payments from acc2 → effectively
        // they will find no account and be silently skipped (correct behaviour).
        accounts.remove(accountId2)
        return true
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tests — mirrors every example from the problem set
// ─────────────────────────────────────────────────────────────────────────────
fun main() {
    var passed = 0
    var failed = 0

    fun <T> check(label: String, got: T, expected: T) {
        if (got == expected) {
            println("  ✓  $label")
            passed++
        } else {
            println("  ✗  $label  →  got=$got  expected=$expected")
            failed++
        }
    }

    // ── Problem 1 ────────────────────────────────────────────────────────────
    println("\n── Problem 1: core operations ──")
    val s1 = BankingSystem()
    check("createAccount acc1",           s1.createAccount(1, "acc1"),              true)
    check("createAccount acc2",           s1.createAccount(2, "acc2"),              true)
    check("createAccount acc1 duplicate", s1.createAccount(2, "acc1"),              false)
    check("deposit acc1 2000",            s1.deposit(3, "acc1", 2000),              2000L)
    check("deposit acc2 500",             s1.deposit(4, "acc2", 500),               500L)
    check("transfer 300",                 s1.transfer(5, "acc1", "acc2", 300),      1700L)
    check("transfer insufficient",        s1.transfer(6, "acc1", "acc2", 5000),     null)
    check("transfer unknown account",     s1.transfer(7, "acc3", "acc1", 100),      null)

    // ── Problem 2 ────────────────────────────────────────────────────────────
    println("\n── Problem 2: top spenders ──")
    // continuing s1 state: acc1.spend=300, acc2.spend=0
    check("deposit acc1 10000",           s1.deposit(8,  "acc1", 10000),            11700L)
    check("transfer acc1→acc2 400",       s1.transfer(9, "acc1", "acc2", 400),      11300L)
    check("transfer acc2→acc1 100",       s1.transfer(10, "acc2", "acc1", 100),     1100L)
    check("topSpenders(2)",
        s1.topSpenders(11, 2),
        listOf("acc1(700)", "acc2(100)")
    )

    // ── Problem 3 ────────────────────────────────────────────────────────────
    println("\n── Problem 3: scheduled payments ──")
    val s3 = BankingSystem()
    s3.createAccount(1, "acc1")
    s3.deposit(2, "acc1", 5000)

    check("schedulePayment p1 (fires t=13)", s3.schedulePayment(3, "acc1", 1000, 10), "payment1")
    check("schedulePayment p2 (fires t=12)", s3.schedulePayment(5, "acc1", 500,  7),  "payment2")

    // deposit at t=15 triggers lazy-apply: p2 at t=12 then p1 at t=13
    // 5000 - 500 - 1000 = 3500; deposit 0 → 3500
    check("deposit t=15 (lazy applies p2+p1)", s3.deposit(15, "acc1", 0), 3500L)
    check("cancel already-executed p1",        s3.cancelPayment(20, "acc1", "payment1"), null)

    check("schedulePayment p3",                s3.schedulePayment(21, "acc1", 200, 5), "payment3")
    check("cancel p3 before firing",           s3.cancelPayment(22, "acc1", "payment3"), "payment3")
    check("deposit t=30 (p3 was cancelled)",   s3.deposit(30, "acc1", 100), 3600L)

    // ── Problem 4 ────────────────────────────────────────────────────────────
    println("\n── Problem 4: historical balance & merge ──")
    val s4 = BankingSystem()
    s4.createAccount(1, "acc1")
    s4.createAccount(2, "acc2")
    s4.deposit(3, "acc1", 1000)
    s4.deposit(4, "acc2", 2000)
    s4.transfer(5, "acc1", "acc2", 500)  // acc1=500, acc2=2500

    check("getBalance acc1 at t=3",  s4.getBalance(6, "acc1", 3),  1000L)
    check("getBalance acc1 at t=5",  s4.getBalance(6, "acc1", 5),  500L)
    check("getBalance acc2 at t=4",  s4.getBalance(6, "acc2", 4),  2000L)
    check("getBalance before create", s4.getBalance(6, "acc1", 0), null)

    check("mergeAccounts acc1←acc2", s4.mergeAccounts(10, "acc1", "acc2"), true)

    // after merge: acc1 at t=3 → acc1(t=3)=1000 + acc2(t=3)=0 = 1000
    check("getBalance acc1 at t=3 post-merge", s4.getBalance(11, "acc1", 3), 1000L)
    // acc1 at t=5 → acc1(t=5)=500 + acc2(t=5)=2500 = 3000
    check("getBalance acc1 at t=5 post-merge", s4.getBalance(11, "acc1", 5), 3000L)
    // acc2 is gone
    check("deposit acc2 after merge", s4.deposit(15, "acc2", 100), null)
    // acc1 current balance = 500 + 2500 = 3000
    check("deposit acc1 after merge", s4.deposit(16, "acc1", 0), 3000L)

    // merge self → false
    check("merge self", s4.mergeAccounts(17, "acc1", "acc1"), false)

    println("\n─────────────────────────────────────")
    println("  $passed passed  |  $failed failed")
    println("─────────────────────────────────────\n")
}