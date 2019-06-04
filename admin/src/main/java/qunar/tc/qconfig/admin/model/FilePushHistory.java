package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.web.bean.PushStatus;
import qunar.tc.qconfig.admin.web.bean.PushType;

/**
 * Created by pingyang.yang on 2018/10/23
 */
public class FilePushHistory {

    private String group;

    private String dataId;

    private String profile;

    private int version;

    private String MD5;

    private PushType type;

    private PushStatus status;

    private String IP;

    private String operator;

    private int port;

    public FilePushHistory() {
    }

    public FilePushHistory(String group, String dataId, String profile, int version, String MD5, PushType type,
            PushStatus status, String IP, int port, String operator) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.version = version;
        this.MD5 = MD5;
        this.type = type;
        this.status = status;
        this.IP = IP;
        this.port = port;
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    public PushType getType() {
        return type;
    }

    public void setType(PushType type) {
        this.type = type;
    }

    public PushStatus getStatus() {
        return status;
    }

    public void setStatus(PushStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FilePushHistory{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", version=" + version +
                ", MD5='" + MD5 + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", IP='" + IP + '\'' +
                ", operator='" + operator + '\'' +
                ", port=" + port +
                '}';
    }
}
