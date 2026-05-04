package airbnb

import kotlin.math.min

class Solution {
    fun shoppingOffers(price: List<Int>, specials: List<List<Int>>, needs: List<Int>): Int {
        val memo = mutableMapOf<List<Int>, Int>()

        fun dfs(needs: List<Int>): Int {
            if (needs.all { it == 0}) return 0

            memo[needs]?.let { return it }
            var minCost = totalPrice(price, needs)

            for (special in specials) {
                var isValid = true
                var mutableNeeds = needs.toMutableList()

                special.forEachIndexed {idx, _ ->
                    if (idx < needs.size - 1) {
                        // we have enough needs to apply
                        if (special[idx] <= needs[idx]) {
                            // update with remaining
                            mutableNeeds[idx] = needs[idx] - special[idx]
                        } else {
                            isValid = false
                            return@forEachIndexed
                        }
                    }
                }

                if (!isValid) continue
                val priceWithOffer = special.last() + dfs(mutableNeeds)
                minCost = min(minCost, priceWithOffer)
            }

            memo[needs] = minCost
            return minCost
        }

        val strArray = Array(8) { "" }
        return dfs(needs)
    }

    fun totalPrice(price: List<Int>, needs: List<Int>): Int {
        return price.mapIndexed { idx, price ->
            needs[idx] * price
        }.sum()
    }
}