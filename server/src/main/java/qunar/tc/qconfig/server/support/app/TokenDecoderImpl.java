package qunar.tc.qconfig.server.support.app;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.TokenUtil;

/**
 * Created by pingyang.yang on 2018/11/14
 */
@Service
public class TokenDecoderImpl implements TokenDecoder {

    @Override
    public String decodeToken(String token) {
        return TokenUtil.decode(token);
    }

    public static void main(String[] args) {
        String src = "qconfig_test/qconfig";
        System.out.println(TokenUtil.encode("b_qconfig_test"));
    }

}
