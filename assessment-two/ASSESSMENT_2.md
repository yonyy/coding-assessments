# Instacart × CodeSignal — Industry Coding Assessment (Mock Set 2)
**4 Questions · 90 Minutes · Kotlin**

---

## Problem 1 — Shopper Tip Calculator
**Difficulty:** Easy | **Estimated Time:** ~15 min | **Tags:** Arrays, Math, Sorting

### Description
Instacart shoppers receive tips from customers after each delivery. Given a list of tip amounts (as doubles), return the **top `k` tips** sorted in descending order.

If the list has fewer than `k` elements, return all tips sorted descending.

### Examples
**Example 1:**

Input:
```kotlin
val tips = listOf(3.5, 10.0, 2.0, 8.5, 6.0)
val k = 3
```
Output: `[10.0, 8.5, 6.0]`

**Example 2:**

Input:
```kotlin
val tips = listOf(1.0, 5.0)
val k = 5
```
Output: `[5.0, 1.0]`

Explanation: Fewer than k elements — return all sorted descending.

### Constraints
- 1 ≤ tips.size ≤ 10⁵
- 0.0 ≤ tips[i] ≤ 1000.0
- 1 ≤ k ≤ 10⁵

### Starter Code
```kotlin
fun topKTips(tips: List<Double>, k: Int): List<Double> {
    // TODO
    return emptyList()
}

fun main() {
    println(topKTips(listOf(3.5, 10.0, 2.0, 8.5, 6.0), 3)) // Expected: [10.0, 8.5, 6.0]
    println(topKTips(listOf(1.0, 5.0), 5))                  // Expected: [5.0, 1.0]
    println(topKTips(listOf(4.0, 4.0, 4.0, 1.0), 2))        // Expected: [4.0, 4.0]
}
```

---

## Problem 2 — Substitution Chains
**Difficulty:** Medium | **Estimated Time:** ~20 min | **Tags:** Hash Map, Simulation

### Description
When a requested item is out of stock, Instacart allows shoppers to substitute it. Substitutions can chain: item A is replaced by B, B is replaced by C, and so on.

Given a map of `substitutions: Map<String, String>` where `substitutions[item]` is the replacement for `item`, and a starting item, return the **final item** at the end of the substitution chain.

A chain ends when an item has no substitution. Assume there are **no cycles**.

### Examples
**Example 1:**

Input:
```kotlin
val substitutions = mapOf("apple" to "pear", "pear" to "mango", "mango" to "grape")
val start = "apple"
```
Output: `"grape"`

Explanation: apple → pear → mango → grape (grape has no substitution)

**Example 2:**

Input:
```kotlin
val substitutions = mapOf("cola" to "pepsi")
val start = "water"
```
Output: `"water"`

Explanation: water has no substitution — return as-is.

### Constraints
- 0 ≤ substitutions.size ≤ 10⁴
- No cycles in the substitution chain
- All keys and values are non-empty lowercase strings

### Starter Code
```kotlin
fun finalItem(substitutions: Map<String, String>, start: String): String {
    // TODO: follow the chain until no substitution exists
    return ""
}

fun main() {
    val subs1 = mapOf("apple" to "pear", "pear" to "mango", "mango" to "grape")
    println(finalItem(subs1, "apple"))  // Expected: grape

    val subs2 = mapOf("cola" to "pepsi")
    println(finalItem(subs2, "water"))  // Expected: water

    println(finalItem(emptyMap(), "milk")) // Expected: milk
}
```

---

## Problem 3 — Order Wave Scheduler
**Difficulty:** Medium-Hard | **Estimated Time:** ~25 min | **Tags:** Sorting, Greedy, Simulation

### Description
Instacart processes orders in **waves**. Each order has an `id`, an `arrivalTime`, and a `processingTime`. A single processor handles one order at a time.

The processor works as follows:
- At time `0`, it is free.
- It always picks the **available order with the shortest `processingTime`** (ties broken by lowest `id`).
- An order is **available** if its `arrivalTime ≤ current time`.
- If no order is available, the processor jumps forward to the next `arrivalTime`.

Return the order IDs in the sequence they are processed.

### Examples
**Example 1:**

Input:
```kotlin
data class Order(val id: Int, val arrivalTime: Int, val processingTime: Int)

val orders = listOf(
    Order(1, 0, 5),
    Order(2, 0, 3),
    Order(3, 2, 1),
    Order(4, 6, 2)
)
```
Output: `[2, 3, 1, 4]`

Explanation:
- t=0: orders 1 and 2 available. Pick order 2 (shortest processing=3). Runs until t=3.
- t=3: orders 1 and 3 available. Pick order 3 (shortest processing=1). Runs until t=4.
- t=4: order 1 available. Pick order 1 (processing=5). Runs until t=9.
- t=9: order 4 available (arrived at t=6). Pick order 4. Done.

### Constraints
- 1 ≤ orders.size ≤ 10⁵
- 0 ≤ arrivalTime ≤ 10⁹
- 1 ≤ processingTime ≤ 10⁴
- All order IDs are unique

### Starter Code
```kotlin
import java.util.PriorityQueue

data class Order(val id: Int, val arrivalTime: Int, val processingTime: Int)

fun processOrders(orders: List<Order>): List<Int> {
    // TODO: simulate shortest-job-first scheduling
    // Sort by arrival. Use a min-heap on (processingTime, id) for available orders.
    return emptyList()
}

fun main() {
    val orders = listOf(
        Order(1, 0, 5),
        Order(2, 0, 3),
        Order(3, 2, 1),
        Order(4, 6, 2)
    )
    println(processOrders(orders)) // Expected: [2, 3, 1, 4]

    // All arrive at once — pick by shortest then by id
    val orders2 = listOf(
        Order(3, 0, 2),
        Order(1, 0, 5),
        Order(2, 0, 2)
    )
    println(processOrders(orders2)) // Expected: [2, 3, 1]
}
```

---

## Problem 4 — Store Coverage Radius
**Difficulty:** Hard | **Estimated Time:** ~30 min | **Tags:** Binary Search, Geometry, Greedy

### Description
Instacart is placing delivery zones along a single road. The road is represented as a number line from `0` to `roadLength`. There are `n` stores at given positions on this road.

You want to place exactly `k` circular coverage zones (each a segment of length `2r` centered on the zone center) such that **all stores are covered** by at least one zone. The zones may overlap.

Find the **minimum radius `r`** (as a double, rounded to 1 decimal place) such that `k` zones can cover all `n` store positions. Zone centers can be placed anywhere on the number line.

**Key insight:** For a given radius `r`, greedily check if `k` zones can cover all stores:
- Sort stores.
- Place the first zone center at `stores[0] + r` (covers as far right as possible from the leftmost uncovered store).
- Advance to the next uncovered store and repeat.
- If you place ≤ `k` zones and cover all stores, `r` is sufficient.

### Examples
**Example 1:**

Input:
```kotlin
val stores = listOf(1, 5, 9, 15)
val k = 2
val roadLength = 20
```
Output: `5.0`

Explanation: With r=5.0, place zone 1 at center 6 (covers 1–11, covers stores 1,5,9). Place zone 2 at center 15 (covers 10–20, covers store 15). 2 zones suffice.

**Example 2:**

Input:
```kotlin
val stores = listOf(1, 2, 3)
val k = 1
val roadLength = 10
```
Output: `1.0`

Explanation: With r=1.0, one zone centered at 2 covers [1,3].

### Constraints
- 1 ≤ stores.size ≤ 10⁴
- 1 ≤ k ≤ stores.size
- 0 ≤ stores[i] ≤ roadLength ≤ 10⁶
- All store positions are distinct

### Starter Code
```kotlin
fun minCoverageRadius(stores: List<Int>, k: Int, roadLength: Int): Double {
    val sorted = stores.sorted()

    // canCover: greedy check — can k zones of radius r cover all stores?
    fun canCover(r: Double): Boolean {
        // TODO: greedy left-to-right sweep
        return false
    }

    // Binary search on r over [0.0, roadLength / 2.0]
    // TODO
    return 0.0
}

fun main() {
    println(minCoverageRadius(listOf(1, 5, 9, 15), 2, 20))  // Expected: 5.0
    println(minCoverageRadius(listOf(1, 2, 3), 1, 10))       // Expected: 1.0
    println(minCoverageRadius(listOf(0, 10), 2, 10))          // Expected: 0.0
}
```

---

## Kotlin Quick Reference

```kotlin
// Sorting
.sortedBy { it.field }
.sortedByDescending { it.field }
.sortedWith(compareBy({ it.field1 }, { it.field2 }))

// Safe access
map[key]                    // → V? (nullable)
map.getOrDefault(key, 0)    // → V with fallback
list.firstOrNull { cond }   // → T?

// PriorityQueue
PriorityQueue<T>(compareBy { it.field })          // min-heap
PriorityQueue<T>(compareByDescending { it.field }) // max-heap

// Iteration
while (pq.isNotEmpty()) { val item = pq.poll() }

// Binary search pattern
var lo = 0.0; var hi = max.toDouble()
repeat(100) {
    val mid = (lo + hi) / 2.0
    if (canSolve(mid)) hi = mid else lo = mid
}
// answer ≈ lo (or hi)

// Run locally
// kotlinc Solution.kt -include-runtime -d sol.jar && java -jar sol.jar
```