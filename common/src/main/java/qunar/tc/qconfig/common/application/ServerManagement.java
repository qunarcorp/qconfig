package qunar.tc.qconfig.common.application;


import qunar.tc.qconfig.common.bean.AppServerConfig;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-11-24
 * Time: 下午3:49
 */
public interface ServerManagement {

    AppServerConfig getAppServerConfig();

    boolean healthCheck();

}