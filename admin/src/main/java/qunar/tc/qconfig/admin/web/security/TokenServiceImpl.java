package qunar.tc.qconfig.admin.web.security;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.TokenUtil;

/**
 * User: zhaohuiyu
 * Date: 5/20/14
 * Time: 6:50 PM
 */
@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Override
    public String decode(String token) {

        logger.debug("decode app server, token={}", token);
        if (!Strings.isNullOrEmpty(token)) return TokenUtil.decode(token);
        return null;
    }

}
