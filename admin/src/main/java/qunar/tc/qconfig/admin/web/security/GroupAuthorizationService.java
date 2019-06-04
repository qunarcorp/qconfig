package qunar.tc.qconfig.admin.web.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.TypedConfig;
import qunar.tc.qconfig.common.support.AuthorizationControl;
import qunar.tc.qconfig.common.support.json.JsonMapper;
import qunar.tc.qconfig.common.support.json.MapperBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 10:30
 */
@Service("groupAuthorizationService")
public class GroupAuthorizationService extends AbstractAuthorizationService implements AuthorizationControl {

    private static Logger logger = LoggerFactory.getLogger(GroupAuthorizationService.class);

    @Resource(name = "eventPostPermissionService")
    private PermissionService permissionService;

    @Resource
    private UserContextService userContextService;

    private static final String VIEW_PREFIX = "/view";
    private static final String EDIT_PREFIX = "/edit";
    private static final String CANCEL_PREFIX = "/cancel";
    private static final String PUSH_PREFIX = "/push";
    private static final String APPROVE_PREFIX = "/approve";
    private static final String REJECT_PREFIX = "/reject";
    private static final String PUBLISH_PREFIX = "/publish";
    private static final String LEADER_PREFIX = "/leader";
    private static final String UPLOAD_PREFIX = "/upload";
    private static final String ONE_BUTTON_PUBLISH_PREFIX = "/oneButtonPublish";
    private static final String DELETE_PREFIX = "/delete";
    private static final String CHECK_PREFIX = "/check";
    private static final String OPERATE_PREFIX = "/operate";

    private static volatile Set<String> limitOperateApps = ImmutableSet.of();

    static {
        TypedConfig<String> config = TypedConfig.get("limit-operate-apps", Feature.create().setFailOnNotExists(false).build(), TypedConfig.STRING_PARSER);
        config.current();
        config.addListener(new Configuration.ConfigListener<String>() {
            @Override
            public void onLoad(String conf) {
                try {
                    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
                    for (String app : CharSource.wrap(Strings.nullToEmpty(conf)).readLines()) {
                        app = app.trim();
                        if (!Strings.isNullOrEmpty(app)) {
                            builder.add(app);
                        }
                    }
                    limitOperateApps = builder.build();
                    logger.info("limit operate apps: {}", limitOperateApps);
                } catch (IOException e) {
                    logger.error("unexpected error, {}", conf, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // 对于List<PermissionType>，表示必须要满足PermissionType中的任意一个；
    // List<List<PermissionType>>表示每个List<PermittionType>都要满足
    @SuppressWarnings("all")
    private static final Map<String, List<List<PermissionType>>> CHECK_REQUIRED = new HashMap<String, List<List<PermissionType>>>() {
        {
            put(VIEW_PREFIX, Arrays.asList(Arrays.asList(PermissionType.VIEW)));
            put(EDIT_PREFIX, Arrays.asList(Arrays.asList(PermissionType.EDIT)));
            put(UPLOAD_PREFIX, Arrays.asList(Arrays.asList(PermissionType.EDIT)));
            put(APPROVE_PREFIX, Arrays.asList(Arrays.asList(PermissionType.APPROVE)));
            put(CHECK_PREFIX, Arrays.asList(Arrays.asList(PermissionType.EDIT, PermissionType.APPROVE, PermissionType.PUBLISH)));
            put(REJECT_PREFIX, Arrays.asList(Arrays.asList(PermissionType.APPROVE)));
            put(CANCEL_PREFIX, Arrays.asList(Arrays.asList(PermissionType.APPROVE, PermissionType.PUBLISH)));
            put(PUBLISH_PREFIX, Arrays.asList(Arrays.asList(PermissionType.PUBLISH)));
            put(PUSH_PREFIX, Arrays.asList(Arrays.asList(PermissionType.PUBLISH)));
            put(LEADER_PREFIX, Arrays.asList(Arrays.asList(PermissionType.LEADER)));
            put(DELETE_PREFIX, Arrays.asList(Arrays.asList(PermissionType.LEADER)));
            put(OPERATE_PREFIX, Arrays.asList(Arrays.asList(PermissionType.EDIT, PermissionType.APPROVE, PermissionType.PUBLISH)));
            put(ONE_BUTTON_PUBLISH_PREFIX, Arrays.asList(
                    Arrays.asList(PermissionType.EDIT),
                    Arrays.asList(PermissionType.APPROVE),
                    Arrays.asList(PermissionType.PUBLISH)));
        }
    };

    private static final Set<String> MODIFY_URI_PREFIXES = ImmutableSet.of(EDIT_PREFIX, APPROVE_PREFIX, PUBLISH_PREFIX,
            REJECT_PREFIX, CANCEL_PREFIX, PUSH_PREFIX, DELETE_PREFIX, OPERATE_PREFIX, UPLOAD_PREFIX, ONE_BUTTON_PUBLISH_PREFIX);

    @Override
    public boolean isCheckRequired(HttpServletRequest request) {
        return getPermissionNeeded(uriOf(request)) != null;
    }

    private final JsonMapper mapper = MapperBuilder.create().build();

    @Override
    public boolean isAuthorized(final HttpServletRequest req) {
        HttpServletRequest request = req;
        if (userContextService.isAdmin()) {
            return true;
        }

        String group = request.getParameter("group");
        String profile = request.getParameter("profile");
        String dataId = request.getParameter("dataId");

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("json")) {
            try {
                Map<String, Object> map = mapper.readValue(new InputStreamReader(req.getInputStream(), "utf8"),
                        new TypeReference<Map<String, Object>>() {
                        });

                String jsonGroup = (String) map.get("group");
                String jsonProfile = (String) map.get("profile");
                String jsonDataId = (String) map.get("dataId");

                if (!isLegal(group, profile, dataId, jsonGroup, jsonProfile, jsonDataId)) {
                    return false;
                }

                if (allNullOrEmpty(group, profile, dataId)) {
                    group = jsonGroup;
                    profile = jsonProfile;
                    dataId = jsonDataId;
                }

            } catch (Exception e) {
                // ignore
            }
        }

        String uri = uriOf(request);

        if (!canOperate(group, uri)) {
            return false;
        }

        return !Strings.isNullOrEmpty(group) && hasPermission(group, profile, dataId, getPermissionNeeded(uri));
    }

    private boolean canOperate(String group, String uri) {
        if (!limitOperateApps.contains(group)) {
            return true;
        }

        for (String prefix : MODIFY_URI_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return false;
            }
        }

        return true;
    }

    // 多个List<PermissionType>必须都要满足
    private boolean hasPermission(String group, String profile, String dataId, List<List<PermissionType>> permissionNeeded) {
        for (List<PermissionType> permissionTypes : permissionNeeded) {
            if (!hasAnyPermission(group, profile, dataId, permissionTypes)) {
                return false;
            }
        }
        return true;
    }

    // 多个PermissionType只要满足一个就好
    private boolean hasAnyPermission(String group, String profile, String dataId, List<PermissionType> permissionNeeded) {
        for (PermissionType permissionType : permissionNeeded) {
            if (hasPermission(group, profile, dataId, permissionType)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermission(String group, String profile, String dataId, PermissionType permissionType) {
        if (Strings.isNullOrEmpty(dataId)) {
            return permissionService.hasPermission(group, profile, permissionType);
        } else {
            return permissionService.hasFilePermission(group, profile, dataId, permissionType);
        }
    }

    private boolean isLegal(String group, String profile, String dataId, String jsonGroup, String jsonProfile, String jsonDataId) {
        return allNullOrEmpty(group, profile, dataId) ||
                allNullOrEmpty(jsonGroup, jsonProfile, jsonDataId) ||
                Strings.nullToEmpty(group).equals(Strings.nullToEmpty(jsonGroup))
                        && Strings.nullToEmpty(profile).equals(Strings.nullToEmpty(jsonProfile))
                        && Strings.nullToEmpty(dataId).equals(Strings.nullToEmpty(jsonDataId));

    }

    private boolean allNullOrEmpty(String str1, String str2, String str3) {
        return Strings.isNullOrEmpty(str1) && Strings.isNullOrEmpty(str2) && Strings.isNullOrEmpty(str3);
    }

    private List<List<PermissionType>> getPermissionNeeded(String uri) {
        for (Map.Entry<String, List<List<PermissionType>>> entry : CHECK_REQUIRED.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
