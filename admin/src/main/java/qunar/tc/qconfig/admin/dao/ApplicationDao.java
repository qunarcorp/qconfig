package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.common.support.Application;

import java.util.List;

/**
 * Created by pingyang.yang on 2018/11/26
 */
public interface ApplicationDao {

    Application getApplicationByAppCode(String appCode);

    int createApplication (Application application);

    List<Application> getApplicationsByAppCode(List<String> appCode);

    boolean checkExist(String appCode);

    int updateApplicationMail (Application application);
}
