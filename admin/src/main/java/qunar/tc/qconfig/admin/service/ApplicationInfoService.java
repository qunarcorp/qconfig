package qunar.tc.qconfig.admin.service;


import qunar.tc.qconfig.common.support.Application;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 11:13
 */
public interface ApplicationInfoService {

    String getToken(String appCode);

    boolean checkExist(String appCode);

    int createApplication(Application application);

    int updateApplication(Application application);

    Application getGroupInfo(String group);

    List<Application> getGroupInfos(String rtxId);
}
