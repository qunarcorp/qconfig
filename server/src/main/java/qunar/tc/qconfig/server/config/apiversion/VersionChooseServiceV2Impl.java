package qunar.tc.qconfig.server.config.apiversion;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2017 2017/3/29 18:09
 */
@Service("v2")
public class VersionChooseServiceV2Impl implements VersionChooseService {

    @Resource(name = "v2Factory")
    private QFileFactory qFileFactory;

    @Resource(name = "v2Parser")
    private RequestParser parser;

    @Override
    public QFileFactory getQFileFactory() {
        return qFileFactory;
    }

    @Override
    public RequestParser getRequestParser() {
        return parser;
    }
}
