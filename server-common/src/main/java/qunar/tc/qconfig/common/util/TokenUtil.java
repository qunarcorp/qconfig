package qunar.tc.qconfig.common.util;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * Created by pingyang.yang on 2018/11/15
 */
public class TokenUtil {

    private static Logger logger = LoggerFactory.getLogger(TokenUtil.class);

    private static byte[] key1 = new byte[]{120, 36, -88, 29, -96, 57, -119, -128, 78, 123, -87, -33, 72, 96, 55, -83};

    private static Key key2 = new SecretKeySpec(key1, "AES");

    private static Cipher cipher;
    private static Cipher decodeCipher;

    static {
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5padding");
            cipher.init(Cipher.ENCRYPT_MODE, key2);

            decodeCipher = Cipher.getInstance("AES/ECB/PKCS5padding");
            decodeCipher.init(Cipher.DECRYPT_MODE, key2);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String decode(String token) {
        try {
            return new String(decodeCipher.doFinal(Hex.decodeHex(token.toCharArray())));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static String encode(String group) {

        try {
            byte[] result = cipher.doFinal(group.getBytes());
            return Hex.encodeHexString(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
