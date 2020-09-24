package ru.alexeyFedechkin.znatoki.studyRussianBot.utils

import mu.KotlinLogging
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.Signature
import java.util.*

/**
 *work with RSA signature
 */
object RSA {
    private val logger = KotlinLogging.logger { }
    private const val SOLID = "aYbWZRqZgBWkxQL2z8Z4kWPBz"
    /**
     * generate activated code.
     * sign signature with giving message (userName of telegram user).
     * for sign using private key.
     * in this case it does not matter which key to sign as both keys are stored
     * on the server and are not accessible from the outside
     * @param message first name of telegram user with solid
     * @return base64 encoded signature
     */
    fun generateKey(message: String): String {
        var s = message + SOLID
        return Base64.getEncoder().encodeToString( MessageDigest.getInstance("SHA-256").digest(s.toByteArray(StandardCharsets.UTF_8)))
    }

    /**
     * check validation of key. as message taken userName of telegram user.
     * for encode signature used base64 algorithm.
     * @param userName userName of telegram user taken by message for update signature
     * @param key activated key from user
     * @return true if key is valid
     */
    fun validateKey(userName: String, key: String): Boolean {
        var s = userName + SOLID
        if (Base64.getEncoder().encodeToString( MessageDigest.getInstance("SHA-256").digest(s.toByteArray(StandardCharsets.UTF_8))) == key) return true;
        return false;
    }
}