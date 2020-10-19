package neerajm.configprovider;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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
    private sun.misc.BASE64Decoder base64decoder;
    private final String ENCRYPT_FUNCTION="CPE";
    private Logger mLogger;

    Encryptor() throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        mLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        String SECRET_KEY = "202007280144";
        DESKeySpec keySpec = new DESKeySpec(SECRET_KEY.getBytes("UTF8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        key = keyFactory.generateSecret(keySpec);
        base64decoder = new BASE64Decoder();
    }

    String encrypt(String plainText) {
        try {
            sun.misc.BASE64Encoder base64encoder = new BASE64Encoder();
            Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return String.format("%s(%s)",ENCRYPT_FUNCTION,base64encoder.encode(cipher.doFinal(plainText.getBytes())));
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
            byte[] encrypedPwdBytes = base64decoder.decodeBuffer(chiperText);
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
