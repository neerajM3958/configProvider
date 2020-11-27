package neerajm.configprovider;

import ch.qos.logback.classic.Logger;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

class Encryptor {
    private SecretKey key;
    private final String ENCRYPT_FUNCTION="CPE";
    private Logger mLogger;

    Encryptor() throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        mLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        String SECRET_KEY = "202007280144";
        DESKeySpec keySpec = new DESKeySpec(SECRET_KEY.getBytes("UTF8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        key = keyFactory.generateSecret(keySpec);
    }

    String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return String.format("%s(%s)",ENCRYPT_FUNCTION,new String(Base64.encodeBase64(cipher.doFinal(plainText.getBytes()))));
        } catch (Exception e) {
            mLogger.info("failed to encypt - " + plainText);
            mLogger.debug(e.toString());
        }
        return plainText;
    }

    String decrypt(String chiperText) {
        if(chiperText.startsWith(ENCRYPT_FUNCTION))
            chiperText = chiperText.substring(ENCRYPT_FUNCTION.length()+1,chiperText.length()-1);
        else return chiperText;
        try {
            byte[] encrypedPwdBytes = Base64.decodeBase64(chiperText.getBytes());
            Cipher cipher2 = Cipher.getInstance("DES");// cipher is not thread safe
            cipher2.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher2.doFinal(encrypedPwdBytes));
        } catch (Exception e) {
            mLogger.info("failed to decrypt - " + chiperText);
            mLogger.debug(e.toString());
        }
        return chiperText;
    }
}
