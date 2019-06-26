package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import org.glassfish.jersey.internal.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * class for working with asymmetric encryption
 * used to generate activation codes and checking them
 */
public class RSA {
    private final Logger logger = Logger.getLogger(RSA.class);
    private final String SOLID = "asdfsdd";
    private Signature signature = null;
    private final org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();

    /**
     * generate activated code.
     * sign signature with giving message (userName of telegram user).
     * for sign using private key.
     * in this case it does not matter which key to sign as both keys are stored
     * on the server and are not accessible from the outside
     * @param message first name of telegram user with solid
     * @return base64 encoded signature
     */
    public String generateKey(String message){
        try{
            message += SOLID;
            signature = Signature.getInstance("SHA256withRSA");
            String pkcs8Pem = Config.getInstance().getRsaPrivateKey();
            pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");
            byte [] pkcs8EncodedBytes = Base64.decode(pkcs8Pem.getBytes(StandardCharsets.UTF_8));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(keySpec);
            signature.initSign(privKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            var realSignature = signature.sign();
            String res = base64.encodeAsString(realSignature);
            logger.info("generated key " + res);
            return res;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            logger.fatal("generate key fail", e);
        }
        return "error";
    }

    /**
     * check validation of key. as message taken userName of telegram user.
     * for encode signature used base64 algorithm.
     * @param userName userName of telegram user taken by message for update signature
     * @param key activated key from user
     * @return true if key is valid
     */
    public boolean validateKey(String userName, String key) {
        try{
            userName += SOLID;
            String pkcs8Pem = Config.getInstance().getRsaPublicKey();
            pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");
            byte [] pkcs8EncodedBytes = Base64.decode(pkcs8Pem.getBytes(StandardCharsets.UTF_8));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(keySpec);
            signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] keyBinary = base64.decode(key);
            boolean isVerify = signature.verify(keyBinary);
            if (isVerify){
                logger.info("key "  + key + " verify");
            } else {
                logger.info("key don't"  + key + " verify");
            }
            return isVerify;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            logger.info("validate key fail", e);
        }
        return false;
    }
}
