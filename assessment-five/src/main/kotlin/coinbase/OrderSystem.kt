package coinbase

import java.util.PriorityQueue
import java.util.TreeMap

//  Part 1: Implement an in-memory order book. All operations receive a timestamp
//  (monotonically increasing integer, milliseconds). An order is either a BUY or SELL
//  for a given quantity at a given price.
//
//    placeOrder(timestamp, orderId, type, price, quantity) → bool
//    - Places a new order into the book.
//    - type is either "BUY" or "SELL".
//    - price and quantity are positive integers.
//    - Returns true if successfully placed.
//    - Returns false if an order with the same orderId already exists.
//
//    cancelOrder(timestamp, orderId) → bool
//    - Cancels an existing open order.
//    - Returns true if successfully cancelled.
//    - Returns false if the orderId does not exist or has already been filled/cancelled.
//
//    getOrder(timestamp, orderId) → Order | null
//    - Returns the current state of an order: {orderId, type, price, quantity, status}.
//    - Status is one of "OPEN", "FILLED", "CANCELLED".
//    - Returns null if orderId does not exist.
//
//  Part 2:
//
//      matchOrders(timestamp) → list[Trade]
//          - Attempts to match all outstanding OPEN BUY and SELL orders using price-time priority.
//          - A BUY order at price B matches a SELL order at price S if B >= S.
//          - Matching rules:
//              1. Always match the highest-priced BUY with the lowest-priced SELL first.
//              2. If multiple BUYs share the same price, the one placed earliest (lowest timestamp) has priority.
//              3. Same rule applies to SELLs.
//              4. Trade price = the SELL order's price (maker pricing).
//              5. Partial fills are allowed: if BUY qty=10 and SELL qty=3, the SELL is FILLED,
//                  the BUY remains OPEN with remaining quantity=7.
//          - Returns a list of Trade objects executed in this call:
//                  {buyOrderId, sellOrderId, price, quantity}
//          - Orders that become fully filled have status updated to "FILLED".
//          - matchOrders may be called multiple times; only OPEN orders participate.
enum class OrderStatus {
    OPEN,
    FILLED,
    CANCELLED,
}

enum class OrderType {
    BUY,
    SELL
}

data class Order(
    val id: String,
    val type: OrderType,
    val price: Int,
    val quantity: Int,
    val placedAt: Long,
    val status: OrderStatus = OrderStatus.OPEN,
)

data class Trade(
    val buyOrderId: String,
    val sellOrderId: String,
    val price: Int,
    val quantity: Int,
)

class OrderSystem {
    val orders = mutableMapOf<String, TreeMap<Long, Order>>()
    val availableBuys = PriorityQueue(
        compareByDescending<Order> { it.price }.thenBy { it.placedAt }
    )
    val availableSells = PriorityQueue(
        compareBy<Order> { it.price }.thenBy { it.placedAt }
    )
    val trades = TreeMap<Long, Trade>()

    fun placeOrder(timestamp: Long, orderId: String, type: OrderType, price: Int, quantity: Int): Boolean {
        return if (!orders.containsKey(orderId)) {
            val order = Order(
                id = orderId,
                type = type,
                price = price,
                quantity = quantity,
                status = OrderStatus.OPEN,
                placedAt = timestamp
            )
            orders[orderId] = TreeMap<Long, Order>().apply {
                put(timestamp, order)
            }

            if (type == OrderType.BUY) {
                availableBuys.offer(order)
            } else if (type == OrderType.SELL) {
                availableSells.offer(order)
            }

            true
        } else {
            false
        }
    }

    fun cancelOrder(timestamp: Long, orderId: String): Boolean {
        val orderHistory = orders[orderId] ?: return false

        orderHistory[timestamp] = orderHistory.lastEntry().value.copy(
            status = OrderStatus.CANCELLED,
        )

        return true
    }

    fun getOrder(timestamp: Long, orderId: String): Order? {
        val orderHistory = orders[orderId] ?: return null

        return orderHistory.lastEntry().value
    }

    fun matchOrders(timestamp: Long): List<Trade> {
        fun nextOpen(orders: PriorityQueue<Order>): Order? {
            while (orders.isNotEmpty() && orders.peek().status == OrderStatus.CANCELLED) {
                orders.poll()
            }

            return if (orders.isNotEmpty()) {
                orders.poll()
            } else {
                null
            }
        }

        while (availableBuys.isNotEmpty() && availableSells.isNotEmpty()) {
            val availableBuy = nextOpen(availableBuys) ?: break
            val availableSell = nextOpen(availableSells) ?: break

            if (availableBuy.price >= availableSell.price) {
                val trade = Trade(
                    sellOrderId = availableSell.id,
                    buyOrderId = availableBuy.id,
                    price = maxOf(availableBuy.price, availableSell.price),
                    quantity = minOf(availableBuy.quantity, availableSell.quantity),
                )
                trades[timestamp] = trade

                val remainingBuyLeft = availableBuy.quantity - trade.quantity
                val newStatus = when (remainingBuyLeft) {
                    0 -> OrderStatus.FILLED
                    else -> OrderStatus.CANCELLED
                }

                orders[availableBuy.id]!![timestamp] = orders[availableBuy.id]!!.lastEntry().value.copy(
                    status = newStatus,
                    quantity = remainingBuyLeft
                )
            }
        }

        return emptyList()
    }
}