package qunar.tc.qconfig.server.web;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * User: zhaohuiyu
 * Date: 5/21/14
 * Time: 12:33 PM
 */
public class AccessLogV1Filter extends AbstractAccessLogFilter {

    private QFileFactory qFileFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        this.qFileFactory = (QFileFactory) context.getBean("v1Factory");
    }

    @Override
    public QFileFactory getQFileFactory() {
        return qFileFactory;
    }
}
