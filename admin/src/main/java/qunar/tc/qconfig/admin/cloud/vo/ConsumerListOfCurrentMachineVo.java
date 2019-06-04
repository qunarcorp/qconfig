package qunar.tc.qconfig.admin.cloud.vo;

import qunar.tc.qconfig.admin.model.ConfigUsedLog;

import java.util.List;
import java.util.Map;

public class ConsumerListOfCurrentMachineVo {
    private String group;
    private String env;
    private Map<String, List<ConfigUsedLog>> host2ConfigUsedLogs;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Map<String, List<ConfigUsedLog>> getHost2ConfigUsedLogs() {
        return host2ConfigUsedLogs;
    }

    public void setHost2ConfigUsedLogs(Map<String, List<ConfigUsedLog>> host2ConfigUsedLogs) {
        this.host2ConfigUsedLogs = host2ConfigUsedLogs;
    }
}
