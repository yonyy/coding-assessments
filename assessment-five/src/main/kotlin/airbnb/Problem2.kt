package airbnb

import kotlin.math.pow

/* Problem 2 — Binary Tree Right Side View: return the values visible from the right side,
   ordered top to bottom. */

// A node is visible from the right side if it is the rightmost node at its depth level.
// Example 1
//         1          ← visible: 1
//        / \
//       2   3       ← visible: 3
//        \   \
//         5   4     ← visible: 4

// Input:  root = [1, 2, 3, null, 5, null, 4]
// Output: [1, 3, 4]
// Example 2 — left-only subtree
//         1
//        /
//       2             ← visible: 2 (no right sibling at this depth)
//        \
//         5           ← visible: 5

// Input:  root = [1, 2, null, null, 5]
// Output: [1, 2, 5]
// Example 3
// Input:  root = []
// Output: []
// Constraints
// Number of nodes: [0, 100]
// -100 ≤ Node.val ≤ 100

fun rightSideView(nodes: List<Int?>): List<Int> {
    TODO("Not yet implemented")
}

fun main() {
    // println(rightSideView(listOf(1, 2, 3, null, 5, null, 4)))  // Expected: [1, 3, 4]
    // println(rightSideView(listOf(1, 2, null, null, 5)))        // Expected: [1, 2, 5]
}
