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
    fun isExpired() = expiresAt.isBefore(Instant.now())
    fun extend(secondsToAdd: Int): Session = copy(expiresAt = expiresAt.plusSeconds(secondsToAdd.toLong()))
}

class TunnelSessionRegistry {
    val sessions = mutableMapOf<String, Session>()

    // Creates and stores a new session. Throws if sessionId already exists. Returns the new Session.
    fun register(sessionId: String, ttlSeconds: Int): Session {
        if (sessions.containsKey(sessionId)) {
            throw IllegalArgumentException("Session already registered: $sessionId")
        }

        sessions[sessionId] = Session(
            id = sessionId,
            expiresAt = Instant.now().plusSeconds(ttlSeconds.toLong())
        )

        return sessions[sessionId]!!
    }

    // Returns session if it exists and has not expired. Returns null if missing or expired.
    // Does NOT side-effect on expiry.
    fun lookup(sessionId: String): Session? {
        return sessions[sessionId].takeIf { it?.isExpired() == false }
    }

    // Extends the TTL of an existing, non-expired session. Throws if session is missing or already expired.
    fun renew(sessionId: String, additionalSeconds: Int): Session {
        if (sessions[sessionId] == null || sessions[sessionId]?.isExpired() == true) {
            throw IllegalArgumentException("Session '$sessionId' is not registered or is expired:")
        }

        sessions[sessionId] = sessions[sessionId]!!.extend(additionalSeconds)
        return sessions[sessionId]!!
    }

    // Removes all expired sessions from the registry. Returns the count of sessions removed.
    // Should not affect active sessions.
    fun evictExpired(): Int {
        val expired = sessions.keys.filter { sessions[it]?.isExpired() == true }
        expired.forEach {
            sessions.remove(it)
        }

        return expired.size
    }

    // Returns the number of currently non-expired sessions.
    fun activeCount(): Int {
        return sessions.keys.count { sessions[it]?.isExpired() == false }
    }
}