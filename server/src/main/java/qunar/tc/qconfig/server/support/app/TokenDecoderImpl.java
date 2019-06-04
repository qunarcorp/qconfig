package qunar.tc.qconfig.server.support.app;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.TokenUtil;

/**
 * Created by pingyang.yang on 2018/11/14
 */
@Service
public class TokenDecoderImpl implements TokenDecoder {

    static byte[] key1 = new byte[]{120, 36, -88, 29, -96, 57, -119, -128, 78, 123, -87, -33, 72, 96, 55, -83};

    // TODO: 2018/11/14 添加实现
    @Override
    public String decodeToken(String token) {
        return TokenUtil.decode(token);
    }

    public static void main(String[] args) {
        String src = "qconfig_test/qconfig";
        System.out.println(TokenUtil.encode("b_qconfig_test"));
    }

}
