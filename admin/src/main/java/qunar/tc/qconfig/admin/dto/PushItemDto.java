package qunar.tc.qconfig.admin.dto;

/**
 * @author zhenyu.nie created on 2014 2014/7/9 1:49
 */
public class PushItemDto {

    private String ipAndPort;

    private String sourceGroup;

    private String sourceDataId;

    private String sourceProfile;

    public String getIpAndPort() {
        return ipAndPort;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public String getSourceGroup() {
        return sourceGroup;
    }

    public void setSourceGroup(String sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    public String getSourceDataId() {
        return sourceDataId;
    }

    public void setSourceDataId(String sourceDataId) {
        this.sourceDataId = sourceDataId;
    }

    public String getSourceProfile() {
        return sourceProfile;
    }

    public void setSourceProfile(String sourceProfile) {
        this.sourceProfile = sourceProfile;
    }

    @Override
    public String toString() {
        return "PushItemDto{" +
                "ipAndPort='" + ipAndPort + '\'' +
                ", sourceGroup='" + sourceGroup + '\'' +
                ", sourceDataId='" + sourceDataId + '\'' +
                ", sourceProfile='" + sourceProfile + '\'' +
                '}';
    }
}
