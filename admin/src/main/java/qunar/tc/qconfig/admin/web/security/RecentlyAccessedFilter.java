package qunar.tc.qconfig.admin.web.security;


import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecentlyAccessedFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(RecentlyAccessedFilter.class);
    private final static Splitter SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();
    private final static Joiner JOINER = Joiner.on(",").skipNulls();
    private final static String COOKIE_PATH = "/qconfig";
    public final static String COOKIE_KEY_RECENTLY_ACCESSED_GROUPS = "recentlyAccessedGroups";
    private final static int RECENTLY_ACCESSED_GROUPS_MAX_SIZE = 10;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Optional<String> currentGroup = getCurrentRequestGroup(request);
        if (currentGroup.isPresent()) {
            logger.debug("recent groups filter, uri:{}, group:{}", request.getRequestURI(), currentGroup.get());
            Cookie[] cookies = request.getCookies();
            List<String> recentGroups = new ArrayList<>();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (COOKIE_KEY_RECENTLY_ACCESSED_GROUPS.equals(cookie.getName())) {
                        recentGroups = decodeRecentlyAccessedGroups(cookie.getValue());
                        break;
                    }
                }
            }
            List<String> updatedRecentGroups = updateRecentGroups(recentGroups, currentGroup.get());
            String encodedCookie = BaseEncoding.base64Url().omitPadding().encode(JOINER.join(updatedRecentGroups).getBytes(Charsets.UTF_8));
            Cookie updatedCookie = new Cookie(COOKIE_KEY_RECENTLY_ACCESSED_GROUPS, encodedCookie);
            updatedCookie.setPath(COOKIE_PATH);
            response.addCookie(updatedCookie);
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    private List<String> decodeRecentlyAccessedGroups(String encoded) {
        try {
            String value = new String(BaseEncoding.base64Url().omitPadding().decode(encoded), Charsets.UTF_8);
            return SPLITTER.splitToList(value);
        } catch (Exception e) {
            return ImmutableList.of();
        }
    }

    private Optional<String> getCurrentRequestGroup(HttpServletRequest request) {
        String uri = uriOf(request);
        String group = null;
        if (uri.startsWith("/qconfig/file/list")) {
            group = Strings.emptyToNull(request.getParameter("group"));
        }
        return Optional.ofNullable(group);
    }

    private List<String> updateRecentGroups(List<String> old, String currentGroup) {
        if (Strings.isNullOrEmpty(currentGroup)) {
            return old;
        }
        List<String> updated = Lists.newArrayList();
        updated.add(currentGroup);
        if (!CollectionUtils.isEmpty(old)) {
            for (String group : old) {
                if (Objects.equals(group, currentGroup)) {
                    continue;
                }
                updated.add(group);
                if (updated.size() >= RECENTLY_ACCESSED_GROUPS_MAX_SIZE) {
                    break;
                }
            }
        }
        return updated;
    }

    private String uriOf(HttpServletRequest request) {
        return request.getRequestURI().replaceFirst(request.getContextPath(), "");
    }
}
