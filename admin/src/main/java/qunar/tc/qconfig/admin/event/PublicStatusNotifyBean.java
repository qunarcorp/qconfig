package qunar.tc.qconfig.admin.event;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/6/27 15:49
 */
public class PublicStatusNotifyBean {

    public PublicStatusNotifyBean(ConfigOperationEvent event, ConfigMeta configMeta, long basedVersion, long currentVersion, String remarks, String operator, String ip) {
        this.event = event;
        this.configMeta = configMeta;
        this.basedVersion = basedVersion;
        this.currentVersion = currentVersion;
        this.remarks = remarks;
        this.operator = operator;
        this.ip  = ip;
    }

    public final ConfigOperationEvent event;
    public final ConfigMeta configMeta;
    public final long basedVersion;
    public final long currentVersion;
    public final String remarks;
    public final String operator;
    public final String ip;

    @Override
    public String toString() {
        return "PublicStatusNotifyBean{" +
                "event=" + event +
                ", configMeta=" + configMeta +
                ", basedVersion=" + basedVersion +
                ", currentVersion=" + currentVersion +
                ", remarks='" + remarks + '\'' +
                ", operator='" + operator + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
