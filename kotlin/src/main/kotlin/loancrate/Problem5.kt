package loancrate

//  Problem
//  Given a binary tree of LoanApplication nodes (each has an amount: Int and approved: Boolean), return the sum of all approved loan amounts on the path from root to every leaf, where the entire path from root to leaf must be all-approved.
//  data class LoanNode(
//      val amount: Int,
//      val approved: Boolean,
//      val left: LoanNode? = null,
//      val right: LoanNode? = null
//  )

//  (100, ✓)
//  /        \
//  (50, ✓)     (200, ✗)
//  /     \
//  (25, ✓) (30, ✗)
//
//  Fully approved paths: root → left → left-left only: 100+50+25 = 175
//  Output: 175

data class LoanNode(
    val amount: Int,
    val approved: Boolean,
    val left: LoanNode? = null,
    val right: LoanNode? = null
)

fun sumOfApprovedNodes(node: LoanNode): Int {
    TODO("Not yet implemented")
}
