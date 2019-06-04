package qunar.tc.qconfig.admin.service;

/**
 * @author zhenyu.nie created on 2016 2016/12/13 17:19
 */
public interface PropertiesCheckService {

    void checkConflictProperty(String group, String dataId, String data);

    boolean isRealPropertyFile(String group, String dataId, String data);
}
