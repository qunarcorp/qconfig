package qunar.tc.qconfig.admin.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.util.RequestUtil;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class LoginFilter implements Filter {

    private final static String USER_ID_COOKIE_NAME = "qcloud_user_name";
    private final static String USER_TYPE_COOKIE_NAME = "user_type";
    private final static String ACCOUNT_COOKIE_NAME = "qcloud_tenant_id";
    private final static String IGNORE_PATH_NAME = "ignorePath";
    private final static Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    private final static Logger logger = LoggerFactory.getLogger(LoginFilter.class);
    private Set<String> ignorePathPrefix;
    private ObjectMapper objectMapper = new ObjectMapper();

    private UserContextService userContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        userContext = wac.getBean(UserContextService.class);
        Preconditions.checkNotNull(userContext, "null userContext error");
        String ignorePath = filterConfig.getInitParameter(IGNORE_PATH_NAME);
        if (Strings.isNullOrEmpty(ignorePath)) {
            ignorePathPrefix = Sets.newHashSet();
        } else {
            ignorePathPrefix = Sets.newHashSet(SPLITTER.split(ignorePath));
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (shouldIgnore(request)) {
            filterChain.doFilter(request, servletResponse);
            return;
        }

        Account account = extractFromSecurity(request, response);

        try {
            if (isLogin(account)) {
                userContext.setAccount(account);

                String ip = RequestUtil.getRealIP(request);
                userContext.setIp(ip);
                userContext.freshGroupInfos();

                MDC.put(MdcConstants.USER_ID, account.getUserId());
                MDC.put(MdcConstants.IP, ip);


                filterChain.doFilter(request, servletResponse);
            } else {
                handleUnLogin(servletResponse);
            }
        } finally {
            userContext.clear();
        }

    }


    private Account extractFromSecurity(HttpServletRequest request, HttpServletResponse response) {
        Account account = userContext.getAccount();
        if (account == null) {
            account = new Account();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            account.setUserId("admin");
            return account;
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        account.setUserId(userDetails.getUsername());
        return account;
    }


    private boolean shouldIgnore(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String ignorePath : this.ignorePathPrefix) {
            if (uri.startsWith(ignorePath)) {
                return true;
            }
        }
        return false;
    }

    private void handleUnLogin(ServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(new JsonV2<>(3, "not login in", null)));
    }

    private boolean isLogin(Account account) {
        return account != null && StringUtils.isNotBlank(account.getUserId());
    }

    @Override
    public void destroy() {

    }
}