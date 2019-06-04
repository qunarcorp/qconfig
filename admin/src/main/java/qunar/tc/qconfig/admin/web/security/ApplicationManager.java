package qunar.tc.qconfig.admin.web.security;

import java.util.*;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.support.Application;

/**
 * 应用中心接口
 * Created by zhouhongbin on 2017/9/22.
 */

@SuppressWarnings("unused")
public interface ApplicationManager {

    Map<String, Map<String, Set<String>>> getAllEnvs(Set<String> appCodes);

    Application getAppByCode(String code);

    List<Application> listByLoginId(String loginId);
}
