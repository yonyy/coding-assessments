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
    fun registerToken(token: String): Boolean { TODO("Not yet implemented") }

    /**
     * Revoke a token. Returns true if it existed, false if not found.
     */
    fun revokeToken(token: String): Boolean { TODO("Not yet implemented") }

    /**
     * Check if token is currently valid (registered and not revoked).
     */
    fun isValid(token: String): Boolean { TODO("Not yet implemented") }

    /**
     * Return all active tokens for a given userId.
     * UserId is extracted as the segment between the first and second underscore.
     */
    fun activeTokensForUser(userId: String): List<String> { TODO("Not yet implemented") }

    private fun extractUserId(token: String): String? { TODO("Not yet implemented") }
}

fun main() {
    // val store = TokenStore()
    // val token1 = "usr_abc123_xK9mP2qL"
    // val badToken1 = "usr_abc123_"
    // val badToken2 = ""
    // val badToken3 = "usr__abc123"

    // println("registerToken: $token1")
    // println(store.registerToken(token1))       // Expected: true

    // println("registerToken: $badToken1")
    // println(store.registerToken(badToken1))    // Expected: false

    // println("isValid: $token1")
    // println(store.isValid(token1))             // Expected: true

    // println("activeTokensForUser: abc123")
    // println(store.activeTokensForUser("abc123"))  // Expected: [usr_abc123_xK9mP2qL]

    // println("revokeToken: $token1")
    // println(store.revokeToken(token1))         // Expected: true

    // println("isValid: $token1")
    // println(store.isValid(token1))             // Expected: false
}
