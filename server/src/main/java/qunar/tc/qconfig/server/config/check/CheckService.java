package qunar.tc.qconfig.server.config.check;

import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;

import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 16:59
 */
public interface CheckService {

    CheckResult check(List<CheckRequest> requests, String ip, QFileFactory qFileFactory);
}
