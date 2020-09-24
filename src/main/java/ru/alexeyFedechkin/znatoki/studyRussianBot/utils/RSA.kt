package ru.alexeyFedechkin.znatoki.studyRussianBot.utils

import mu.KotlinLogging
import ru.alexeyFedechkin.znatoki.studyRussianBot.Config
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 *work with RSA signature
 */
object RSA {
    private val logger = KotlinLogging.logger { }
    private const val SOLID = "aYbWZRqZgBWkxQL2z8Z4kWPBz"
    private var signature: Signature = Signature.getInstance("SHA256withRSA")
    private val base64 = org.apache.commons.codec.binary.Base64()
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
        return MessageDigest.getInstance("SHA-256").digest(s.toByteArray(StandardCharsets.UTF_8)).toString()
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
        if (MessageDigest.getInstance("SHA-256").digest(s.toByteArray(StandardCharsets.UTF_8)).toString() == key) return true;
        return false;
    }
}