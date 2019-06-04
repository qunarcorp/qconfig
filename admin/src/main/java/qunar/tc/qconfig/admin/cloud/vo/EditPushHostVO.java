package qunar.tc.qconfig.admin.cloud.vo;

/**
 * Created by pingyang.yang on 2018/10/25
 */
public class EditPushHostVO {

    private String ipAndPort;

    private int currentVersion;

    public EditPushHostVO(String ipAndPort, int currentVersion) {
        this.ipAndPort = ipAndPort;
        this.currentVersion = currentVersion;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public String toString() {
        return "EditPushHostVO{" +
                "ipAndPort='" + ipAndPort + '\'' +
                ", currentVersion=" + currentVersion +
                '}';
    }
}
