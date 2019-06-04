package qunar.tc.qconfig.server.config.apiversion;

import qunar.tc.qconfig.server.domain.CheckRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/3/29 17:03
 */
public interface RequestParser {

    List<CheckRequest> parse(HttpServletRequest req) throws IOException;
}
