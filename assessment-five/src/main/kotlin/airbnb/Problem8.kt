package airbnb

class Problem8 {
    fun accountsMerge(accounts: List<List<String>>): List<List<String>> {
        val parents = mutableMapOf<String, String>()
        val emailToName = mutableMapOf<String, String>()

        fun find(email: String): String {
            if (parents[email] != email) {
                parents[email] = find(parents[email]!!)
            }

            return parents[email]!!
        }

        fun union(email1: String, email2: String) {
            val ra = find(email1)
            val rb = find(email2)

            parents[rb] = ra
        }

        accounts.forEach { accountInfo ->
            val name = accountInfo[0]
            val firstEmail = accountInfo[1]

            for (i in 1..<accountInfo.size) {
                val email = accountInfo[i]
                if (parents[email] == null) {
                    parents[email] = name   // init
                }
                emailToName[email] = name
                union(firstEmail, email)
            }
        }

        return parents.keys
            .groupBy { find(it) }
            .values
            .map { emails ->
                listOf(emailToName[emails[0]]!!) + emails.sorted()
            }
    }
}