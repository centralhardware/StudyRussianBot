package me.centralhardware.znatoki.studyRussianBot.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

/**
 *work with RSA signature
 */
object RSA {
    private const val SOLID = "aYbWZRqZgBWkxQL2z8Z4kWPBz"
    /**
     * generate activated code.
     * hash username + SOLID
     * @param message first name of telegram user with solid
     * @return base64 encoded signature
     */
    fun generateKey(message: String): String {
        val s = message + SOLID
        return Base64.getEncoder().encodeToString( MessageDigest.getInstance("SHA-256").digest(s.toByteArray(StandardCharsets.UTF_8)))
    }

    /**
     * check validation of key. as message taken userName of telegram user.
     * @param userName userName of telegram user taken by message for update signature
     * @param key activated key from user
     * @return true if key is valid
     */
    fun validateKey(userName: String, key: String): Boolean {
        val s = userName + SOLID
        return Base64.getEncoder().encodeToString( MessageDigest.getInstance("SHA-256").digest(s.toByteArray(StandardCharsets.UTF_8))) == key
    }
}