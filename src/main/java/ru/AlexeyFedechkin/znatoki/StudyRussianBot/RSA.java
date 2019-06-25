package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.apache.log4j.Logger;
import org.glassfish.jersey.internal.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSA {
    private final Logger logger = Logger.getLogger(RSA.class);
    private final String SOLID = "asdfsdd";
    private Signature signature = null;
    final org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();

    /**
     * @param message first name of telegram user with solid
     * @return base64 encoded signature
     */
    public String generateKey(String message){
        try{
            message += SOLID;
            signature = Signature.getInstance("SHA256withRSA");
            StringBuilder pkcs8Lines = new StringBuilder();
            BufferedReader rdr = new BufferedReader(new StringReader(Config.getInstance().getRsaPrivateKey()));
            String line;
            while ((line = rdr.readLine()) != null) {
                pkcs8Lines.append(line);
            }
            // Remove the "BEGIN" and "END" lines, as well as any whitespace
            String pkcs8Pem = pkcs8Lines.toString();
            pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");
            // Base64 decode the result
            byte [] pkcs8EncodedBytes = Base64.decode(pkcs8Pem.getBytes(StandardCharsets.UTF_8));
            // extract the private key
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(keySpec);
            signature.initSign(privKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            var realSignature = signature.sign();
            String res = base64.encodeAsString(realSignature);
            logger.info("generated key " + res);
            return res;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            logger.fatal("generate key fail", e);
        }
        return "error";
    }

    /**
     * @param firstName message to sight signature
     * @param key activated key from user
     * @return true if key is valid
     */
    public boolean validateKey(String firstName, String key){
        try{
            firstName += SOLID;
            StringBuilder pkcs8Lines = new StringBuilder();
            BufferedReader rdr = new BufferedReader(new StringReader(Config.getInstance().getRsaPublicKey()));
            String line;
            while ((line = rdr.readLine()) != null) {
                pkcs8Lines.append(line);
            }
            String pkcs8Pem = pkcs8Lines.toString();
            pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");
            byte [] pkcs8EncodedBytes = Base64.decode(pkcs8Pem.getBytes(StandardCharsets.UTF_8));

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            PublicKey publicKey = kf.generatePublic(keySpec);
            signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(firstName.getBytes(StandardCharsets.UTF_8));
            byte[] keyBinary = base64.decode(key);
            boolean isVerify = signature.verify(keyBinary);
            if (isVerify){
                logger.info("key "  + key + " verify");
            } else {
                logger.info("key don't"  + key + " verify");
            }
            return isVerify;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            logger.info("validate key fail", e);
        }
        return false;
    }
}
