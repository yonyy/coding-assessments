package airbnb

import kotlin.math.min

//    Coin Change
//    You are given an integer array coins representing coins of different denominations and an
//    integer amount representing a total amount of money. Return the fewest number of coins that
//    you need to make up that amount. If that amount of money cannot be made up by any combination
//    of the coins, return -1. You may assume that you have an infinite number of each kind of coin.
//
//    Constraints:
//    1 <= coins.length <= 12
//    1 <= coins[i] <= 2^31 - 1
//    0 <= amount <= 10^4

class Problem6 {
    fun minCoinChange(coins: List<Int>, amount: Int): Int {
        val dp = Array(amount + 1) { Int.MAX_VALUE }
        dp[0] = 0

        // dp = [ 0, MAX_VALUE, ... , MAX_VALUE ]
        // coins [ 1, 5, 10, 25 ]
        // amount: 50

        // amount = 1
        //      coin = 1    1<=1 => dp[1] = min(dp[1-1] + 1, dp[1]) = min(0+1,MAX_VALUE) = 1
        //      coin = 5    false
        //      ...
        // amount = 2
        //      coin = 1    1<=2 => dp[2] = min(dp[2-1] + 1, dp[2]) = min(1+1,MAX_VALUE) = 2
        //      coin = 5    false
        // amount = 5
        //      coin = 1    1<=5 => dp[5] = min(dp[5-1] + 1, dp[5]) = min(4+1,MAX_VALUE) = 5
        //      coin = 5    5<=5 => dp[5] = min(dp[5-5] + 1, dp[5]) = min(0+1,5) = 1
        //      coin = 10   false
        for (amount in 1..amount) {
            for (coin in coins) {
                if (coin <= amount && dp[amount - coin] != Int.MAX_VALUE) {
                    dp[amount] = min(dp[amount - coin] + 1, dp[amount])
                }
            }
        }

        return if (dp[amount] == Int.MAX_VALUE) -1 else dp[amount]
    }
}