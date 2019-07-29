package ru.alexeyFedechkin.znatoki.studyRussianBot.utils

import mu.KotlinLogging
import org.glassfish.jersey.internal.util.Base64
import ru.alexeyFedechkin.znatoki.studyRussianBot.Config
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

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
        try {
            val messageWithSolid = message + SOLID
            signature = Signature.getInstance("SHA256withRSA")
            var pkcs8Pem = Config.privateKey
            pkcs8Pem = pkcs8Pem.replace("\\s+".toRegex(), "")
            val pkcs8EncodedBytes = Base64.decode(pkcs8Pem.toByteArray(StandardCharsets.UTF_8))
            val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
            val kf = KeyFactory.getInstance("RSA")
            val privateKey = kf.generatePrivate(keySpec)
            signature.initSign(privateKey)
            signature.update(messageWithSolid.toByteArray(StandardCharsets.UTF_8))
            val realSignature = signature.sign()
            val res = base64.encodeAsString(realSignature)
            logger.info("generated key \"$res\"")
            return res
        } catch (e: NoSuchAlgorithmException) {
            logger.warn("generate key fail", e)
        } catch (e: InvalidKeyException) {
            logger.warn("generate key fail", e)
        } catch (e: SignatureException) {
            logger.warn("generate key fail", e)
        } catch (e: InvalidKeySpecException) {
            logger.warn("generate key fail", e)
        }
        return "error"
    }

    /**
     * check validation of key. as message taken userName of telegram user.
     * for encode signature used base64 algorithm.
     * @param userName userName of telegram user taken by message for update signature
     * @param key activated key from user
     * @return true if key is valid
     */
    fun validateKey(userName: String, key: String): Boolean {
        var userName = userName
        try {
            userName += SOLID
            var pkcs8Pem = Config.publicKey
            pkcs8Pem = pkcs8Pem.replace("\\s+".toRegex(), "")
            val pkcs8EncodedBytes = Base64.decode(pkcs8Pem.toByteArray(StandardCharsets.UTF_8))
            val keySpec = X509EncodedKeySpec(pkcs8EncodedBytes)
            val kf = KeyFactory.getInstance("RSA")
            val publicKey = kf.generatePublic(keySpec)
            signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(userName.toByteArray(StandardCharsets.UTF_8))
            val keyBinary = base64.decode(key)
            val isVerify = signature.verify(keyBinary)
            if (isVerify) {
                logger.info("key \"$key\" verify")
            } else {
                logger.info("key don't$key verify")
            }
            return isVerify
        } catch (e: NoSuchAlgorithmException) {
            logger.info("validate key fail", e)
        } catch (e: InvalidKeyException) {
            logger.info("validate key fail", e)
        } catch (e: SignatureException) {
            logger.info("validate key fail", e)
        } catch (e: InvalidKeySpecException) {
            logger.info("validate key fail", e)
        }
        return false
    }
}