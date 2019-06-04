package qunar.tc.qconfig.admin.web.security;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.util.JacksonSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zhenyu.nie created on 2017 2017/10/30 19:59
 */
public class UnLoginHandler implements AuthorizationFilter.UnauthorizedHandler {

    @Override
    public void handle(HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write(JacksonSerializer.getSerializer().serialize(new JsonV2<>(3, "not login in", null)));
    }
}
