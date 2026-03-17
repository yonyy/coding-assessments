# Coding Practice

Mock coding assessments in Kotlin, styled after industry assessments (CodeSignal format). Each assessment is a self-contained Gradle project with 4 problems of increasing difficulty.

## Structure

```
.
├── assessment-one/       # Instacart mock — Arrays, Intervals, Sliding Window, Dijkstra
├── assessment-two/       # Instacart mock — Sorting, Hash Map, SJF Scheduling, Binary Search
├── assessment-three/     # 4 problems
├── assessment-four/      # 4 problems
├── assessment-five/      # 4 problems
├── LEARNINGS.md          # Algorithm patterns and Kotlin idioms learned across all assessments
└── LEARNINGS.html        # Rendered version of LEARNINGS.md
```

## Assessments

| # | Theme | Problems |
|---|-------|---------|
| 1 | Instacart | Grocery Restock Priority · Delivery Time Windows · Batch Order Fulfillment · Shopper Network Reliability |
| 2 | Instacart | Shopper Tip Calculator · Substitution Chains · Order Wave Scheduler · Store Coverage Radius |
| 3 | — | 4 problems |
| 4 | — | 4 problems |
| 5 | — | 4 problems |

Each assessment is 4 questions, 90-minute format, Kotlin.

## Running a Problem

Each assessment is a Gradle project. From any assessment directory:

```bash
./gradlew run
```

Or compile and run a single file directly:

```bash
kotlinc src/main/kotlin/Problem1.kt -include-runtime -d sol.jar && java -jar sol.jar
```

## Algorithm Patterns Covered

See [`LEARNINGS.md`](LEARNINGS.md) for detailed notes, code examples, and common bugs for each pattern:

1. **Monotonic Deque** — sliding window min/max in O(n)
2. **Frequency Map Sliding Window** — longest subarray with ≤ k distinct elements
3. **Kahn's Algorithm** — topological sort with cycle detection
4. **Binary Search on Answer Space** — search possible answers, not inputs
5. **DP + Sliding Window Minimum** — O(n) optimization of O(n×k) DP
6. **SJF Scheduling** — two-structure approach (arrivals list + available heap)
7. **Sequential Greedy (k swaps)** — re-evaluate candidate pool after each operation
8. **Run-Length Encoding** — encode/decode with multi-digit counts
9. **Hash Map + Priority Sort** — frequency ranking with tie-breaking
10. **Modified Dijkstra** — primary metric + secondary constraint (e.g. reliability)
11. **Union-Find (DSU)** — connected components with path compression and union by rank
12. **Stack-Based Undo** — snapshot history before each mutation
