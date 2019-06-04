package qunar.tc.qconfig.admin.service;

import java.util.Map;

/**
 * Created by chenjk on 2018/6/8.
 */
public interface SubenvService {
    Map<String, String> getGroupInfoMapByAppIdAndEnv(String appId, String env);
}
