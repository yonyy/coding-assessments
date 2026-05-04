package coinbase.solutions

import java.util.PriorityQueue
import java.util.TreeMap

// ─────────────────────────────────────────────────────────────────────────────
// Domain models
// ─────────────────────────────────────────────────────────────────────────────

enum class OrderType { BUY, SELL }
enum class OrderStatus { OPEN, FILLED, CANCELLED }

data class Order(
    val orderId: String,
    val type: OrderType,
    val price: Int,
    var quantity: Int,        // mutable — partial fills decrement this
    var status: OrderStatus,
    val placedAt: Long,       // original placement timestamp, used for price-time priority
    val expiresAt: Long?,     // null = no TTL
    val userId: String?       // null = anonymous; excluded from getTopTraders
)

data class Trade(
    val buyOrderId: String,
    val sellOrderId: String,
    val price: Int,
    val quantity: Int,
    val executedAt: Long
)

// ─────────────────────────────────────────────────────────────────────────────
// OrderBook — all four tasks in a single class
// ─────────────────────────────────────────────────────────────────────────────

class OrderBook {

    // ── Task 1: primary order store ───────────────────────────────────────────
    private val orders = HashMap<String, Order>()

    // ── Task 2: matching heaps ────────────────────────────────────────────────
    // BUY heap: highest price first; ties resolved by earliest placedAt (FIFO)
    private val buyHeap = PriorityQueue(
        compareByDescending<Order> { it.price }.thenBy { it.placedAt }
    )
    // SELL heap: lowest price first; ties resolved by earliest placedAt
    private val sellHeap = PriorityQueue(
        compareBy<Order> { it.price }.thenBy { it.placedAt }
    )

    // ── Task 3: expiry heap ───────────────────────────────────────────────────
    // Min-heap on (expiresAt, orderId) — drained lazily before every operation
    private val expiryHeap = PriorityQueue<Pair<Long, String>>(compareBy { it.first })

    // ── Task 4: trade log and analytics structures ────────────────────────────
    // Appended in strictly increasing executedAt order → supports binary search
    private val tradeLog = mutableListOf<Trade>()

    // Price-level depth maps: price → total open quantity on that side.
    // TreeMap gives O(log n) lookup, useful for getPriceLevel.
    private val buyLevels  = TreeMap<Int, Int>()
    private val sellLevels = TreeMap<Int, Int>()

    // Cumulative traded volume per named user (both buy and sell sides count)
    private val userVolume = HashMap<String, Long>()

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Expire all pending orders whose expiresAt <= timestamp.
     * Must be called at the top of every public method — this is the lazy TTL
     * flush that mirrors the KV-store TTL pattern.
     */
    private fun flushExpired(timestamp: Long) {
        while (expiryHeap.isNotEmpty() && expiryHeap.peek().first <= timestamp) {
            val (_, oid) = expiryHeap.poll()
            val order = orders[oid] ?: continue
            if (order.status == OrderStatus.OPEN) {
                order.status = OrderStatus.CANCELLED
                adjustLevel(order.type, order.price, -order.quantity)
            }
        }
    }

    /** Add delta (positive or negative) to the price level for one side. */
    private fun adjustLevel(type: OrderType, price: Int, delta: Int) {
        val map = if (type == OrderType.BUY) buyLevels else sellLevels
        val next = map.getOrDefault(price, 0) + delta
        if (next <= 0) map.remove(price) else map[price] = next
    }

    private fun Order.isInactive() = status != OrderStatus.OPEN

    // ─────────────────────────────────────────────────────────────────────────
    // Task 1: placeOrder / cancelOrder / getOrder
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Place a new order.
     *
     * @param ttl    optional time-to-live in milliseconds; order expires at timestamp+ttl
     * @param userId optional owner identifier for analytics
     * @return true if placed, false if orderId already exists
     */
    fun placeOrder(
        timestamp: Long,
        orderId: String,
        type: OrderType,
        price: Int,
        quantity: Int,
        ttl: Long? = null,
        userId: String? = null
    ): Boolean {
        flushExpired(timestamp)
        if (orders.containsKey(orderId)) return false

        val expiresAt = ttl?.let { timestamp + it }
        val order = Order(
            orderId   = orderId,
            type      = type,
            price     = price,
            quantity  = quantity,
            status    = OrderStatus.OPEN,
            placedAt  = timestamp,
            expiresAt = expiresAt,
            userId    = userId
        )

        orders[orderId] = order
        if (type == OrderType.BUY) buyHeap.add(order) else sellHeap.add(order)
        adjustLevel(type, price, quantity)
        expiresAt?.let { expiryHeap.add(Pair(it, orderId)) }

        return true
    }

    /**
     * Cancel an open order.
     * @return true if cancelled, false if not found or already not OPEN
     */
    fun cancelOrder(timestamp: Long, orderId: String): Boolean {
        flushExpired(timestamp)
        val order = orders[orderId] ?: return false
        if (order.status != OrderStatus.OPEN) return false

        order.status = OrderStatus.CANCELLED
        adjustLevel(order.type, order.price, -order.quantity)
        return true
    }

    /** @return the Order if it exists (any status), null otherwise */
    fun getOrder(timestamp: Long, orderId: String): Order? {
        flushExpired(timestamp)
        return orders[orderId]
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 2: matchOrders
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Match all crossable OPEN orders using price-time priority.
     *
     * Matching rules:
     *   - Highest BUY price vs lowest SELL price; match when BUY >= SELL.
     *   - Trade price = SELL price (maker pricing).
     *   - Partial fills allowed; residual order stays OPEN with reduced quantity.
     *   - Heap entries for cancelled/filled orders are skipped lazily (lazy deletion).
     *
     * @return list of Trade objects executed during this call, in execution order
     */
    fun matchOrders(timestamp: Long): List<Trade> {
        flushExpired(timestamp)
        val executed = mutableListOf<Trade>()

        // Advance past any stale heap entries (cancelled/filled since last match)
        fun peekActiveBuy(): Order? {
            while (buyHeap.isNotEmpty() && buyHeap.peek().isInactive()) buyHeap.poll()
            return buyHeap.peek()
        }
        fun peekActiveSell(): Order? {
            while (sellHeap.isNotEmpty() && sellHeap.peek().isInactive()) sellHeap.poll()
            return sellHeap.peek()
        }

        while (true) {
            val buy  = peekActiveBuy()  ?: break
            val sell = peekActiveSell() ?: break
            if (buy.price < sell.price) break   // no more crossable pairs

            // Remove from heaps — we will re-add if partially filled
            buyHeap.poll()
            sellHeap.poll()

            val tradedQty  = minOf(buy.quantity, sell.quantity)
            val tradePrice = sell.price  // maker pricing

            val trade = Trade(
                buyOrderId  = buy.orderId,
                sellOrderId = sell.orderId,
                price       = tradePrice,
                quantity    = tradedQty,
                executedAt  = timestamp
            )
            executed.add(trade)
            tradeLog.add(trade)

            // Accumulate user volumes (both sides)
            buy.userId?.let  { userVolume[it] = (userVolume[it] ?: 0L) + tradedQty }
            sell.userId?.let { userVolume[it] = (userVolume[it] ?: 0L) + tradedQty }

            // Reduce quantities and update price-level depth
            buy.quantity  -= tradedQty
            sell.quantity -= tradedQty
            adjustLevel(OrderType.BUY,  buy.price,  -tradedQty)
            adjustLevel(OrderType.SELL, sell.price, -tradedQty)

            // Update statuses; re-queue partial fills
            if (buy.quantity == 0) {
                buy.status = OrderStatus.FILLED
            } else {
                buyHeap.add(buy)
            }
            if (sell.quantity == 0) {
                sell.status = OrderStatus.FILLED
            } else {
                sellHeap.add(sell)
            }
        }

        return executed
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 3: getOpenOrderCount
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Count OPEN orders after flushing expired ones at the given timestamp.
     * O(n) over total orders — acceptable given assessment constraints.
     */
    fun getOpenOrderCount(timestamp: Long): Int {
        flushExpired(timestamp)
        return orders.values.count { it.status == OrderStatus.OPEN }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task 4: getVWAP / getTopTraders / getPriceLevel
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Volume-Weighted Average Price over trades in (timestamp - windowMs, timestamp].
     *
     * Uses binary search on tradeLog (sorted by executedAt) to find the window
     * boundary in O(log n), then O(k) to sum notional and quantity.
     *
     * VWAP = Σ(price_i * qty_i) / Σ(qty_i)
     *
     * @return VWAP as a Double, or null if no trades exist in the window
     */
    fun getVWAP(timestamp: Long, windowMs: Long): Double? {
        flushExpired(timestamp)
        val windowStart = timestamp - windowMs   // exclusive lower bound

        // Binary search for first trade with executedAt > windowStart
        var lo = 0; var hi = tradeLog.size
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (tradeLog[mid].executedAt <= windowStart) lo = mid + 1 else hi = mid
        }
        val fromIdx = lo
        if (fromIdx >= tradeLog.size) return null

        var notional = 0L
        var totalQty = 0L
        for (i in fromIdx until tradeLog.size) {
            val t = tradeLog[i]
            if (t.executedAt > timestamp) break
            notional += t.price.toLong() * t.quantity
            totalQty += t.quantity
        }

        return if (totalQty == 0L) null else notional.toDouble() / totalQty
    }

    /**
     * Top n users by total traded volume (buy + sell sides both count).
     * Sorted descending by volume; ties broken alphabetically ascending by userId.
     * Anonymous orders (userId == null) are excluded.
     *
     * @return list of strings in format "userId(volume)"
     */
    fun getTopTraders(timestamp: Long, n: Int): List<String> {
        flushExpired(timestamp)
        return userVolume.entries
            .sortedWith(compareByDescending<Map.Entry<String, Long>> { it.value }.thenBy { it.key })
            .take(n)
            .map { "${it.key}(${it.value})" }
    }

    /**
     * Total open quantity at a specific price on one side of the book.
     * O(log n) via TreeMap lookup.
     *
     * @return total open quantity, or 0 if no orders exist at that price
     */
    fun getPriceLevel(timestamp: Long, type: OrderType, price: Int): Int {
        flushExpired(timestamp)
        val map = if (type == OrderType.BUY) buyLevels else sellLevels
        return map.getOrDefault(price, 0)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Test suite — run with: kotlinc OrderBook.kt -include-runtime -d ob.jar && java -jar ob.jar
// ─────────────────────────────────────────────────────────────────────────────

fun check(condition: Boolean, msg: String) {
    if (!condition) throw AssertionError("FAIL: $msg")
    println("PASS: $msg")
}

fun main() {

    // ── Task 1: basic CRUD ────────────────────────────────────────────────────
    run {
        val ob = OrderBook()
        check(ob.placeOrder(1, "o1", OrderType.BUY,  100, 10), "placeOrder new BUY")
        check(ob.placeOrder(2, "o2", OrderType.SELL, 105,  5), "placeOrder new SELL")
        check(!ob.placeOrder(3, "o1", OrderType.BUY, 100, 10), "placeOrder duplicate → false")
        check(ob.cancelOrder(4, "o2"),  "cancelOrder existing open order")
        check(!ob.cancelOrder(5, "o2"), "cancelOrder already cancelled → false")
        check(!ob.cancelOrder(6, "o9"), "cancelOrder nonexistent → false")

        val o1 = ob.getOrder(7, "o1")!!
        check(o1.status   == OrderStatus.OPEN, "getOrder: status OPEN")
        check(o1.price    == 100,              "getOrder: price correct")
        check(o1.quantity == 10,               "getOrder: quantity correct")

        check(ob.getOrder(8, "o2")!!.status == OrderStatus.CANCELLED, "getOrder: CANCELLED status")
        check(ob.getOrder(9, "o3") == null, "getOrder: nonexistent → null")
    }

    // ── Task 2: full fill ─────────────────────────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY,  102, 5)
        ob.placeOrder(2, "b2", OrderType.BUY,  100, 10)
        ob.placeOrder(3, "s1", OrderType.SELL, 101,  3)
        ob.placeOrder(4, "s2", OrderType.SELL, 100,  8)

        val trades = ob.matchOrders(5)

        // b1 (highest buy) matches s2 (lowest sell) for 5 units
        check(trades.any { it.buyOrderId == "b1" && it.sellOrderId == "s2" && it.quantity == 5 },
            "b1 fully fills against s2")
        // b2 takes s2's remaining 3 units, then s1's 3 units
        check(trades.any { it.buyOrderId == "b2" && it.sellOrderId == "s2" && it.quantity == 3 },
            "b2 partially fills against s2 remainder")
        check(trades.any { it.buyOrderId == "b2" && it.sellOrderId == "s1" && it.quantity == 3 },
            "b2 fills against s1")

        check(ob.getOrder(6, "b1")!!.status   == OrderStatus.FILLED, "b1 FILLED")
        check(ob.getOrder(7, "s2")!!.status   == OrderStatus.FILLED, "s2 FILLED")
        check(ob.getOrder(8, "s1")!!.status   == OrderStatus.FILLED, "s1 FILLED")
        check(ob.getOrder(9, "b2")!!.status   == OrderStatus.OPEN,   "b2 still OPEN (partial)")
        check(ob.getOrder(9, "b2")!!.quantity == 4,                  "b2 remaining qty = 4")

        check(ob.matchOrders(10).isEmpty(), "no cross → empty trades")
        // Regression: cancel still works post-partial-fill
        check(ob.cancelOrder(11, "b2"), "cancel partially-filled open order")
    }

    // ── Task 2: price-time priority tie-break ─────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b_early", OrderType.BUY, 100, 3)
        ob.placeOrder(2, "b_late",  OrderType.BUY, 100, 3)   // same price, later timestamp
        ob.placeOrder(3, "s1",      OrderType.SELL, 100, 3)

        val trades = ob.matchOrders(4)
        check(trades.size == 1,                   "exactly one trade for tie-break test")
        check(trades[0].buyOrderId == "b_early",  "earlier BUY has priority (price-time)")
        check(ob.getOrder(5, "b_late")!!.status == OrderStatus.OPEN, "later BUY still open")
    }

    // ── Task 2: trade price = sell price (maker pricing) ─────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY,  110, 5)
        ob.placeOrder(2, "s1", OrderType.SELL,  90, 5)

        val trades = ob.matchOrders(3)
        check(trades.size == 1,        "one trade")
        check(trades[0].price == 90,   "trade price = sell price (maker), not BUY price")
    }

    // ── Task 3: TTL expiry ────────────────────────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1,  "b1", OrderType.BUY,  100, 5, ttl = 50)  // expires at t=51
        ob.placeOrder(2,  "b2", OrderType.BUY,  100, 5)             // no TTL
        ob.placeOrder(3,  "s1", OrderType.SELL,  99, 3, ttl = 10)  // expires at t=13

        check(ob.getOpenOrderCount(12) == 3, "t=12: all 3 open (s1 expires at t=13)")
        check(ob.getOpenOrderCount(14) == 2, "t=14: s1 expired; b1+b2 remain")
        check(ob.getOpenOrderCount(55) == 1, "t=55: b1 expired; only b2 remains")

        check(!ob.cancelOrder(61, "s1"), "cancel already-expired order → false")

        // Expired orders must not participate in matching
        ob.placeOrder(62, "s2", OrderType.SELL, 100, 5)
        val trades = ob.matchOrders(63)
        check(trades.size == 1,                "only b2 matches s2; expired b1 excluded")
        check(trades[0].buyOrderId == "b2",    "correct buyer is b2")
    }

    // ── Task 3: TTL = 0 edge case ─────────────────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY, 100, 5, ttl = 0)  // expiresAt = 1
        // Next operation at same timestamp should flush it
        check(ob.getOpenOrderCount(1) == 0, "ttl=0 → order expired at own timestamp")
    }

    // ── Task 3: expiry during match ───────────────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY,  100, 5, ttl = 8)   // expires at t=9
        ob.placeOrder(2, "s1", OrderType.SELL, 100, 5)

        // Calling matchOrders at t=10 flushes b1 first, so no match occurs
        val trades = ob.matchOrders(10)
        check(trades.isEmpty(), "expired BUY order does not match at t=10")
    }

    // ── Task 4: getVWAP ───────────────────────────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1,  "b1", OrderType.BUY,  100, 10)
        ob.placeOrder(2,  "s1", OrderType.SELL, 100,  4)
        ob.placeOrder(3,  "s2", OrderType.SELL, 100,  6)
        ob.matchOrders(4)   // trades at t=4: (price=100,qty=4) and (price=100,qty=6)

        ob.placeOrder(10, "b2", OrderType.BUY,  102, 2)
        ob.placeOrder(11, "s3", OrderType.SELL, 102, 2)
        ob.matchOrders(12)  // trade at t=12: (price=102,qty=2)

        // Full window: notional = 100*4+100*6+102*2 = 1204; qty = 12 → 100.333…
        val vwapFull = ob.getVWAP(15, windowMs = 20)!!
        check(Math.abs(vwapFull - (1204.0 / 12)) < 0.001, "VWAP full window ≈ 100.33")

        // Narrow window: only t=12 trade → VWAP = 102
        val vwapNarrow = ob.getVWAP(15, windowMs = 5)!!
        check(Math.abs(vwapNarrow - 102.0) < 0.001, "VWAP narrow window = 102.0")

        // Window with no trades
        check(ob.getVWAP(15, windowMs = 1) == null, "VWAP empty window → null")

        // Window exactly on trade boundary (inclusive right)
        val vwapBoundary = ob.getVWAP(12, windowMs = 0)
        check(vwapBoundary != null && Math.abs(vwapBoundary - 102.0) < 0.001,
            "VWAP windowMs=0 includes trade at exact timestamp (inclusive right)")
    }

    // ── Task 4: getTopTraders ─────────────────────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY,  100, 10, userId = "alice")
        ob.placeOrder(2, "s1", OrderType.SELL, 100, 10, userId = "bob")
        ob.matchOrders(3)   // alice +10, bob +10

        ob.placeOrder(4, "b2", OrderType.BUY,  100, 5, userId = "alice")
        ob.placeOrder(5, "s2", OrderType.SELL, 100, 5, userId = "carol")
        ob.matchOrders(6)   // alice +5, carol +5  →  alice=15, bob=10, carol=5

        val top3 = ob.getTopTraders(7, 3)
        check(top3[0] == "alice(15)", "top trader: alice with 15")
        check(top3[1] == "bob(10)",   "second: bob with 10")
        check(top3[2] == "carol(5)",  "third: carol with 5")

        // n larger than available traders
        check(ob.getTopTraders(8, 10).size == 3, "getTopTraders returns only available traders")

        // Anonymous trades must not appear
        ob.placeOrder(9,  "b3", OrderType.BUY,  100, 100)
        ob.placeOrder(10, "s3", OrderType.SELL, 100, 100)
        ob.matchOrders(11)
        check(ob.getTopTraders(12, 5).size == 3, "anonymous trades excluded from leaderboard")
    }

    // ── Task 4: alphabetical tie-break in getTopTraders ───────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY,  100, 5, userId = "zara")
        ob.placeOrder(2, "s1", OrderType.SELL, 100, 5, userId = "alan")
        ob.matchOrders(3)   // zara=5, alan=5 — tied; alan should come first alphabetically

        val top2 = ob.getTopTraders(4, 2)
        check(top2[0] == "alan(5)", "tie-break: alan before zara (alphabetical ascending)")
        check(top2[1] == "zara(5)", "tie-break: zara second")
    }

    // ── Task 4: getPriceLevel ─────────────────────────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY, 100, 7)
        ob.placeOrder(2, "b2", OrderType.BUY, 100, 3)   // same level → total 10
        ob.placeOrder(3, "b3", OrderType.BUY,  99, 5)   // different level

        check(ob.getPriceLevel(4, OrderType.BUY, 100) == 10, "BUY level 100 = 10")
        check(ob.getPriceLevel(5, OrderType.BUY,  99) ==  5, "BUY level 99 = 5")
        check(ob.getPriceLevel(6, OrderType.BUY,  98) ==  0, "BUY level 98 = 0 (no orders)")

        // Partial match reduces level
        ob.placeOrder(7, "s1", OrderType.SELL, 100, 4)
        ob.matchOrders(8)   // 4 units consumed from BUY level 100

        check(ob.getPriceLevel(9, OrderType.BUY, 100)   == 6, "BUY level 100 reduced to 6 post-fill")
        check(ob.getPriceLevel(10, OrderType.SELL, 100) == 0, "SELL level 100 = 0 (fully filled)")
    }

    // ── Task 4: getPriceLevel respects TTL flush ──────────────────────────────
    run {
        val ob = OrderBook()
        ob.placeOrder(1, "b1", OrderType.BUY, 100, 5, ttl = 10)  // expires at t=11

        check(ob.getPriceLevel(5,  OrderType.BUY, 100) == 5, "level=5 before expiry")
        check(ob.getPriceLevel(12, OrderType.BUY, 100) == 0, "level=0 after TTL flush at t=11")
    }

    println("\n✓ All tests passed.")
}