package qunar.tc.qconfig.server.web;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 13:41
 */
public class AccessLogV2Filter extends AbstractAccessLogFilter {

    private QFileFactory qFileFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        if (context == null) {
            throw new ServletException("init failed");
        }

        this.qFileFactory = (QFileFactory) context.getBean("v2Factory");
    }

    @Override
    public QFileFactory getQFileFactory() {
        return qFileFactory;
    }
}
