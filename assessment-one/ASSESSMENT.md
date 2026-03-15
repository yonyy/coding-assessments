# Instacart × CodeSignal — Industry Coding Assessment (Mock)
**4 Questions · 90 Minutes · Kotlin**

---

## Problem 1 — Grocery Restock Priority
**Difficulty:** Easy | **Estimated Time:** ~15 min | **Tags:** Arrays, Sorting

### Description
Instacart's warehouse management system tracks items that need restocking. Each item has a `stock` level and a `threshold`. An item needs restocking if its stock is **strictly below** its threshold.

Given a list of items, return the names of items that need restocking, sorted by how urgently they need it — sorted by `(stock - threshold)` ascending (most urgent first). Break ties alphabetically.

### Example
**Input:**
```kotlin
data class Item(val name: String, val stock: Int, val threshold: Int)

val items = listOf(
    Item("apple",  5,  10),
    Item("banana", 2,  4),
    Item("mango",  8,  8),
    Item("grape",  1,  9)
)
```
**Output:** `["grape", "apple", "banana"]`

**Explanation:**
- grape: 1-9 = -8 (most urgent)
- apple: 5-10 = -5
- banana: 2-4 = -2
- mango: 8-8 = 0 → not strictly below threshold, excluded

### Constraints
- 1 ≤ items.size ≤ 10⁴
- 0 ≤ stock, threshold ≤ 10⁶
- All item names are unique lowercase strings

### Starter Code
```kotlin
data class Item(val name: String, val stock: Int, val threshold: Int)

fun restockPriority(items: List<Item>): List<String> {
    // TODO: filter items where stock < threshold,
    // then sort by (stock - threshold) asc, break ties alphabetically
    return emptyList()
}

fun main() {
    val items = listOf(
        Item("apple",  5,  10),
        Item("banana", 2,  4),
        Item("mango",  8,  8),
        Item("grape",  1,  9)
    )
    println(restockPriority(items))
    // Expected: [grape, apple, banana]
}
```

---

## Problem 2 — Delivery Time Windows
**Difficulty:** Medium | **Estimated Time:** ~20 min | **Tags:** Intervals, Greedy, Sorting

### Description
Instacart offers delivery windows to customers. A shopper can handle **one delivery at a time**. Given a list of delivery requests as `[start, end]` pairs (inclusive), find the **maximum number of non-overlapping deliveries** a shopper can complete.

Two deliveries overlap if one starts **before** the other ends. A delivery ending at time `t` and another starting at `t` do **not** overlap.

### Examples
**Example 1:**

Input:
```kotlin
val deliveries = listOf(
    intArrayOf(1,3), intArrayOf(2,4), intArrayOf(3,5),
    intArrayOf(6,8), intArrayOf(7,9), intArrayOf(8,10)
)
```
Output: `3`

Explanation: Select [1,3],[3,5],[6,8] or [1,3],[3,5],[8,10] → 3 non-overlapping deliveries.

**Example 2:**

Input:
```kotlin
val deliveries = listOf(
    intArrayOf(1,10), intArrayOf(2,3), intArrayOf(4,5)
)
```
Output: `2`

Explanation: Picking [1,10] blocks everything else. Best: [2,3] and [4,5].

### Constraints
- 1 ≤ deliveries.size ≤ 10⁵
- 0 ≤ start ≤ end ≤ 10⁹

### Starter Code
```kotlin
fun maxDeliveries(deliveries: List<IntArray>): Int {
    // TODO: classic interval scheduling
    // Sort by end time, greedily pick the earliest-ending non-overlapping delivery
    return 0
}

fun main() {
    println(maxDeliveries(listOf(
        intArrayOf(1,3), intArrayOf(2,4), intArrayOf(3,5),
        intArrayOf(6,8), intArrayOf(7,9), intArrayOf(8,10)
    ))) // Expected: 3

    println(maxDeliveries(listOf(
        intArrayOf(1,10), intArrayOf(2,3), intArrayOf(4,5)
    ))) // Expected: 2

    println(maxDeliveries(listOf(
        intArrayOf(1,2), intArrayOf(3,4), intArrayOf(5,6)
    ))) // Expected: 3
}
```

---

## Problem 3 — Batch Order Fulfillment
**Difficulty:** Medium-Hard | **Estimated Time:** ~25 min | **Tags:** Sliding Window, Hash Map, Two Pointers

### Description
Instacart batches multiple customer orders into a single shopper trip. A trip is valid if the shopper visits **at most `k` distinct store aisles**.

Given a list of items as their aisle IDs in collection order, and an integer `k`, return the **length of the longest contiguous sub-trip** visiting at most `k` distinct aisles.

### Examples
**Example 1:**

Input: `aisles = [1, 2, 1, 3, 4, 2, 3], k = 2` → Output: `3`

Explanation: [1,2,1] visits {1,2} — length 3. No window of length 4+ stays ≤ 2 aisles.

**Example 2:**

Input: `aisles = [1, 2, 3, 4, 5], k = 3` → Output: `3`

Explanation: Any 3-element window visits exactly 3 aisles.

**Example 3:**

Input: `aisles = [1, 1, 1, 1], k = 2` → Output: `4`

Explanation: Only 1 distinct aisle — entire array qualifies.

### Constraints
- 1 ≤ aisles.size ≤ 10⁵
- 1 ≤ k ≤ aisles.size
- 1 ≤ aisles[i] ≤ 10⁴

### Starter Code
```kotlin
fun longestBatch(aisles: IntArray, k: Int): Int {
    // TODO: sliding window with a frequency map
    // Expand right pointer; shrink left when distinct count > k
    return 0
}

fun main() {
    println(longestBatch(intArrayOf(1,2,1,3,4,2,3), 2)) // Expected: 3
    println(longestBatch(intArrayOf(1,2,3,4,5), 3))      // Expected: 3
    println(longestBatch(intArrayOf(1,1,1,1), 2))         // Expected: 4
    println(longestBatch(intArrayOf(1,2,1,2,3), 2))       // Expected: 4
}
```

---

## Problem 4 — Shopper Network Reliability
**Difficulty:** Hard | **Estimated Time:** ~30 min | **Tags:** Graphs, Dijkstra, Priority Queue

### Description
Instacart models its shopper network as a weighted directed graph. Nodes are fulfillment zones; edges carry a **travel time** (Int) and a **reliability** (Double, 0.0–1.0 probability of being open).

Given `n` zones (0-indexed), `edges` as `[u, v, time, reliability]`, a `src`, a `dst`, and a `minReliability` threshold, find the **minimum travel time** from `src` to `dst` along any path whose **path reliability** (product of edge reliabilities) is **≥ minReliability**.

Return `-1` if no valid path exists.

### Example
**Input:**
```
n = 4
edges = [
  [0, 1, 4,  0.90],
  [0, 2, 2,  0.80],
  [1, 3, 3,  0.95],
  [2, 3, 5,  0.70],
  [0, 3, 10, 1.00]
]
src = 0, dst = 3, minReliability = 0.75
```
**Output:** `7`

**Explanation:**
- 0→1→3: time=7, reliability=0.9×0.95=0.855 ✓
- 0→2→3: time=7, reliability=0.8×0.7=0.56 ✗ (below 0.75)
- 0→3 direct: time=10, reliability=1.0 ✓ but slower
- Best valid path: 0→1→3 with time=7

### Constraints
- 1 ≤ n ≤ 10⁴
- 0 ≤ edges.size ≤ 5×10⁴
- 0.0 ≤ reliability ≤ 1.0
- All edge times are positive integers
- src ≠ dst

### Starter Code
```kotlin
import java.util.PriorityQueue

data class Edge(val to: Int, val time: Int, val reliability: Double)
data class State(val time: Int, val node: Int, val reliability: Double)

fun minTravelTime(
    n: Int,
    edges: List<List<Any>>,
    src: Int,
    dst: Int,
    minReliability: Double
): Int {
    // Step 1: Build adjacency list
    val graph = Array(n) { mutableListOf<Edge>() }
    for (e in edges) {
        val u = e[0] as Int; val v = e[1] as Int
        val t = e[2] as Int; val r = e[3] as Double
        graph[u].add(Edge(v, t, r))
    }

    // Step 2: Modified Dijkstra
    // TODO: min-heap on time; track best (time, reliability) per node
    // Accept dst only if reliability >= minReliability

    return -1
}

fun main() {
    val edges = listOf(
        listOf(0, 1, 4,  0.90),
        listOf(0, 2, 2,  0.80),
        listOf(1, 3, 3,  0.95),
        listOf(2, 3, 5,  0.70),
        listOf(0, 3, 10, 1.00)
    )
    println(minTravelTime(4, edges, 0, 3, 0.75)) // Expected: 7
}
```

---

## Kotlin Quick Reference

```kotlin
// Multi-key sort
.sortedWith(compareBy({ it.field1 }, { it.field2 }))

// Safe map increment
map[key] = (map[key] ?: 0) + 1

// PriorityQueue (min-heap)
PriorityQueue<T>(compareBy { it.field })
PriorityQueue<T>(compareByDescending { it.field })

// Collection chains
.filter { }.map { }.groupBy { }
.sumOf { }  .maxOf { }  .minOf { }

// Data class destructuring
val (a, b, c) = myDataClass

// Run locally
// kotlinc Solution.kt -include-runtime -d sol.jar && java -jar sol.jar
```