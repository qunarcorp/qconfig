package qunar.tc.qconfig.common.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Created by pingyang.yang on 2018/11/15
 */
public class TokenUtil {

    private static Logger logger = LoggerFactory.getLogger(TokenUtil.class);

    private static Key publicKey;

    private static Key privateKey;

    private static Base64.Decoder decoder = Base64.getDecoder();

    private static Base64.Encoder encoder = Base64.getEncoder();

    private static ThreadLocal<Cipher> CIPHER = new ThreadLocal<Cipher>() {
        @Override
        protected Cipher initialValue() {
            try {
                return Cipher.getInstance("RSA");
            } catch (Exception e) {
                throw new RuntimeException("get RSA Instance Fail");
            }
        }
    };

    private static ThreadLocal<Cipher> DECODE_CIPHER = new ThreadLocal<Cipher>() {
        @Override
        protected Cipher initialValue() {
            try {
                return Cipher.getInstance("RSA");
            } catch (Exception e) {
                throw new RuntimeException("get RSA Instance Fail");
            }
        }
    };

    static {
        try {
            loadKey();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String decode(String token) {
        try {
            Cipher decodeCipher = DECODE_CIPHER.get();
            decodeCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(decodeCipher.doFinal(decoder.decode(token)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static String encode(String group) {

        try {
            Cipher cipher = CIPHER.get();
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] result = cipher.doFinal(group.getBytes());
            return encoder.encodeToString(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static void loadKey() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        byte[] privateKeyByte = getBytes("QConfig");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByte);
        privateKey = keyFactory.generatePrivate(keySpec);

        byte[] publicKeyByte = getBytes("QConfig.pub");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyByte);
        publicKey = keyFactory.generatePublic(publicKeySpec);
    }

    private static byte[] getBytes(String path) throws IOException {
        System.out.println(new ClassPathResource(path));
        File in = new ClassPathResource(path).getFile();
        String keyWord = Files.asCharSource(in, Charsets.UTF_8).read();
        return Base64.getDecoder().decode(keyWord);
    }

    public static void main(String[] args) {
        System.out.println(encode("qconfig"));
        System.out.println(decode(encode("qconfig ")));
        String a = "iaH6P5AMUHOjgCb2e5yLYaIHxEn8ptneJdyISxWdNIIKa1hi1o03+U5zQObSzFMDA7NXaw1asyPMIn2dwWvZn6rO+VWKGAdzilzj3K2YZs2fAD5BE61HgRN0pf8gVICzqClJ81+d9aZJ05X+3ZNWKNh70gKtcd+YKMgW6CGidHc=";
    }
}
