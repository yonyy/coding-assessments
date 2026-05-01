package ngrok

/**
 * ngrok authTokens look like: "<userId>_<randomSuffix>"
 * Example: "usr_abc123_xK9mP2qL"
 *
 * Implement a token store and validator.
 */
data class ParsedToken(
    val userId: String,
    val token: String,
) {
    companion object {
        val REGEX = "^usr_[a-zA-Z0-9]+_[a-zA-Z0-9]{6,}$".toRegex()

        fun from(token: String): ParsedToken? {
            if (!token.matches(REGEX)) {
                return null
            }

            val segments = token.split("_")
            val userId = segments[1]

            return ParsedToken(
                userId = userId,
                token = token,
            )
        }
    }
}

class TokenStore {
    private val userTokens = mutableMapOf<String, MutableSet<String>>()

    /**
     * Register a token. Returns false if token format is invalid.
     * Valid format: starts with "usr_", followed by at least one alphanumeric userId segment,
     * underscore, then at least 6 alphanumeric characters.
     */
    fun registerToken(token: String): Boolean {
        val parsedToken = ParsedToken.from(token) ?: return false

        userTokens.getOrPut(parsedToken.userId) { mutableSetOf() }
            .add(token)
        return true
    }

    /**
     * Revoke a token. Returns true if it existed, false if not found.
     */
    fun revokeToken(token: String): Boolean {
        val parsedToken = ParsedToken.from(token) ?: return false

        return userTokens[parsedToken.userId]?.remove(token) == true
    }

    /**
     * Check if token is currently valid (registered and not revoked).
     */
    fun isValid(token: String): Boolean {
        val parsedToken = ParsedToken.from(token) ?: return false

        return userTokens[parsedToken.userId]?.contains(token) == true
    }

    /**
     * Return all active tokens for a given userId.
     * UserId is extracted as the segment between the first and second underscore.
     */
    fun activeTokensForUser(userId: String): List<String> {
        return userTokens[userId]?.toList() ?: emptyList()
    }
}