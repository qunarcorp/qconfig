package qunar.tc.qconfig.admin.web.security;


import com.google.common.collect.ImmutableSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class RedirectFilter implements Filter {

    private Set<String> uriPrefixSet = ImmutableSet.of("/view/", "/home");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (needRedirect(request)) {
            response.sendRedirect("/");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean needRedirect(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String prefix : uriPrefixSet) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void destroy() {

    }

}
