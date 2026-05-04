package ngrok

import java.time.Instant

// ngrok manages many concurrent tunnel sessions.
// Each session opens a connection between a remote client and a local service, and has a lifecycle:
// it can be created, updated (e.g. metadata changes), and expired.
//
// You're building the in-memory registry that tracks active sessions for a single ngrok agent node.
// The registry must support fast lookup by session ID, and efficient cleanup of expired sessions
// without blocking active ones.
data class Session(
    val id: String,
    val expiresAt: Instant
) {
    fun isExpired(): Boolean { TODO("Not yet implemented") }
    fun extend(secondsToAdd: Int): Session { TODO("Not yet implemented") }
}

class TunnelSessionRegistry {
    val sessions = mutableMapOf<String, Session>()

    // Creates and stores a new session. Throws if sessionId already exists. Returns the new Session.
    fun register(sessionId: String, ttlSeconds: Int): Session { TODO("Not yet implemented") }

    // Returns session if it exists and has not expired. Returns null if missing or expired.
    // Does NOT side-effect on expiry.
    fun lookup(sessionId: String): Session? { TODO("Not yet implemented") }

    // Extends the TTL of an existing, non-expired session. Throws if session is missing or already expired.
    fun renew(sessionId: String, additionalSeconds: Int): Session { TODO("Not yet implemented") }

    // Removes all expired sessions from the registry. Returns the count of sessions removed.
    // Should not affect active sessions.
    fun evictExpired(): Int { TODO("Not yet implemented") }

    // Returns the number of currently non-expired sessions.
    fun activeCount(): Int { TODO("Not yet implemented") }
}
