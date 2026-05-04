package ngrok

/**
 * ngrok authTokens look like: "<userId>_<randomSuffix>"
 * Example: "usr_abc123_xK9mP2qL"
 *
 * Implement a token store and validator.
 */
class TokenStore {
    val tokens = mutableMapOf<String, MutableSet<String>>()
    /**
     * Register a token. Returns false if token format is invalid.
     * Valid format: starts with "usr_", followed by at least one alphanumeric userId segment,
     * underscore, then at least 6 alphanumeric characters.
     */
    fun registerToken(token: String): Boolean {
        val userId = extractUserId(token) ?: return false
        return tokens.getOrPut(userId) { mutableSetOf() }.add(token)
    }

    /**
     * Revoke a token. Returns true if it existed, false if not found.
     */
    fun revokeToken(token: String): Boolean {
        val userId = extractUserId(token) ?: return false
        return tokens[userId]?.remove(token) ?: false
    }

    /**
     * Check if token is currently valid (registered and not revoked).
     */
    fun isValid(token: String): Boolean {
        val userId = extractUserId(token) ?: return false
        return tokens[userId]?.contains(token) ?: false
    }

    /**
     * Return all active tokens for a given userId.
     * UserId is extracted as the segment between the first and second underscore.
     */
    fun activeTokensForUser(userId: String): List<String> {
        return tokens[userId]?.toList() ?: emptyList()
    }

    private fun extractUserId(token: String): String? {
        val regex = "^usr_[a-zA-Z0-9]+_[a-zA-Z0-9]{6,}$".toRegex()
        if (!token.matches(regex)) {
            return null
        }

        val segments = token.split("_")
        return segments[1]
    }
}

fun main() {
    val store = TokenStore()
    val token1 = "usr_abc123_xK9mP2qL"
    val badToken1 = "usr_abc123_"
    val badToken2 = ""
    val badToken3 = "usr__abc123"

    println("registerToken: $token1")
    println(store.registerToken(token1))

    println("registerToken: $badToken1")
    println(store.registerToken(badToken1))

    println("registerToken: $badToken2")
    println(store.registerToken(badToken2))

    println("registerToken: $badToken3")
    println(store.registerToken(badToken3))

    println("isValid: $token1")
    println(store.isValid(token1))

    println("isValid: $badToken3")
    println(store.isValid(badToken3))

    println("activeTokensForUser: abc123")
    println(store.activeTokensForUser("abc123"))

    println("revokeToken: $token1")
    println(store.revokeToken(token1))

    println("revokeToken: $badToken3")
    println(store.revokeToken(badToken3))

    println("isValid: $token1")
    println(store.isValid(token1))

    println("activeTokensForUser: abc123")
    println(store.activeTokensForUser("abc123"))
}