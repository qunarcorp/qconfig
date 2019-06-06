package qunar.tc.qconfig.admin.service;


import qunar.tc.qconfig.common.support.Application;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 11:13
 */
public interface ApplicationInfoService {

    /**
     * 通过应用ID获取配置文件中校验用的Token
     *
     * @param appCode 应用Id
     * @return Token
     */
    String getToken(String appCode);

    /**
     * 检查应用是否存在
     *
     * @param appCode 应用Id
     * @return 存在则返回True
     */
    boolean checkExist(String appCode);

    /**
     * 创建应用
     *
     * @param application 应用信息
     * @return 应用数量
     */
    int createApplication(Application application);

    /**
     * 更新应用
     *
     * @param application 应用信息
     * @return 应用数量
     */
    int updateApplication(Application application);

    /**
     * 获取应用信息
     *
     * @param appCode 应用id
     * @return 应用信息
     */
    Application getGroupInfo(String appCode);

    /**
     * 根据用户ID获取有权限查看的应用
     *
     * @param rtxId 用户ID
     * @return 应用信息
     */
    List<Application> getGroupInfos(String rtxId);
}
