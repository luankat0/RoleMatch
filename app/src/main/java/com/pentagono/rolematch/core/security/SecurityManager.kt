package com.pentagono.rolematch.core.security

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Client-side security helpers.
 *
 * NOTE: All security-critical validation MUST also be done on the backend
 * (Firebase Cloud Functions / Firestore Rules). This class provides a
 * first-layer defence only.
 */
object SecurityManager {

    // ── Rate Limiting ─────────────────────────────────────────────────────────

    private data class RateLimitEntry(
        val count: AtomicInteger = AtomicInteger(0),
        val windowStart: Long = System.currentTimeMillis()
    )

    private val loginAttempts = ConcurrentHashMap<String, RateLimitEntry>()

    private const val MAX_LOGIN_ATTEMPTS = 5
    private const val LOCKOUT_WINDOW_MS = 15 * 60 * 1000L  // 15 minutes

    /**
     * Returns true if the email/IP is allowed to attempt login.
     * Returns false (locked out) after MAX_LOGIN_ATTEMPTS within the window.
     */
    fun canAttemptLogin(identifier: String): Boolean {
        val now = System.currentTimeMillis()
        val entry = loginAttempts.getOrPut(identifier) { RateLimitEntry() }

        // Reset window if expired
        if (now - entry.windowStart > LOCKOUT_WINDOW_MS) {
            loginAttempts[identifier] = RateLimitEntry()
            return true
        }

        return entry.count.get() < MAX_LOGIN_ATTEMPTS
    }

    /** Call on every failed login attempt. */
    fun recordFailedLogin(identifier: String) {
        val entry = loginAttempts.getOrPut(identifier) { RateLimitEntry() }
        entry.count.incrementAndGet()
    }

    /** Call on successful login to clear the counter. */
    fun clearLoginAttempts(identifier: String) {
        loginAttempts.remove(identifier)
    }

    /** Returns remaining lockout time in minutes, or 0 if not locked. */
    fun getLockoutMinutes(identifier: String): Int {
        val entry = loginAttempts[identifier] ?: return 0
        if (entry.count.get() < MAX_LOGIN_ATTEMPTS) return 0
        val elapsed = System.currentTimeMillis() - entry.windowStart
        val remaining = LOCKOUT_WINDOW_MS - elapsed
        return if (remaining <= 0) 0 else (remaining / 60_000).toInt() + 1
    }

    // ── Input Sanitisation ────────────────────────────────────────────────────

    private val SCRIPT_PATTERN = Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE)
    private val HTML_TAG_PATTERN = Regex("<[^>]+>")
    private val SQL_INJECTION_PATTERN = Regex("(['\";]|--|\\/\\*|\\*\\/|xp_)", RegexOption.IGNORE_CASE)

    /** Strip HTML tags and obvious injection patterns from user input. */
    fun sanitize(input: String): String = input
        .replace(SCRIPT_PATTERN, "")
        .replace(HTML_TAG_PATTERN, "")
        .trim()

    /** Returns true if input contains suspicious SQL/script patterns. */
    fun isMalicious(input: String): Boolean =
        SQL_INJECTION_PATTERN.containsMatchIn(input) ||
        SCRIPT_PATTERN.containsMatchIn(input)

    // ── Session ───────────────────────────────────────────────────────────────

    private var sessionStart: Long = 0L
    private const val SESSION_TIMEOUT_MS = 8 * 60 * 60 * 1000L // 8 hours

    fun startSession() { sessionStart = System.currentTimeMillis() }

    fun isSessionValid(): Boolean {
        if (sessionStart == 0L) return false
        return System.currentTimeMillis() - sessionStart < SESSION_TIMEOUT_MS
    }

    fun invalidateSession() { sessionStart = 0L }

    // ── Audit Log (in-memory, send to Firestore/Cloud Function in prod) ───────

    data class SecurityEvent(
        val type: String,
        val userId: String,
        val metadata: Map<String, String>,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val securityLog = mutableListOf<SecurityEvent>()

    fun logEvent(type: String, userId: String, metadata: Map<String, String> = emptyMap()) {
        securityLog.add(SecurityEvent(type, userId, metadata))
        // TODO: forward to Firestore adminLogs collection via Cloud Function
    }

    fun getRecentEvents(limit: Int = 50) = securityLog.takeLast(limit)
}
