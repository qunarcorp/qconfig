package qunar.tc.qconfig.admin.web.security;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ning.http.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.servercommon.service.EnvironmentMappingService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.crypto.Cipher;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.Key;
import java.util.Iterator;

/**
 * User: zhaohuiyu
 * Date: 5/20/14
 * Time: 6:50 PM
 */
@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final char STX = (char) 2; // start text
    private static final char ETX = (char) 3; // end text
    private static final char FF = '\r';
    private static final char CR = '\n';

    private static final Splitter SPLITTER = Splitter.on(CR).trimResults();

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

    @Resource
    private EnvironmentMappingService environmentMappingService;

    // TODO: 2019-05-09 这里的实现
    @Override
    public App decode(String token) {

        logger.debug("decode app server, token={}", token);
        if (Strings.isNullOrEmpty(token)) return null;

        try {
//            AppServer appServer = ServerManager.getInstance().decodeAppServer(token);
////            String mappedEnv = environmentMappingService.getMappedEnv(appServer.getEnv());
////            return new App(appServer.getApp(), mappedEnv, appServer.getIp());
            return null;
        } catch (Exception e) {
            throw new RuntimeException("解密应用配置失败, token=" + token, e);
        }
    }

    private Key privateKey;

    @PostConstruct
    public void init() throws Exception {
        privateKey = loadKey("RSAPrivateKey");
    }

    private Key loadKey(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream is = new ObjectInputStream(new ClassPathResource(path).getInputStream())) {
            return (Key) is.readObject();
        }
    }

    private String decrypt(String secret) throws Exception {
        byte[] bytes = decryptBASE64(secret.replace(STX, FF).replace(ETX, CR));

        Cipher cipher = CIPHER.get();
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(cipher.doFinal(bytes), "UTF-8");
    }

    private static byte[] decryptBASE64(String key) throws Exception {
        return Base64.decode(key);
    }
}
