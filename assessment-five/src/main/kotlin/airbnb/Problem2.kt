package airbnb

import kotlin.math.pow

// Binary Tree Right Side View
// Medium BFS DFS Tree
// Given the root of a binary tree, imagine yourself standing on the right side of it. Return the values of the nodes you can see, ordered from top to bottom.

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
    var idx = 0
    var level = 0
    val results = mutableListOf<Int>()

    while(idx < nodes.size) {
        results.add(nodes[idx]!!) // add
        level++

        val rightIdx = idx + 2.0.pow(level.toDouble()).toInt()
        val leftIdx = rightIdx - 1

        idx = if (rightIdx < nodes.size && nodes[rightIdx] != null) {
            rightIdx
        } else if (leftIdx < nodes.size) {
            leftIdx
        } else {
            break
        }
    }

    return results
}

fun main() {
    println(rightSideView(listOf(1, 2, 3, null, 5, null, 4)))
    println(rightSideView(listOf(1, 2, null, null, 5)))
}