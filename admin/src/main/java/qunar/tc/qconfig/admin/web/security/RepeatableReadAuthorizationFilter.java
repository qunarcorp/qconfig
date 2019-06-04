package qunar.tc.qconfig.admin.web.security;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import qunar.tc.qconfig.admin.support.AdminConstants;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 13:20
 */
public class RepeatableReadAuthorizationFilter extends AuthorizationFilter {

    @Override
    protected ServletRequest preProcessRequest(ServletRequest request) {
        try {
            return new BodyReaderHttpServletRequestWrapper((HttpServletRequest) request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 上传文件CommonsMultipartResolver的resolveLazily设置为true时，spring异常处理器抓不到MaxUploadSizeExceededException异常
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            super.doFilter(request, response, chain);
        } catch (ServletException e) {
            if (e.getCause() instanceof MaxUploadSizeExceededException) {
                response.setContentType("application/json");
                response.setCharacterEncoding("utf8");
                response.getWriter().println("{\"message\":\"文件不能超过" + AdminConstants.MAX_FILE_SIZE_IN_K + "k\",\"data\":null,\"status\":1}");
            } else {
                throw e;
            }
        }
    }
}
