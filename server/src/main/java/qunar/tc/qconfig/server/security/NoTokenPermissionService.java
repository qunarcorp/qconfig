package qunar.tc.qconfig.server.security;

/**
 * @author zhenyu.nie created on 2016 2016/11/30 14:39
 */
public interface NoTokenPermissionService {

    boolean hasPermission(String group, String dataId);
}
