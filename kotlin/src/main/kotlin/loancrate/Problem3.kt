package loancrate

//  Problem
//  Given a list of transaction records (userId: String, amount: Int), return the top K users by total spend, sorted descending. If two users have equal total, sort by userId ascending.
//  data class Transaction(val userId: String, val amount: Int)
//
//  Input:  [("alice", 50), ("bob", 30), ("alice", 70), ("carol", 150), ("bob", 20)], k = 2
//  Output: ["carol", "alice"]   // 150, 120, 50

data class Transaction(val userId: String, val amount: Int)

fun topKSpenders(transactions: List<Transaction>, k: Int): List<String> {
    TODO("Not yet implemented")
}

fun main() {
    // println(topKSpenders(k = 2, transactions = listOf(
    //     Transaction("alice", 50),
    //     Transaction("bob", 30),
    //     Transaction("alice", 70),
    //     Transaction("carol", 150),
    //     Transaction("bob", 20)
    // )))  // Expected: [carol, alice]
}
