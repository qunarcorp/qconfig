package qunar.tc.qconfig.server.feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2016 2016/9/21 16:16
 */
@Service
public class MockMailParseErrorServiceImpl implements MailParseErrorService {

    private static final Logger logger = LoggerFactory.getLogger(MockMailParseErrorServiceImpl.class);


    @Override
    public void mailParseError(String group, ConfigMeta meta, long version) {
        logger.info("notify parse error");
    }
}
