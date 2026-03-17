# Assessment Learnings — Algorithms & Kotlin

---

## Algorithmic Patterns

### 1. Monotonic Deque (Sliding Window Min / Max)
Store **indices** in the deque, not values. The front always holds the extremum for the current window.

| Variant | Maintain | Pop back when… | Use case |
|---------|----------|----------------|----------|
| Decreasing | front = window max | new value ≥ back's value | Sliding window maximum |
| Increasing | front = window min | new value ≤ back's value | Sliding window minimum / DP |

**Four-step loop (always the same shape):**
```kotlin
if (deque.first() < i - windowSize) deque.removeFirst()   // 1. expire stale front
// use deque.first() here                                   // 2. read the extremum
while (deque.isNotEmpty() && condition) deque.removeLast() // 3. maintain monotone order
deque.addLast(i)                                           // 4. enqueue current index
```
*Principle:* Any O(n²) "scan back for min/max" can be reduced to O(n) with this pattern.

```kotlin
// A4 P3 — maxFreshness: decreasing deque → sliding window maximum
val deque = ArrayDeque<Int>()
val results = mutableListOf<Int>()
scores.forEachIndexed { index, score ->
    if (deque.isNotEmpty() && index - deque.first() > w - 1) deque.removeFirst()
    while (deque.isNotEmpty() && scores[deque.last()] <= score) deque.removeLast()
    deque.addLast(index)
    if (index >= w - 1) results.add(scores[deque.first()])  // gate: only once full window exists
}
return results
```

```kotlin
// A5 P3 — minTripCost (O(n) alt): increasing deque → sliding window minimum over dp values
val dp = IntArray(n); dp[0] = cost[0]
val deque = ArrayDeque<Int>(); deque.addLast(0)
for (i in 1 until n) {
    if (deque.first() < i - maxJump) deque.removeFirst()         // expire out-of-range
    dp[i] = cost[i] + dp[deque.first()]                          // cheapest predecessor
    while (deque.isNotEmpty() && dp[deque.last()] >= dp[i]) deque.removeLast()
    deque.addLast(i)
}
return dp[n - 1]
```

---

### 2. Frequency Map Sliding Window — Variable-Width, Constraint-Based
Use when: find the longest/shortest contiguous subarray satisfying a constraint on distinct elements (or any per-element count).

**Core idea:** expand `right` unconditionally; shrink `left` whenever the window violates the constraint. The frequency map tracks membership — its `.size` is the distinct count. Removing a key when its count hits 0 is what makes `.size` reliable.

```kotlin
// A1 P3 — longestBatch: longest subarray with ≤ k distinct aisles
fun longestBatch(aisles: IntArray, k: Int): Int {
    val freq = mutableMapOf<Int, Int>()
    var left = 0
    var max = 0
    for (right in aisles.indices) {
        freq[aisles[right]] = (freq[aisles[right]] ?: 0) + 1   // expand
        while (freq.size > k) {                                  // shrink until valid
            val l = aisles[left++]
            freq[l] = freq[l]!! - 1
            if (freq[l] == 0) freq.remove(l)                    // ← key removal keeps size accurate
        }
        max = maxOf(max, right - left + 1)
    }
    return max
}
```

**Contrast with Monotonic Deque (pattern #1):**

| | Monotonic Deque | Frequency Map Window |
|---|---|---|
| **Question** | What's the extremum in this window? | How long can the window be? |
| **Window size** | Fixed (`w`) | Variable — expands until invalid |
| **Left pointer** | Advances by expiring stale front indices | Advances to restore validity |
| **Structure shrinks** | Yes — dominated elements are discarded | No — every element is counted |
| **What you read** | `deque.first()` (the extremum) | `freq.size` (distinct count) |

*Principle:* The deque answers "what's the best element I've seen?"; the frequency map answers "does this window still satisfy my constraint?" They are complementary — you'd combine both if the problem required finding the longest valid window *and* the max element within it.

**Common bugs:**
- Slicing from index 0 each iteration (`aisles.slice(0..index)`) — only checks prefixes, misses mid-array windows. O(n²).
- Not removing zero-count keys → `freq.size` overcounts distinct elements still "in" the window.

---

### 3. Kahn's Algorithm — Topological Sort (BFS)
Use when: task ordering, dependency resolution, cycle detection.

```
inDegree[task]    = number of prerequisites remaining
outboundMap[task] = list of tasks this task unblocks
```

**Key directions:**
- `dependency = [a, b]` means *b must run before a* → edge goes `b → a`
- `inDegree[a]++` (a gains a prerequisite)
- `outboundMap[b].add(a)` (b unblocks a when it completes)

Use a **min-heap** instead of a plain queue for lexicographically smallest order.

```kotlin
// A4 P4 — buildOrder: lex-smallest topological sort with cycle detection
val inDegree = IntArray(n) { 0 }
val outbound = Array(n) { mutableListOf<Int>() }
dependencies.forEach { (a, b) -> inDegree[a]++; outbound[b].add(a) }  // b → a

val pq = PriorityQueue<Int>(compareBy { it })
(0 until n).filter { inDegree[it] == 0 }.forEach { pq.add(it) }

val result = mutableListOf<Int>()
while (pq.isNotEmpty()) {
    val task = pq.poll(); result.add(task)
    outbound[task].forEach { if (--inDegree[it] == 0) pq.add(it) }
}
return if (result.size < n) emptyList() else result  // < n means a cycle exists
```

```kotlin
// A3 P3 — resolveDependencies: same algorithm; original bug had graph direction reversed:
//   ❌  inDegree[b]++; outbound[a].add(b)   ← wrong: makes b depend on a
//   ✅  inDegree[a]++; outbound[b].add(a)   ← correct: a depends on b
```

---

### 3. Binary Search on the Answer Space
Use when: the answer is a number and you can write a `canAchieve(x): Boolean` predicate.

```kotlin
var lo = lowerBound; var hi = upperBound
repeat(100) {              // ~100 iterations → float precision to 2^-100
    val mid = (lo + hi) / 2.0
    if (canAchieve(mid)) hi = mid else lo = mid
}
return lo
```
*Principle:* Don't search the input — search the space of possible answers.

```kotlin
// A2 P4 — minCoverageRadius: binary search on radius r; canCover is a greedy sweep
var lo = 0.0; var hi = (sorted.last() - sorted.first()).toDouble()
repeat(100) {
    val mid = (lo + hi) / 2.0
    if (canCover(sorted, k, mid)) hi = mid else lo = mid
}
return lo

// canCover: place a circle anchored at the first uncovered stop, jump past everything within 2r
fun canCover(sorted: List<Int>, k: Int, r: Double): Boolean {
    var circles = 1
    var reach = sorted[0] + 2 * r
    for (stop in sorted) {
        if (stop > reach) { circles++; reach = stop + 2 * r }
    }
    return circles <= k
}
```

---

### 4. Dynamic Programming with Sliding Window Minimum
Pattern: `dp[i] = cost[i] + min(dp[i-k .. i-1])`

- **O(n × k) naive:** inner loop scans back up to k steps.
- **O(n) optimized:** monotonic increasing deque (see pattern #1 above).

```kotlin
// A5 P3 — minTripCost: naive O(n × maxJump) — scan back up to maxJump positions
val dp = IntArray(cost.size); dp[0] = cost[0]
for (i in 1 until cost.size) {
    var best = dp[i - 1]
    var j = i - 2
    while (j >= max(i - maxJump, 0)) {
        if (dp[j] < best) best = dp[j]
        j--
    }
    dp[i] = cost[i] + best
}
return dp.last()   // ← NOT cost.last() — that's only the raw cost of the final stop
```

---

### 5. SJF Scheduling — Two-Structure Approach
Shortest Job First requires two separate structures:

```
arrivals list  → sorted by arrivalTime (determines what becomes available)
available heap → min-heap by processingTime (determines what to run next)
```

```kotlin
// A2 P3 — processOrders: drain arrivals into heap as time advances; always run shortest next
val arrivals = orders.sortedBy { it.arrivalTime }
val available = PriorityQueue(compareBy<Order> { it.processingTime })
var time = 0; var i = 0
while (i < arrivals.size || available.isNotEmpty()) {
    while (i < arrivals.size && arrivals[i].arrivalTime <= time) available.add(arrivals[i++])
    if (available.isEmpty()) { time = arrivals[i].arrivalTime; continue }  // jump to next arrival
    val next = available.poll()
    time += next.processingTime
}

// ❌ Common bug: single PriorityQueue sorted by arrivalTime — doesn't reprioritize
//    already-arrived orders by duration
```

---

### 6. Sequential Greedy (k Swaps / Moves)
When asked to minimize a metric over k operations:

1. Re-evaluate the full candidate pool **after each operation** — state changes each round.
2. Use a sentinel (e.g., `-1`) to detect when no improvement exists → break early.
3. Remove used elements from the pool to avoid reuse.

*Reframe:* "minimize `|sumA − sumB|` after swapping `(a, b)`" → find the pair where `a − b ≈ diff / 2`.

```kotlin
// A4 P2 — minDiff: minimize |sumA - sumB| with at most k swaps
var currSumA = sortedA.sum(); var currSumB = sortedB.sum()
val poolA = sortedA.toMutableList(); val poolB = sortedB.toMutableList()
repeat(k) {
    var bestA = -1; var bestB = -1
    var bestDiff = abs(currSumA - currSumB)
    poolA.forEach { a ->
        poolB.forEach { b ->
            val d = abs((currSumA - a + b) - (currSumB - b + a))
            if (d < bestDiff) { bestA = a; bestB = b; bestDiff = d }
        }
    }
    if (bestA == -1) return@repeat                        // no improvement possible; stop early
    poolA.remove(bestA); poolB.remove(bestB)
    currSumA = currSumA - bestA + bestB
    currSumB = currSumB - bestB + bestA
}
return abs(currSumA - currSumB)
```

---

### 7. Run-Length Encoding (Variable-Length Tokens)
**Encode** — track current char + run count; flush on change and after the loop ends.
**Decode** — scan forward past all digits after each letter to capture multi-digit counts.

```kotlin
// A5 P2 — encode: forEachIndexed; flush at char change and at final index
route.forEachIndexed { index, ch ->
    if (ch.toString() == currentDir) {
        currCount++
    } else {
        encoded += "$currentDir${if (currCount > 1) currCount else ""}"
        currentDir = ch.toString(); currCount = 1
    }
    if (index == route.length - 1)
        encoded += "$currentDir${if (currCount > 1) currCount else ""}"
}
```

```kotlin
// A5 P2 — decode: j-pointer scans ahead to collect full multi-digit number
var i = 0
while (i < encoded.length) {
    val ch = encoded[i]; var j = i + 1
    while (j < encoded.length && encoded[j].isDigit()) j++       // collect all digit chars
    val count = if (j > i + 1) encoded.substring(i + 1, j).toInt() else 1
    sb.append(ch.toString().repeat(count))
    i = j
}
// ❌ Bug: ch.digitToInt() - 1 only works for single-digit counts
//    "N12" would be misread as 1 then 2 instead of 12
```

---

### 8. Hash Map + Priority Sort (Frequency Ranking)
Count occurrences into a map, then sort by frequency with tie-breaking.

- **Static** (all counts known before sorting) → `sortedWith` on map entries — simpler.
- **Dynamic / streaming** → `PriorityQueue` with a multi-key comparator.

```kotlin
// A5 P1 — sortByFrequency: PriorityQueue with frequency desc, name asc tie-break
val pq = PriorityQueue(
    compareByDescending<Pair<Int, String>> { it.first }.thenBy { it.second }
)
cart.groupingBy { it }.eachCount().forEach { (name, count) -> pq.add(count to name) }
val result = mutableListOf<String>()
while (pq.isNotEmpty()) result.add(pq.poll().second)
return result

// ❌ Bug: omitting .thenBy { it.second } silently produces arbitrary tie order
```

```kotlin
// Alternative — static sort (all counts known upfront; no PQ needed)
return cart.groupingBy { it }.eachCount()
    .entries
    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
    .map { it.key }
```

---

### 9. Modified Dijkstra — Primary Metric + Secondary Constraint
Use when: find the shortest path (time, cost, etc.) where paths must also satisfy a constraint on a second dimension (reliability, capacity, etc.).

**Core idea:** run Dijkstra ordered by the primary metric; use a `best[]` array on the secondary dimension to prune dominated states on pop.

```kotlin
// A1 P4 — minTravelTime: min-time path with reliability ≥ threshold
// Primary metric: time (min-heap)  |  Secondary constraint: reliability (multiplicative)
val pq = PriorityQueue<State>(compareBy { it.time })
val bestReliability = DoubleArray(n) { 0.0 }

pq.offer(State(0, src, 1.0))

while (pq.isNotEmpty()) {
    val (time, node, rel) = pq.poll()
    if (rel < bestReliability[node]) continue   // a better-reliability path already settled this node
    bestReliability[node] = rel

    if (node == dst) return if (rel >= minReliability) time else continue

    for (edge in graph[node]) {
        val newRel = rel * edge.reliability
        if (newRel > bestReliability[edge.to])  // only enqueue if reliability is an improvement
            pq.offer(State(time + edge.time, edge.to, newRel))
    }
}
return -1
```

**Key principles:**

| Principle | Detail |
|-----------|--------|
| Heap ordered by primary metric | Time is minimized greedily — first time we reach dst with valid reliability is the answer |
| `best[]` tracks secondary dimension | Prunes states on pop: if a better-reliability path already settled this node, skip |
| Multiplicative reliability | Unlike additive costs, reliability compounds as a product: `newRel = rel * edge.reliability` |
| Constraint check at destination | Reaching dst doesn't mean returning immediately — check `rel >= minReliability` first; `continue` to keep searching if not met |
| Pruning on push | Only enqueue a neighbour if `newRel > bestReliability[edge.to]` — avoids polluting the heap with dominated states |

**Why standard Dijkstra doesn't work here:** Dijkstra assumes the optimal substructure holds for a single metric. With two dimensions, the fastest path to a node may not be the most reliable — so you need `bestReliability[]` to avoid discarding paths that are slower but open up better-reliability routes downstream.

---

### 10. Union-Find / Disjoint Set Union (DSU)
Use when: connected components, "are A and B in the same group?", region counting.

**Core idea:** a forest of trees where each tree = one component. The root of each tree is the component's representative. Two nodes are in the same region iff they share a root.

```kotlin
// A5 P4 — surgePricingZones
class DSU(n: Int) {
    val parent = IntArray(n) { it }   // each node starts as its own root
    val rank   = IntArray(n) { 0 }    // upper bound on subtree height

    fun find(x: Int): Int {
        if (parent[x] != x) parent[x] = find(parent[x])  // path compression
        return parent[x]                                   // return the root, not x
    }

    fun union(a: Int, b: Int) {
        val ra = find(a); val rb = find(b)
        if (ra == rb) return
        if (rank[ra] < rank[rb]) parent[ra] = rb
        else { parent[rb] = ra; if (rank[ra] == rank[rb]) rank[ra]++ }
    }

    fun connected(a: Int, b: Int) = find(a) == find(b)
    fun regionCount() = parent.indices.count { parent[it] == it }  // count roots only
}

fun surgePricingZones(n: Int, corridors: List<List<Int>>, queries: List<List<Int>>): Pair<List<Boolean>, Int> {
    val dsu = DSU(n)
    corridors.forEach { dsu.union(it[0], it[1]) }
    return Pair(queries.map { dsu.connected(it[0], it[1]) }, dsu.regionCount())
}
// Complexity: O((m + q) · α(n)) — α is inverse Ackermann, effectively constant
```

**Three key techniques:**

| Technique | Rule |
|-----------|------|
| Path compression | Recursively set `parent[x]` to the root; return `parent[x]` (the root), not `x` |
| Union by rank | Attach shallower tree under deeper; only increment rank when both ranks are equal |
| Root identification | `parent[i] == i` is the only reliable root check — counts distinct *roots*, not distinct *values* |

**Rank only increments when two equal-rank trees merge** — the only case where the combined tree grows taller than either input.

---

### 10. Stack-Based Text Editing (Undo)
Push the current state onto a history stack **before** mutating — restoring is a single `removeLast()`.

```kotlin
// A3 P2 — TextEditor: O(1) undo via history stack
// ✅ Preferred: StringBuilder for O(1) append; snapshot toString() before each mutation
class TextEditor {
    private val document = StringBuilder()
    private val history = ArrayDeque<String>()       // stores immutable snapshots

    fun insert(s: String) {
        history.addLast(document.toString())         // snapshot before mutating
        document.append(s)                           // O(1) append — no new String allocated
    }
    fun delete(k: Int) {
        history.addLast(document.toString())         // snapshot before mutating
        val newLen = maxOf(0, document.length - k)
        document.delete(newLen, document.length)     // O(k) in-place trim
    }
    fun undo() { if (history.isNotEmpty()) { document.clear(); document.append(history.removeLast()) } }
    fun getDocument() = document.toString()
}

// ❌ Original used String += which is O(n²):
//    fun insert(s: String) { history.addLast(document); document += s }
//    Each += allocates a new String and copies all existing characters

// ❌ Bug: forgetting to reassign substring — it's a pure function, returns a new String
//    document.substring(0, document.length - k)       ← result discarded
//    document = document.substring(0, document.length - k)  ← correct (String version)
```

---

## Universal Principles

| Principle | Details |
|-----------|---------|
| **Graph edge direction** | Input tuple `[a, b]` ("b before a") → edge `b → a`. Increment `inDegree[a]`, add `a` to `outboundMap[b]`. Getting this backwards is the most common graph bug. |
| **Cycle detection** | After Kahn's: `if (result.size < n) return emptyList()` — a cycle means some nodes never reach in-degree 0 and are never processed. `> n` is always false; it must be `< n`. |
| **Static vs dynamic collections** | All items known upfront → `sortedWith` (one-shot, cleaner). Items arrive over time → `PriorityQueue` (maintains order as items are added). |
| **String building** | `+=` on `String` is O(n²) — each concatenation allocates a new object. Use `StringBuilder.append()` for O(n). |
| **Deque front vs back** | Expire stale window entries from the **front**. Maintain monotone order by popping from the **back**. Read the current extremum from the **front**. |
| **DP return value** | Return `dp.last()` (minimum accumulated cost), never `cost.last()` (raw cost of the final stop only). |
| **Tie-breaking in comparators** | A single-key comparator silently produces arbitrary ordering for equal elements. Always chain `.thenBy { }` for deterministic tie-breaking. |
| **Empty deque guard** | Always check `deque.isNotEmpty()` before `.last()` or `.first()` — an unchecked access on an empty deque throws `NoSuchElementException`. |
| **Pure functions don't mutate** | `String.substring()`, `String.replace()`, etc. return new strings — always reassign the result. |
| **Multi-dimensional shortest path** | When a path must satisfy a constraint on a second metric, standard Dijkstra breaks — the greedy optimal for metric A may not be optimal when metric B is required. Track `best[]` for the secondary dimension and prune/check at destination. |
| **Multiplicative vs additive path costs** | Additive costs (time, distance) accumulate with `+`. Multiplicative costs (reliability, probability) accumulate with `*`. The heap ordering and relaxation logic remain the same; only the accumulation operator changes. |
| **DSU root check** | Use `parent[i] == i` to count roots. `parent.distinct().size` overcounts when stale intermediate pointers exist after union-by-rank demotes a former root. |
| **DSU find return value** | Path compression sets `parent[x]` to the root, but you must `return parent[x]` — returning `x` makes every `find` return the input node, breaking all connectivity checks. |
| **Freq map key removal** | In a sliding window frequency map, always `remove` a key when its count reaches 0. Leaving zero-count entries makes `freq.size` an unreliable distinct-count. |
| **Sliding window anchor** | A window that always starts at index 0 only checks prefixes — it misses valid subarrays that start mid-array. Use a proper `left` pointer that advances when the window becomes invalid. |

---

## Performance & Data Structure Choices

### StringBuilder vs String `+=`
Every `+=` on a `String` allocates a brand-new object and copies all existing characters into it.
Over n concatenations that's O(1 + 2 + … + n) = **O(n²)** total. `StringBuilder` appends in amortized **O(1)**.

```kotlin
// ❌ O(n²) — new String object allocated on every iteration
var encoded = ""
runs.forEach { (ch, count) ->
    encoded += "$ch$count"        // copies entire string each time
}

// ✅ O(n) — single buffer, append in-place
val sb = StringBuilder()
runs.forEach { (ch, count) ->
    sb.append(ch)
    if (count > 1) sb.append(count)
}
return sb.toString()              // one allocation at the end
```

*Came up in:* A5 P2 `encode` / `decode` — the active solution used `+=` (acceptable for short routes),
but the alternative uses `StringBuilder` for correctness at scale.

---

### ArrayDeque vs MutableList as a Deque
A plain `MutableList` supports both-end access but `removeFirst()` is **O(n)** — it shifts every element
left. `ArrayDeque` uses a ring buffer, giving **O(1)** for all four named operations.

```kotlin
// ❌ MutableList — removeFirst() is O(n); intent is also unclear
val deque = mutableListOf<Int>()
deque.add(i)              // add to back — ok
deque.removeAt(0)         // remove from front — O(n) shift, unclear intent

// ✅ ArrayDeque — O(1) on both ends; operation names make intent explicit
val deque = ArrayDeque<Int>()
deque.addLast(i)          // enqueue index at back
deque.removeFirst()       // expire stale window front — O(1)
deque.removeLast()        // pop back to maintain monotone order — O(1)
deque.first()             // peek current extremum — O(1)
```

*Came up in:* A4 P3 `maxFreshness`, A5 P3 `minTripCost`, A3 P2 `TextEditor` (as history stack).
The named operations (`addLast`, `removeFirst`) also make the sliding-window intent self-documenting.

---

### PriorityQueue vs `sortedWith`

```kotlin
// sortedWith — best when all items are known before any processing begins
// O(n log n) one-shot sort; clean and concise
val ordered = items.sortedWith(compareByDescending<Item> { it.freq }.thenBy { it.name })

// PriorityQueue — best when items arrive over time (streaming / simulation)
// O(log n) per insertion/removal; maintains order as new items are added
val pq = PriorityQueue(compareByDescending<Item> { it.freq }.thenBy { it.name })
items.forEach { pq.add(it) }           // items can be added at any point
while (pq.isNotEmpty()) process(pq.poll())
```

*Came up in:* A5 P1 `sortByFrequency` — all frequencies were known upfront so `sortedWith` was the
simpler choice; the `PriorityQueue` version was kept as an alternative to show the streaming pattern.
Also A2 P3 `processOrders` — the available-jobs heap *must* be a PriorityQueue because jobs arrive
dynamically as simulation time advances.

---

### O(n × k) DP vs O(n) Monotonic Deque

```kotlin
// O(n × k) naive — inner loop rescans up to k predecessors for every i
for (i in 1 until n) {
    var best = dp[i - 1]
    var j = i - 2
    while (j >= max(i - k, 0)) { if (dp[j] < best) best = dp[j]; j-- }
    dp[i] = cost[i] + best
}
// Total: O(n × k) — fine when k is small; degrades to O(n²) when k ≈ n

// O(n) deque — front always holds the min-dp index in the window; no rescanning
for (i in 1 until n) {
    if (deque.first() < i - k) deque.removeFirst()
    dp[i] = cost[i] + dp[deque.first()]
    while (deque.isNotEmpty() && dp[deque.last()] >= dp[i]) deque.removeLast()
    deque.addLast(i)
}
// Total: each index pushed and popped at most once → O(n)
```

*Came up in:* A5 P3 `minTripCost` — the active solution is the O(n × maxJump) naive scan;
the O(n) deque version is kept as the commented-out alternative.

---

## Kotlin Idioms

### Sorting & Comparators
```kotlin
// Multi-key sort (desc frequency, then asc name)
list.sortedWith(compareByDescending<T> { it.count }.thenBy { it.name })

// PriorityQueue with multi-key comparator
val pq = PriorityQueue(compareByDescending<Pair<Int,String>> { it.first }.thenBy { it.second })
```

### Frequency Counting
```kotlin
// Concise: Map<T, Int> in one expression
val freq = items.groupingBy { it }.eachCount()

// Full pipeline: count → sort → extract keys
items.groupingBy { it }.eachCount()
    .entries
    .sortedWith(compareByDescending<Map.Entry<String,Int>> { it.value }.thenBy { it.key })
    .map { it.key }
```

### ArrayDeque (monotonic deque / stack)
```kotlin
val deque = ArrayDeque<Int>()
deque.addLast(i)          // enqueue back
deque.removeLast()        // pop back   (maintain monotone order)
deque.removeFirst()       // expire front (slide the window)
deque.first()             // peek front  (current extremum)
deque.isNotEmpty()        // always guard before peek/remove
```

### DP Array Initialization
```kotlin
val dp = IntArray(n) { 0 }   // size n, all zeros
dp[0] = cost[0]              // seed the base case manually
```

### Useful One-Liners
```kotlin
ch.digitToInt()                          // Char → Int (single digit only)
s.substring(i + 1, j).toInt()           // multi-char digit string → Int
(0..<n).toList()                         // range to list
list.sortedDescending()                  // descending copy (non-mutating)
mutableMapOf<K,V>().apply { /* fill */ } // inline map initialization
repeat(k) { /* return@repeat to skip an iteration */ }
```

### Graph / Map Setup Pattern
```kotlin
val inDegree = IntArray(n) { 0 }
val outbound = Array(n) { mutableListOf<Int>() }

dependencies.forEach { (a, b) ->   // b must happen before a
    inDegree[a]++
    outbound[b].add(a)
}
```
